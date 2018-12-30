package com.cscecee.basesite.core.udp.test.server;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;
import com.cscecee.basesite.core.udp.test.ExpRequest;
import com.cscecee.basesite.core.udp.test.ExpResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class ExpRequestHandler implements RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(ExpRequestHandler.class);

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data) {
		// ExpRequest
		ExpRequest message = JSON.parseObject(new String(data), ExpRequest.class);
		int base = message.getBase();
		int exp = message.getExp();
		long start = System.nanoTime();
		long res = 1;
		for (int i = 0; i < exp; i++) {
			res *= base;
		}
		long cost = System.nanoTime() - start;
		//响应输出
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		writeStr(buf, requestId);// len+ reqId
		buf.writeBoolean(true);//isRsp=true
		writeStr(buf, "0" );//len+fromId
		writeStr(buf, "exp_res");//command
		buf.writeBoolean(false);//isCompressed
		String strOutData = JSON.toJSONString(new ExpResponse(res, cost));
		byte[] outData = strOutData.getBytes(Charsets.UTF8);
		buf.writeInt(outData.length);// len
		buf.writeBytes(outData);// data
		//响应输出
		logger.info("send exp_res>>>>>" + strOutData);
		ctx.writeAndFlush(new DatagramPacket(buf, sender));
	}
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}
}
