package com.cscecee.basesite.core.udp.client;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.MessageReq;
import com.cscecee.basesite.core.udp.common.RPCException;
import com.cscecee.basesite.core.udp.common.RequestId;
import com.cscecee.basesite.core.udp.common.RpcFuture;
import com.cscecee.basesite.core.udp.common.UdpEndPoint;

import io.netty.channel.ChannelFuture;


public class UdpClient extends UdpEndPoint {
	
	private final static Logger logger = LoggerFactory.getLogger(UdpClient.class);

	private InetSocketAddress remoteSocketAddress = null;

	protected boolean started = false;

	private int localPort;

	public UdpClient(String serverName, int serverPort, int localPort, String myId) throws Exception {
		remoteSocketAddress = new InetSocketAddress(serverName, serverPort);
		this.localPort = localPort;
		this.myId = myId;
		this.init();
//		//启动服务端口
//		try {
//			this.bind(getPort());
//			started = true;
//		} catch (Exception e) {
//			started = false;
//			throw e;
//		}
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

	/**
	 * 绑定端口, 客户端绑定0
	 * @throws Exception 
	 */
	public void bind() throws Exception {
		if (channel == null || !channel.isActive()) {
			ChannelFuture channelFuture = bootstrap.bind(getPort()).sync();
			channel = channelFuture.channel();
			logger.info("client localAddress = " +  channel.localAddress());
			started = true;
		}
	}
	
	/**
	 * 适用于客户端-->服务器
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
	public byte[] send(String command, boolean isCompressed, byte[] data) {
		RpcFuture future = sendAsync(command, isCompressed, data);
		try {
			return future.get();
		} catch (Exception e) {
			throw new RPCException(e);
		}
	}
	
	/**
	 * 异步发送
	 * 
	 * @param type
	 * @param payload
	 * @return
	 */
	private RpcFuture sendAsync(String command, boolean isCompressed, byte[] data) {
		if (channel == null || !channel.isActive()) {
			throw new RPCException(" channel is not active");
		}
		
		MessageReq output = new MessageReq(RequestId.next(), myId, command, isCompressed, data);
		return udpMessageHandler.send(getRemoteSocketAddress(), output);
	}

	public void reconnect() {
		// if (stopped) {
		// return;
		// }
		// bootstrap.connect(ip, port).addListener(future -> {
		// if (future.isSuccess()) {
		// return;
		// }
		// if (!stopped) {
		// group.schedule(() -> {
		// reconnect();
		// }, 1, TimeUnit.SECONDS);
		// }
		// logger.error("connect {}:{} failure", ip, port, future.cause());
		// });
	}

	@Override
	public int getPort() {
		return this.localPort;
	}
}
