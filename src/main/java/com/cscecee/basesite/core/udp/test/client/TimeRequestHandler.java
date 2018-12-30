package com.cscecee.basesite.core.udp.test.client;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class TimeRequestHandler extends RpcMsgHandler {
	 SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
	/**
	 * payload = time
	 */
	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data) {
//		int n = Integer.valueOf(new String(data));
//		
//		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//		writeStr(buf, requestId);// len+ reqId
//		buf.writeBoolean(true);//isRsp=true
//		writeStr(buf, "0" );//len+fromId
//		writeStr(buf, "fib_res");//****
//		buf.writeBoolean(false);//isCompressed
//		byte[] outData = (fibs.get(n) + "").getBytes(Charsets.UTF8);
//		buf.writeInt(outData.length);// len
//		buf.writeBytes(outData);// data
//		//响应输出
//		logger.info("send fib_res>>>>>" + fibs.get(n));
//		ctx.writeAndFlush(new DatagramPacket(buf, sender));
	}

}
