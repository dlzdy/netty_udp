package com.cscecee.basesite.core.udp.common;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;


public abstract class UdpEndPoint {

	private final static Logger logger = LoggerFactory.getLogger(UdpEndPoint.class);

	// 客户端ID
	protected String myId = "";
	//
	protected Bootstrap bootstrap;
	//
	protected EventLoopGroup eventLoopGroup;
	//
	protected Channel channel;
	
	protected InetSocketAddress remoteSocketAddress = null;

	protected UdpChannelHandler udpMessageHandler;
	
	protected Throwable ConnectionClosed = new Exception("rpc connection not active error");
	
	protected RpcMsgUtils handlers = new RpcMsgUtils();

	
	public Channel getChannel() {
		return channel;
	}

	public RpcMsgUtils getHandlers() {
		return handlers;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}
	/*
	 * 注册服务的快捷方式
	 */
	public void register(String type,  RpcMsgHandler handler) {
		handlers.register(type, handler);
	}

	public void init() throws Exception {
		bootstrap = new Bootstrap();
		// 1.设置bossGroup和workGroup
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap.group(eventLoopGroup);
		// 2.指定使用NioServerSocketChannel来处理连接请求。
		bootstrap.channel(NioDatagramChannel.class);
		// 3.配置TCP/UDP参数。
		// 4.配置handler和childHandler，数据处理器。
		udpMessageHandler =  new UdpChannelHandler(this,10);
		
		// bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {

			@Override
			protected void initChannel(NioDatagramChannel ch) throws Exception {
				// 注册hander
				ChannelPipeline pipe = ch.pipeline();
				// 将业务处理器放到最后
			    pipe.addLast(udpMessageHandler);
			}

		});
	}
	public abstract int getPort() ;

	public abstract void bind() throws Exception;
	/**
	 * 异步发送
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
//	private <T> RpcFuture<T> sendAsync(String command, boolean isCompressed, byte[] data) {
//		if (!started) {//未连接
//			try {
//				bind();
//				started = true;
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				started = false;
//			}
//		}
//		String requestId = RequestId.next();
//		MessageReq output = new MessageReq(requestId, myId, command, isCompressed, data);
//		return udpMessageHandler.send(output);
//	}


	/**
	 * 适用于客户端-->服务器
	 * @param type
	 * @param payload
	 * @return
	 */
//	public <T> T send(String command, boolean isCompressed, byte[] data) {
//		RpcFuture<T> future = sendAsync(command, isCompressed, data);
//		try {
//			return future.get();
//		} catch (Exception e) {
//			throw new RPCException(e);
//		}
//	}

	/**
	 * 适用于服务器-->客户端
	 * @param peerId
	 * @param command
	 * @param isCompressed
	 * @param data
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public byte[] send(String peerId, String command, boolean isCompressed, byte[] data) throws InterruptedException, ExecutionException {
		if (channel == null || !channel.isActive()) {
			throw new RPCException("channel is not active");
		}
		RpcMsgReq output = new RpcMsgReq(RequestId.next(), myId, command, isCompressed, data);
		RpcFuture future = udpMessageHandler.send(peerId, output);		
		return future.get();
	}
	
	/**
	 * 异步发送
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
//	private RpcFuture sendAsync(String peerId, String command, boolean isCompressed, byte[] data) {
//		if (channel == null || !channel.isActive()) {
//			throw new RPCException(" channel is not active");
//		}
//		
//		RpcMsgReq output = new RpcMsgReq(RequestId.next(), myId, command, isCompressed, data);
//		return udpMessageHandler.send(peerId, output);
//	}
	/**
	 * 适用于客户端-->服务器
	 * 
	 * @param type
	 * @param payload
	 * @return
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public byte[] send(String command, boolean isCompressed, byte[] data) throws InterruptedException, ExecutionException {
//		RpcFuture future = sendAsync(command, isCompressed, data);
		if (channel == null || !channel.isActive()) {
			throw new RPCException(" channel is not active");
		}
		
		RpcMsgReq output = new RpcMsgReq(RequestId.next(), myId, command, isCompressed, data);
		RpcFuture future =  udpMessageHandler.send(getRemoteSocketAddress(), output);
		return future.get();
//		try {
//			return future.get();
//		} catch (Exception e) {
//			return null;
//		}
	}
	
//	/**
//	 * 异步发送
//	 * 
//	 * @param type
//	 * @param payload
//	 * @return
//	 */
//	private RpcFuture sendAsync(String command, boolean isCompressed, byte[] data) {
//		if (channel == null || !channel.isActive()) {
//			throw new RPCException(" channel is not active");
//		}
//		
//		RpcMsgReq output = new RpcMsgReq(RequestId.next(), myId, command, isCompressed, data);
//		return udpMessageHandler.send(getRemoteSocketAddress(), output);
//	}
	/**
	 * 关闭
	 */
	public void close() {
		channel.close();
		eventLoopGroup.shutdownGracefully(0, 5000, TimeUnit.MILLISECONDS);
	}

}
