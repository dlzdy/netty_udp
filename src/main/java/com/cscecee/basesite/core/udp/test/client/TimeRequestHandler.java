package com.cscecee.basesite.core.udp.test.client;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.RpcMsg;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class TimeRequestHandler extends RpcMsgHandler {
	
	private final static Logger logger = LoggerFactory.getLogger(TimeRequestHandler.class);

	SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
	/**
	 * data = time(long)
	 */
	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, long reqId, byte[] data) {
		String rspData = new String(aDate.format(new Date()));
	
		RpcMsg rpcMsg = new RpcMsg(reqId, true, "0", "time_rsp", false, rspData.getBytes());

		//响应输出
		logger.info("send time_rsp>>>>>" + rspData);
		ctx.writeAndFlush(new DatagramPacket(rpcMsg.toByteBuf(), sender));
	}

}
