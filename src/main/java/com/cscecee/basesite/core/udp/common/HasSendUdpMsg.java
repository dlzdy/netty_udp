package com.cscecee.basesite.core.udp.common;

import io.netty.channel.socket.DatagramPacket;

public class HasSendUdpMsg {
	
	private DatagramPacket updPkg;
	
	private int sendCount = 1;
	
	private long lastSendtime = System.currentTimeMillis();

	public DatagramPacket getUpdPkg() {
		return updPkg;
	}

	public void setUpdPkg(DatagramPacket updPkg) {
		this.updPkg = updPkg;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public long getLastSendtime() {
		return lastSendtime;
	}

	public void setLastSendtime(long lastSendtime) {
		this.lastSendtime = lastSendtime;
	}

}
