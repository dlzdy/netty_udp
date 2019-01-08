package com.cscecee.basesite.core.udp.test.server;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;
import com.cscecee.basesite.core.udp.test.ExpRequest;
import com.cscecee.basesite.core.udp.test.ExpResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class BigdataRequestHandler extends RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(BigdataRequestHandler.class);

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, byte[] data) {
		// ExpRequest
		JSONObject message = null;
		try {
			message = JSON.parseObject(new String(data, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(message.toJSONString());
		
		//响应输出
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		writeStr(buf, requestId);// len+ reqId
		buf.writeBoolean(true);//isRsp=true
		writeStr(buf, "0" );//len+fromId
		writeStr(buf, "bigdata_rsp");//command
		buf.writeBoolean(false);//isCompressed
		
		String strOutData = "bigdata is ok";
		byte[] outData = strOutData.getBytes(Charsets.UTF8);
		buf.writeInt(outData.length);// len
		buf.writeBytes(outData);// data
		//响应输出
		logger.info("send bigdata_rsp>>>>>" + strOutData);
		ctx.writeAndFlush(new DatagramPacket(buf, sender));
	}
}
