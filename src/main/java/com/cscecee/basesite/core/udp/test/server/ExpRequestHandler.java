package com.cscecee.basesite.core.udp.test.server;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.RpcMsg;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;
import com.cscecee.basesite.core.udp.test.ExpRequest;
import com.cscecee.basesite.core.udp.test.ExpResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class ExpRequestHandler extends RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(ExpRequestHandler.class);

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, long reqId, byte[] data) {
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
//		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//		writeStr(buf, requestId);// len+ reqId
//		buf.writeBoolean(true);//isRsp=true
//		writeStr(buf, "0" );//len+fromId
//		writeStr(buf, "exp_rsp");//command
//		buf.writeBoolean(false);//isCompressed
		String strOutData = JSON.toJSONString(new ExpResponse(res, cost));
//		byte[] outData = strOutData.getBytes(Charsets.UTF8);
//		buf.writeInt(outData.length);// len
//		buf.writeBytes(outData);// data
		//响应输出
		RpcMsg rpcMsg = new RpcMsg(reqId, true, "0", "exp_rsp", false, strOutData.getBytes());
	
		logger.info("send exp_res>>>>>" + strOutData);
		ctx.writeAndFlush(new DatagramPacket(rpcMsg.toByteBuf(), sender));
	}
}
