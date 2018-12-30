package com.cscecee.basesite.core.udp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.UdpEndPoint;

import io.netty.channel.ChannelFuture;

public class UdpServer extends UdpEndPoint {

	private final static Logger logger = LoggerFactory.getLogger(UdpServer.class);

	private int port;

	public UdpServer(int port) throws Exception {
		this.port = port;
		this.init();
	}

	/**
	 * 绑定端口, 客户端绑定0
	 * @throws Exception 
	 */
	@Override
	public void bind() throws Exception {
		if (channel == null || !channel.isActive()) {
			ChannelFuture channelFuture = bootstrap.bind(getPort()).sync();
			channel = channelFuture.channel();
			logger.info("server is start up, bind port = " +  port);
			channel.closeFuture().await();
		}else {
			logger.warn("channel is active, not need bind()");	
		}
	}

	@Override
	public int getPort() {
		return port;
	}

}
