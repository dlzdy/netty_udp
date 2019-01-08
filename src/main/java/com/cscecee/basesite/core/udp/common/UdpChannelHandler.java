package com.cscecee.basesite.core.udp.common;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;


/**
 * 处理发送消息，接收消息
 */
public class UdpChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final static Logger logger = LoggerFactory.getLogger(UdpChannelHandler.class);
	
	public static int FRAME_SIZE = 1300;
	// 业务线程池
	private ThreadPoolExecutor executor;
	
	private UdpEndPoint udpEndPoint;

	private ConcurrentMap<String, Map<String, String>> peersMap = new ConcurrentHashMap<>();

	private ConcurrentMap<String, RpcFuture> pendingTasks = new ConcurrentHashMap<>();

	private Throwable ConnectionClosed = new Exception("rpc connection not active error");
	
	public UdpChannelHandler(UdpEndPoint udpEndPoint, int workerThreads) {
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
			RpcMsg messageInput = RpcMsg.fromByteBuf(in);
			//System.out.println(datagramPacket.content().toString(CharsetUtil.UTF_8));
//			String requestId = readStr(in);//requestId
//			Boolean isRsp =in.readBoolean();
//			String fromId = readStr(in);//fromId
//			String command = readStr(in);//command
//			Boolean isCompressed =in.readBoolean();//isCompressed
//			int dataLen = in.readInt();
//			byte[] data = new byte[dataLen];
//			in.readBytes(data);
			//
//			while (in.isReadable()) {
//                byte[] dst = new byte[in.readableBytes()];
//                in.readBytes(dst);
//			}
//			int aa = dataLen / 1024;
//			int bb = dataLen % 1024;
//			for (int i=0 ; i < aa; i++) {
//				byte[] dataTemp = new byte[1024];
//				//in.readBytes(dataTemp);
//				System.arraycopy(dataTemp, 0, data, i*1024, dataTemp.length);
//			}
//			byte[] dataTemp = new byte[bb];
//			in.readBytes(dataTemp);
//			System.arraycopy(dataTemp, 0, data, aa*1024, dataTemp.length);
//			System.out.println(new String(data,CharsetUtil.UTF_8));
			//
			
			// 用业务线程处理消息
			this.executor.execute(() -> {
				//RpcMsg messageInput;
				byte[] tmpData = messageInput.getData(); 
				if (messageInput.getIsCompressed()) {//接收进行解压
					try {
						tmpData = GzipUtils.ungzip(tmpData);
						messageInput.setData(tmpData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (messageInput.getIsRsp()) {//响应消息
					RpcFuture future = (RpcFuture) pendingTasks.remove(messageInput.getRequestId());
					if (future == null) {
						logger.error("future not found with command {}", messageInput.getCommand());
						return;
					}
					future.success(messageInput.getData());					
				}else {// 请求
					Map<String, String> clientMap = new HashMap<>();
					clientMap.put("ip", sender.getAddress().getHostAddress());
					clientMap.put("port", sender.getPort() + "");
					clientMap.put("time", System.currentTimeMillis() + "");
					peersMap.put(messageInput.getFromId(), clientMap);//放入缓存，记录对端的ip，port
					//messageInput = new RpcMsgReq(requestId, fromId, command, isCompressed, tmpData);
					this.handleMessage(ctx, sender, messageInput);
				}
			});

		} catch (Exception e) {
			logger.error("failed", e);
		}
	}
	
	private void handleMessage(ChannelHandlerContext ctx, InetSocketAddress sender, RpcMsg messageInput) {
		// 业务逻辑在这里
		
		RpcMsgHandler handler = udpEndPoint.getHandlers().get(messageInput.getCommand());
		if (handler != null) {
			handler.handle(ctx, sender, messageInput.getRequestId(),  messageInput.getData());
		} else {
			logger.error("not found handler of " + messageInput.getCommand());
		}
	}
	
//	private String readStr(ByteBuf in) {
//		int len = in.readInt();
//		if (len < 0 || len > (1 << 20)) {
//			throw new DecoderException("string too long len=" + len);
//		}
//		byte[] bytes = new byte[len];
//		in.readBytes(bytes);
//		return new String(bytes, Charsets.UTF8);
//	}

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
	public RpcFuture send(String peerId, RpcMsg msgReq) {
		RpcFuture future = new RpcFuture();
		Map<String, String> peerMap = peersMap.get(peerId);
		if (peerMap == null || peerMap.isEmpty()) {
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
	public  RpcFuture send(InetSocketAddress remoteSocketAddress, RpcMsg msgReq) {
		RpcFuture future = new RpcFuture();
	
		ByteBuf buf = msgReq.toByteBuf();
		if (getChannel() != null) {
			getChannel().eventLoop().execute(() -> {
				pendingTasks.put(msgReq.getRequestId(), future);
				// datasocket
				logger.info("send req " + msgReq.getCommand() +  " >>>>>  " + remoteSocketAddress);
				byte[] orignalData =  msgReq.getData();
				int frameTotalCount = msgReq.getData().length / UdpChannelHandler.FRAME_SIZE;
				int leftDataLen = msgReq.getData().length % UdpChannelHandler.FRAME_SIZE;
				if (leftDataLen != 0) {
					frameTotalCount++;
				}
				for (int i = 0; i <= frameTotalCount; i++) {
					try {
						RpcMsg tempMsgReq =(RpcMsg)msgReq.clone();
						tempMsgReq.setFragmentIndex(i+1);
						tempMsgReq.setTotalFragment(frameTotalCount);
						if(i == frameTotalCount && leftDataLen > 0) {//最后一帧,有剩余
							byte[] tempData = new byte[leftDataLen];
							System.arraycopy(orignalData, i*UdpChannelHandler.FRAME_SIZE, tempData, 
									i*UdpChannelHandler.FRAME_SIZE + leftDataLen, leftDataLen);
							tempMsgReq.setData(tempData );
							getChannel().writeAndFlush(new DatagramPacket(tempMsgReq.toByteBuf(), remoteSocketAddress));
							
						}else {
							byte[] tempData = new byte[UdpChannelHandler.FRAME_SIZE];
							System.arraycopy(orignalData, i*UdpChannelHandler.FRAME_SIZE, tempData, 
									(i+1)*UdpChannelHandler.FRAME_SIZE, UdpChannelHandler.FRAME_SIZE);
							tempMsgReq.setData(tempData );
							getChannel().writeAndFlush(new DatagramPacket(tempMsgReq.toByteBuf(), remoteSocketAddress));
						}
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} else {
			future.fail(ConnectionClosed);
		}		
		return future;
	}
	
//	private void writeStr(ByteBuf buf, String s) {
//		buf.writeInt(s.length());
//		buf.writeBytes(s.getBytes(Charsets.UTF8));
//	}	
}
