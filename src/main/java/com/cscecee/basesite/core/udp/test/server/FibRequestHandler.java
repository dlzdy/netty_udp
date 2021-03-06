package com.cscecee.basesite.core.udp.test.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.RpcMsg;
import com.cscecee.basesite.core.udp.common.RpcMsgHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


//斐波那契和指数计算处理
public class FibRequestHandler extends RpcMsgHandler {

	private final static Logger logger = LoggerFactory.getLogger(FibRequestHandler.class);
	
	private List<Long> fibs = new ArrayList<>();

	{
		fibs.add(1L); // fib(0) = 1
		fibs.add(1L); // fib(1) = 1
	}

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, long reqId, byte[] data) {
		int n = Integer.valueOf(new String(data));
		for (int i = fibs.size(); i < n + 1; i++) {
			long value = fibs.get(i - 2) + fibs.get(i - 1);
			fibs.add(value);
		}
		
//		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
//		writeStr(buf, requestId);// len+ reqId
//		buf.writeBoolean(true);//isRsp=true
//		writeStr(buf, "0" );//len+fromId
//		writeStr(buf, "fib_rsp");//****
//		buf.writeBoolean(false);//isCompressed
		byte[] outData = (fibs.get(n) + "").getBytes(Charsets.UTF8);
//		buf.writeInt(outData.length);// len
//		buf.writeBytes(outData);// data
		//响应输出
		RpcMsg rpcMsg = new RpcMsg(reqId, 1, "0", "fib_rsp", false, outData);
		logger.info("send fib_res>>>>>" + fibs.get(n));
		ctx.writeAndFlush(new DatagramPacket(rpcMsg.toByteBuf(), sender));
	}

}