package com.cscecee.basesite.core.udp.common;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;


/**
 * 处理发送消息，接收消息
 */
public class UdpMessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final static Logger logger = LoggerFactory.getLogger(UdpMessageHandler.class);
	
	// 业务线程池
	private ThreadPoolExecutor executor;
	
	private UdpEndPoint udpEndPoint;

	private ConcurrentMap<String, Map<String, String>> peersMap = new ConcurrentHashMap<>();

	private ConcurrentMap<String, RpcFuture> pendingTasks = new ConcurrentHashMap<>();

	private Throwable ConnectionClosed = new Exception("rpc connection not active error");
	private Throwable NotFoundCommand = new Exception("not found command");
	
	public UdpMessageHandler(UdpEndPoint udpEndPoint, int workerThreads) {
		// 业务队列最大1000,避免堆积
		// 如果子线程处理不过来,io线程也会加入业务逻辑(callerRunsPolicy)
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
		// 给业务线程命名
		ThreadFactory factory = new ThreadFactory() {
			AtomicInteger seq = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("rpc-" + seq.getAndIncrement());
				logger.info("new rpc thread:" + t.getName());
				return t;
			}
		};
		// 闲置时间超过30秒的线程就自动销毁
		this.executor = new ThreadPoolExecutor(1, workerThreads, 30, TimeUnit.SECONDS, queue, factory,
				new CallerRunsPolicy());
		this.udpEndPoint = udpEndPoint;
	}

	public Channel getChannel() {
		return udpEndPoint.getChannel();
	}

	public void closeGracefully() {
		// 优雅一点关闭,先通知,再等待,最后强制关闭
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		this.executor.shutdownNow();
	}

	/**
	 * 处理接收到的消息, 响应的和请求的
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
		try {
			InetSocketAddress sender = datagramPacket.sender();
			ByteBuf in = datagramPacket.content();
			String requestId = readStr(in);//requestId
			Boolean isRsp =in.readBoolean();
			String fromId = readStr(in);//fromId
			String command = readStr(in);//command
			Boolean isCompressed =in.readBoolean();//isCompressed
			int dataLen = in.readInt();
			byte[] data = new byte[dataLen];
			in.readBytes(data);
			// 用业务线程处理消息
			this.executor.execute(() -> {
				MessageCommon messageInput;
				if (isRsp) {//响应消息
					messageInput = new MessageRsp(requestId, fromId, command, isCompressed, data);
					RpcFuture future = (RpcFuture) pendingTasks.remove(messageInput.getRequestId());
					if (future == null) {
						logger.error("future not found with command {}", messageInput.getCommand());
						future.fail(NotFoundCommand);
						return;
					}
					future.success(messageInput.getData());					
				}else {// 请求
					
					Map<String, String> clientMap = new HashMap<>();
					clientMap.put("ip", sender.getAddress().getHostAddress());
					clientMap.put("port", sender.getPort() + "");
					clientMap.put("time", System.currentTimeMillis() + "");
					
					peersMap.put(fromId, clientMap);//放入缓存，记录对端的ip，port
					messageInput = new MessageReq(requestId, fromId, command, isCompressed, data);
					this.handleMessage(ctx, sender, messageInput);
				}
			});

		} catch (Exception e) {
			logger.error("failed", e);
		}
	}
	
	private void handleMessage(ChannelHandlerContext ctx, InetSocketAddress sender, MessageCommon messageInput) {
		// 业务逻辑在这里
		
		IMessageHandler handler = udpEndPoint.getHandlers().get(messageInput.getCommand());
		if (handler != null) {
			handler.handle(ctx, sender, messageInput.getRequestId(),  messageInput.getData());
		} else {
			logger.error("not found handler of " + messageInput.getCommand());
		}
	}
	
	private String readStr(ByteBuf in) {
		int len = in.readInt();
		if (len < 0 || len > (1 << 20)) {
			throw new DecoderException("string too long len=" + len);
		}
		byte[] bytes = new byte[len];
		in.readBytes(bytes);
		return new String(bytes, Charsets.UTF8);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		logger.error(cause.getMessage(), cause);
	}

	/**
	 * 发送消息,服务器端-->客户端
	 * 因为客户端可能是局域网，不能指定ip
	 * @param peerId
	 * @param msgReq
	 * @return
	 */
	public RpcFuture send(String peerId, MessageReq msgReq) {
		Map<String, String> peerMap = peersMap.get(peerId);
		RpcFuture future = new RpcFuture();
		if (peerMap == null || peerMap.size() == 0) {
			future.fail(ConnectionClosed);
			return future;
		}
		String ip = peerMap.get("ip");
		int port = Integer.valueOf(peerMap.get("port"));
		long time = Long.valueOf(peerMap.get("time"));
		if (System.currentTimeMillis() - time > 30000 ) {
			future.fail(ConnectionClosed);
			return future;
		}
		InetSocketAddress remoteSocketAddress = new InetSocketAddress(ip, port);
		return send(remoteSocketAddress, msgReq);
	}
	/**
	 * 通用
	 * 发送消息,客户端-->服务器端
	 * @param msgReq
	 * @return
	 */
	public  RpcFuture send(InetSocketAddress remoteSocketAddress, MessageReq msgReq) {
		RpcFuture future = new RpcFuture();

		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		writeStr(buf, msgReq.getRequestId());// requestId
		buf.writeBoolean(msgReq.getIsRsp());// isRsp
		writeStr(buf, msgReq.getFromId() );// fromId
		writeStr(buf, msgReq.getCommand());//command
		buf.writeBoolean(msgReq.getIsCompressed());// isCompressed
		buf.writeInt(msgReq.getData().length);
		buf.writeBytes(msgReq.getData());//data
		//ctx.writeAndFlush(new DatagramPacket(buf, sender));
		if (getChannel() != null) {
			getChannel().eventLoop().execute(() -> {
				pendingTasks.put(msgReq.getRequestId(), future);
				// datasocket
				logger.debug("send reqId >>>>>" + msgReq.getRequestId());
				getChannel().writeAndFlush(new DatagramPacket(buf, remoteSocketAddress));
				
			});
		} else {
			future.fail(ConnectionClosed);
		}		
		return future;
	}
	
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}	
}
