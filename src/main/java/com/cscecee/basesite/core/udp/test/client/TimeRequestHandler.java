package com.cscecee.basesite.core.udp.test.client;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.RpcMsgHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class TimeRequestHandler extends RpcMsgHandler {
	
	private final static Logger logger = LoggerFactory.getLogger(TimeRequestHandler.class);

	SimpleDateFormat aDate=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
	/**
	 * data = time(long)
	 */
	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data) {
		String rspData = new String(aDate.format(new Date()));
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		writeStr(buf, requestId);// len+ reqId
		buf.writeBoolean(true);//isRsp=true
		writeStr(buf, "0" );//len+fromId, 响应的可以不填真实fromid
		writeStr(buf, "time_rsp");//****
		buf.writeBoolean(false);//isCompressed
		buf.writeInt(rspData.getBytes().length);// len
		buf.writeBytes(rspData.getBytes());// data
		//响应输出
		logger.info("send time_rsp>>>>>" + rspData);
		ctx.writeAndFlush(new DatagramPacket(buf, sender));
	}

}
