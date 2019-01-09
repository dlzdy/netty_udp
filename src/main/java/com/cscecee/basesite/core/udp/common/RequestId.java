package com.cscecee.basesite.core.udp.common;

public class RequestId {
	//简单UUID 64
//	public static String next() {
//		return UUID.randomUUID().toString().replace("-", "");
//	}
	static SnowflakeIdWorker idWorker = new SnowflakeIdWorker();
	public static long nextId() {
		return idWorker.nextId();
	}
}
