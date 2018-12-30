package com.cscecee.basesite.core.udp.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.MessageCommon;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;
import com.cscecee.basesite.core.udp.common.MessageReq;
import com.cscecee.basesite.core.udp.common.MessageRsp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;

/**
 * Created by sherry on 16/11/7.
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final static Logger logger = LoggerFactory.getLogger(UdpServerHandler.class);
	// 业务线程池
	private ThreadPoolExecutor executor;

	private MessageHandlers handlers;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		// TODO Auto-generated method stub
		
	}

//	private MessageRegistry registry;

//	public UdpServerHandler(MessageHandlers handlers, int workerThreads) {
//		// 业务队列最大1000,避免堆积
//		// 如果子线程处理不过来,io线程也会加入业务逻辑(callerRunsPolicy)
//		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
//		// 给业务线程命名
//		ThreadFactory factory = new ThreadFactory() {
//			AtomicInteger seq = new AtomicInteger();
//
//			@Override
//			public Thread newThread(Runnable r) {
//				Thread t = new Thread(r);
//				t.setName("rpc-" + seq.getAndIncrement());
//				return t;
//			}
//
//		};
//		// 闲置时间超过30秒的线程就自动销毁
//		this.executor = new ThreadPoolExecutor(1, workerThreads, 30, TimeUnit.SECONDS, queue, factory,
//				new CallerRunsPolicy());
//		this.handlers = handlers;
//		//this.registry = registry;
//	}
//
//	// public void closeGracefully() {
//	// //优雅一点关闭,先通知,再等待,最后强制关闭
//	// this.executor.shutdown();
//	// try {
//	// this.executor.awaitTermination(10, TimeUnit.SECONDS);
//	// } catch (InterruptedException e) {
//	// }
//	// this.executor.shutdownNow();
//	// }
//	@Override
//	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
//		try {
//			InetSocketAddress sender = datagramPacket.sender();
//			ByteBuf in = datagramPacket.content();
//			
////			 * requestId string
////			 * isRsp byte
////			 * fromId string
////			 * command string
////			 * isCompressed byte
////			 * data byte[]
//					 
//			String requestId = readStr(in);//requestId
//			byte isRsp =in.readByte();
//			String fromId = readStr(in);//fromId
//			String command = readStr(in);//command
//			byte isCompressed =in.readByte();//isCompressed
//			int dataLen = in.readInt();
//			byte[] data = new byte[dataLen];
//			in.readBytes(data);
//			// 用业务线程处理消息
//			this.executor.execute(() -> {
//				MessageCommon messageInput;
//				if (isRsp == 0) {//req
//					messageInput = new MessageRsp(requestId, fromId, command, isCompressed, data);
//				}else {
//					messageInput = new MessageReq(requestId, fromId, command, isCompressed, data);
//				}
//				this.handleMessage(ctx, sender, messageInput);
//			});
//
//		} catch (Exception e) {
//			logger.error("failed", e);
//		}
//	}
//	
//	private void handleMessage(ChannelHandlerContext ctx, InetSocketAddress sender, MessageCommon messageInput) {
//		// 业务逻辑在这里
//		
//		IMessageHandler handler = handlers.get(messageInput.getCommand());
//		if (handler != null) {
//			handler.handle(ctx, sender, messageInput.getRequestId(),  messageInput.getData());
//		} else {
//			//handlers.defaultHandler().handle(ctx, input.getRequestId(), input);
//			logger.error("not found handler of " + messageInput.getCommand());
//		}
//	}
//	private String readStr(ByteBuf in) {
//		int len = in.readInt();
//		if (len < 0 || len > (1 << 20)) {
//			throw new DecoderException("string too long len=" + len);
//		}
//		byte[] bytes = new byte[len];
//		in.readBytes(bytes);
//		return new String(bytes, Charsets.UTF8);
//	}
//
//	@Override
//	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		ctx.close();
//		logger.error(cause.getMessage(), cause);
//	}
//
////	public <T> RpcFuture<T> send(CommonMessage output) {
////		RpcFuture<T> future = new RpcFuture<T>();
////
////		
////		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
////		writeStr(buf, output.getFromId() );// fromId
////		writeStr(buf, output.getRequestId());// requestId
////		writeStr(buf, output.getType());//type
////		writeStr(buf, JSON.toJSONString(output.getPayload()));//payload
////		//ctx.writeAndFlush(new DatagramPacket(buf, sender));
////		Channel channel = udpClient.getChannel();
////		if (channel != null) {
////			channel.eventLoop().execute(() -> {
////				pendingTasks.put(output.getRequestId(), future);
////				// datasocket
////				channel.writeAndFlush(new DatagramPacket(buf, udpClient.getRemoteSocketAddress()));
////				
////			});
////		} else {
////			future.fail(ConnectionClosed);
////		}		
////		return future;
////	}
//	
//	private void writeStr(ByteBuf buf, String s) {
//		buf.writeInt(s.length());
//		buf.writeBytes(s.getBytes(Charsets.UTF8));
//	}	
}
