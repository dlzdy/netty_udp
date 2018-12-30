package com.cscecee.basesite.core.udp.common;

import java.util.HashMap;
import java.util.Map;

public class RpcMsgUtils {

	private Map<String, RpcMsgHandler> handlers = new HashMap<>();
	
	public void register(String command, RpcMsgHandler handler) {
		handlers.put(command, handler);
	}

	public RpcMsgHandler get(String command) {
		RpcMsgHandler handler = handlers.get(command);
		return handler;
	}

}
