package com.cscecee.basesite.core.udp.test.server;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.RpcMsg;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class BigdataRequestHandler extends RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(BigdataRequestHandler.class);

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, long reqId, byte[] data) {
		try {
			File file =new File(System.currentTimeMillis() + ".dat");
			FileOutputStream out =new FileOutputStream(file);
			out.write(data);
			out.close();
			System.out.println("save file = " + file.getAbsolutePath());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		String rspData = "bigdata is ok";
		RpcMsg rpcMsg = new RpcMsg(reqId, 1, "0", "bigdata_rsp", false, rspData.getBytes());

		//响应输出
		logger.info("send bigdata_rsp>>>>>" + rspData);
		ctx.writeAndFlush(new DatagramPacket(rpcMsg.toByteBuf(), sender));
	}
}
