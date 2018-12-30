package com.cscecee.basesite.core.udp.test.client;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.IMessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class TimeRequestHandler implements IMessageHandler {
	 SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
	/**
	 * payload = time
	 */
	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data) {
		//响应输出
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		String fromId = "0";
		writeStr(buf, fromId );
		writeStr(buf, requestId);
		writeStr(buf, "exp_res");//****
		writeStr(buf, aDate.format(new Date()));
		ctx.writeAndFlush(new DatagramPacket(buf, sender));
	}
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}
}
