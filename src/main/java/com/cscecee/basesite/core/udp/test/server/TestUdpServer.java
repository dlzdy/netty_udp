package com.cscecee.basesite.core.udp.test.server;

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
	public Object time() {
		byte[] result  = server.send("time", "".getBytes());
		return  new String(result, Charsets.UTF8);
	}

	public static void main(String[] args) throws Exception {
		UdpServer server = new UdpServer(8800);
		server.register("fib", new FibRequestHandler());
		server.register("exp", new ExpRequestHandler());
		server.register("heatbeat", new HeatbeatRequestHandler());
		TestUdpServer testServer = new TestUdpServer(server);
		server.bind();
		//testServer.time();
	}
}
