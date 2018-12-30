package com.cscecee.basesite.core.udp.common;

/**
 * 响应消息
 * @author zhangdy
 *
 */
public class MessageRsp extends MessageCommon{

	public MessageRsp(String requestId, String fromId, String command, Boolean isCompressed,
			byte[] data) {
		super(requestId, false, fromId, command, isCompressed, data);
	}
}
