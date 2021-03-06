package com.cscecee.basesite.core.udp.common;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
/*
 * 消息处理器接口，每个自定义服务必须实现handle方法
 */
public abstract class RpcMsgHandler {
	/*
	 * 处理接收到的数据
	 * data 是未压缩数据
	 */
	public abstract void handle(ChannelHandlerContext ctx, InetSocketAddress sender, long reqId, byte[] data);
	
//	protected void writeStr(ByteBuf buf, String s) {
//		buf.writeInt(s.length());
//		buf.writeBytes(s.getBytes(Charsets.UTF8));
//	}
}
