package com.cscecee.basesite.core.udp.common;

/**
 * 请求消息
 * @author zhangdy
 *
 */
public class MessageReq extends MessageCommon{

	public MessageReq(String requestId, String fromId, String command, Boolean isCompressed,
			byte[] payload) {
		super(requestId, false, fromId, command, isCompressed, payload);
	}

}
