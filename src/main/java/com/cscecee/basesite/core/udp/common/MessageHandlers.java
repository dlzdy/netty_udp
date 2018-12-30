package com.cscecee.basesite.core.udp.common;

import java.util.HashMap;
import java.util.Map;

public class MessageHandlers {

	private Map<String, IMessageHandler> handlers = new HashMap<>();
	
	public void register(String command, IMessageHandler handler) {
		handlers.put(command, handler);
	}

	public IMessageHandler get(String command) {
		IMessageHandler handler = handlers.get(command);
		return handler;
	}

}
