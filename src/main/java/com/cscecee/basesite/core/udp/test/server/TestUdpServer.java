package com.cscecee.basesite.core.udp.test.server;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.server.HeatbeatRequestHandler;
import com.cscecee.basesite.core.udp.server.UdpServer;
import com.cscecee.basesite.core.udp.test.server.ExpRequestHandler;
import com.cscecee.basesite.core.udp.test.server.FibRequestHandler;

public class TestUdpServer {

	UdpServer server;
	public TestUdpServer(UdpServer server ) {
		this.server = server;
	}
	public String time(String clientId) {
		try {
			byte[] result = server.send(clientId, "time", false, "".getBytes());
			return  new String(result, Charsets.UTF8);
		} catch (Exception e) {
			e.printStackTrace();
			return  null;
		}
	}

	public static void main(String[] args) throws Exception {
		UdpServer server = new UdpServer(8800);
		server.register("fib", new FibRequestHandler());
		server.register("exp", new ExpRequestHandler());
		server.register("heatbeat", new HeatbeatRequestHandler());
		server.register("bigdata", new BigdataRequestHandler());
		TestUdpServer testServer = new TestUdpServer(server);
		server.bind();
//		while(true) {
//			try {
//				TimeUnit.SECONDS.sleep(60);
//				System.out.println("get time from client " + testServer.time("zdy001"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}
}
