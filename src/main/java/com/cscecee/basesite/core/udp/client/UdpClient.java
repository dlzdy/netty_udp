package com.cscecee.basesite.core.udp.client;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.UdpEndPoint;

import io.netty.channel.ChannelFuture;


public class UdpClient extends UdpEndPoint {
	
	private final static Logger logger = LoggerFactory.getLogger(UdpClient.class);

	protected boolean heatbeatStarted = false;
	
	protected Thread heatbeatThread = null;

	private int localPort;
	// 与服务器连接状态
	private boolean isConnected = false;
	
	public UdpClient(String serverName, int serverPort, int localPort, String myId) throws Exception {
		this.remoteSocketAddress = new InetSocketAddress(serverName, serverPort);
		this.localPort = localPort;
		this.myId = myId;
		this.init();
	}


	/**
	 * 绑定端口, 客户端绑定0
	 * @throws Exception 
	 */
	@Override
	public void bind() throws Exception {
		if (channel == null || !channel.isActive()) {
			ChannelFuture channelFuture = bootstrap.bind(getPort()).sync();
			channel = channelFuture.channel();
			logger.info("client localAddress = " +  channel.localAddress());
		}
	}
	

	@Override
	public int getPort() {
		return this.localPort;
	}

	/**
	 * 心跳协议
	 * @return
	 */
	public String heartbeat() {
		try {
			byte[] result = send("heatbeat", false, ("hello").getBytes());
			return new String(result, Charsets.UTF8);
		} catch (Exception e) {
			return null;
		}

	}
	/**
	 * 启用心跳线程
	 */
	public void startHeartbeat() {
		heatbeatStarted = true;
		if (heatbeatThread != null) {
			heatbeatThread.interrupt();
		}
		heatbeatThread = null;
		heatbeatThread = new Thread() {
			public void run() {
				while(heatbeatStarted) {
					try {
						String result = heartbeat();
						if ("hello".equalsIgnoreCase(result)) {//心跳有响应
							isConnected = true;
							logger.info("heatbeat return ok");
							//
						}else {
							isConnected = false;
							logger.error("heatbeat return error");
						}
					}catch (Exception e) {
						isConnected = false;
						logger.error("heatbeat return error", e);
					}
					//间隔随机10~20秒，不能超过30秒
					try {
						TimeUnit.SECONDS.sleep(10 + (new Random().nextInt(10)));
					} catch (InterruptedException e) {
						
					}
				}
				
			}
		};
		heatbeatThread.start();
	}
	/**
	 * 停止心跳线程
	 */
	public void stopHeatbeat() {
		heatbeatStarted = false;
		heatbeatThread.interrupt();
		heatbeatThread = null;
	}

}
