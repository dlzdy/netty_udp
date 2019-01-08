package com.cscecee.basesite.core.udp.server;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.RpcMsg;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;
import com.cscecee.basesite.core.udp.test.ExpRequest;
import com.cscecee.basesite.core.udp.test.ExpResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class HeatbeatRequestHandler extends RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(HeatbeatRequestHandler.class);

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data) {
//		String rspData = new String(data);
//		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//		writeStr(buf, requestId);// len+ reqId
//		buf.writeBoolean(true);//isRsp=true
//		writeStr(buf, "0" );//len+fromId
//		writeStr(buf, "heatbeat_rsp");//****
//		buf.writeBoolean(false);//isCompressed
//		buf.writeInt(data.length);// len
//		buf.writeBytes(data);// data
		RpcMsg rpcMsg = new RpcMsg(requestId, true, "0", "heatbeat_rsp", false, data);
		//响应输出
		logger.info("send rsp " + new String(data) + "-->" + sender);
		ctx.writeAndFlush(new DatagramPacket(rpcMsg.toByteBuf(), sender));	}
}
