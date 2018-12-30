package com.cscecee.basesite.core.udp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageHandlers;
import com.cscecee.basesite.core.udp.common.UdpMessageHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;


public class UdpServer333 {

	private final static Logger logger = LoggerFactory.getLogger(UdpServer333.class);
	// 服务器端口
	private int port;

	private Bootstrap bootstrap;
	private EventLoopGroup eventLoopGroup;
	private Channel channel;
	private UdpMessageHandler serverHandler;
	

	private MessageHandlers handlers = new MessageHandlers();
	
	public UdpServer333(int port) {
		this.port = port;
	}
	/*
	 * 注册服务的快捷方式
	 */
	public void register(String type,  IMessageHandler handler) {
		handlers.register(type, handler);
	}
	
	public void start() {
//		eventLoopGroup = new NioEventLoopGroup();
//		bootstrap = new Bootstrap();
//		// 1.设置bossGroup和workGroup
//		bootstrap.group(eventLoopGroup);
//		// 2.指定使用NioServerSocketChannel来处理连接请求。
//		bootstrap.channel(NioDatagramChannel.class);
//		// 3.配置TCP/UDP参数。
//		//bootstrap.option(ChannelOption.SO_BROADCAST, true);
//		//serverHandler = new UdpServerHandler(handlers ,10);
//		serverHandler = new UdpMessageHandler(handlers ,10);
//		// 4.配置handler 数据处理器。
//		bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
//			@Override
//			protected void initChannel(NioDatagramChannel ch) throws Exception {
//				// 注册hander
//				ChannelPipeline pipe = ch.pipeline();
//				pipe.addLast(serverHandler);
//			}
//
//		});
//
//		// 5.bootstrap启动服务器。
//		try {
//			ChannelFuture sync = bootstrap.bind(port).sync();
//			logger.info("udp server is running ......");
//			channel = sync.channel();
//			serverHandler.setChannel(channel);//*****
//			channel.closeFuture().await();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			eventLoopGroup.shutdownGracefully();
//		}		
	}

}
