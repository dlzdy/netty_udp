package com.cscecee.basesite.core.udp.test.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.client.UdpClient;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.RPCException;
import com.cscecee.basesite.core.udp.test.ExpRequest;
import com.cscecee.basesite.core.udp.test.ExpResponse;

public class TestUdpClient {
	private final static Logger logger = LoggerFactory.getLogger(TestUdpClient.class);

	private UdpClient client;

	public TestUdpClient(UdpClient client) {
		this.client = client;
		this.client.register("time", new TimeRequestHandler());
	}

	public String fib(int n) {
		byte[] result = client.send("fib", false, (n + "").getBytes());
		return new String(result, Charsets.UTF8);
	}

	// ExpResponse
	public Object exp(int base, int exp) {
		byte[] result = client.send("exp", false, JSON.toJSONString(new ExpRequest(base, exp)).getBytes());
		return new String(result, Charsets.UTF8);
		// return (ExpResponse) client.send("exp", new ExpRequest(base, exp));
	}
	// RPC客户端要链接远程IP端口，并注册服务输出类(RPC响应类)，
	// 然后分别调用20次斐波那契服务和指数服务，输出结果
	public static void main(String[] args) throws Exception {
		//UdpClient client = new UdpClient("localhost", 8800, 0, UUID.randomUUID().toString().replaceAll("-", ""));
		UdpClient client = new UdpClient("localhost", 8800, 0, "zdy001");
		client.bind();
		client.startHeatbeat();
		TestUdpClient testClient = new TestUdpClient(client);
		
		// System.out.printf("fib(%d) = %s\n", 2, (testClient.fib(2)+""));
		for (int i = 0; i < 30; i++) {
			try {
				System.out.printf("fib(%d) = %s\n", i, (testClient.fib(i) + ""));
				Thread.sleep(100);
			} catch (RPCException e) {
				i--; // retry
			}
		}
		Thread.sleep(3000);

//		String strJsonObj = testClient.exp(2, 2) + "";
//		ExpResponse expResp = JSON.parseObject(strJsonObj, ExpResponse.class);
//		System.out.printf("exp2(%d) = %d cost=%dns\n", 2, expResp.getValue(), expResp.getCostInNanos());

		
//		for (int i = 0; i < 30; i++) {
//			try {
//				String strJsonObj = testClient.exp(2, i) + "";
//				ExpResponse expResp = JSON.parseObject(strJsonObj, ExpResponse.class);
//				if (expResp != null) {
//					System.out.printf("exp2(%d) = %d cost=%dns\n", i, expResp.getValue(), expResp.getCostInNanos());
//				} else {
//					System.err.println("null");
//				}
//				Thread.sleep(100);
//			} catch (RPCException e) {
//				i--; // retry
//			}
//		}
		//client.close();
	}
}
