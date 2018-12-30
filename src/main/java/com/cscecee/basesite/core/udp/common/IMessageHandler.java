package com.cscecee.basesite.core.udp.common;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
/*
 * 消息处理器接口，每个自定义服务必须实现handle方法
 */
public interface IMessageHandler {

	void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data);

}
