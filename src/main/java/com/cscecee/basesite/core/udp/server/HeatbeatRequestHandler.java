package com.cscecee.basesite.core.udp.server;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.RpcMsg;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class HeatbeatRequestHandler extends RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(HeatbeatRequestHandler.class);

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, long reqId, byte[] data) {
		RpcMsg rpcMsg = new RpcMsg(reqId, true, "0", "heatbeat_rsp", false, data);
		//响应输出
		logger.info("send rsp " + new String(data) + "-->" + sender);
		ctx.writeAndFlush(new DatagramPacket(rpcMsg.toByteBuf(), sender));	}
}
