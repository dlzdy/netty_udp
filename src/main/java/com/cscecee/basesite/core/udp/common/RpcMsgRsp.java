package com.cscecee.basesite.core.udp.common;

/**
 * 响应消息
 * @author zhangdy
 *
 */
public class RpcMsgRsp extends RpcMsg{

	public RpcMsgRsp(String requestId, String fromId, String command, Boolean isCompressed,
			byte[] data) {
		super(requestId, true, fromId, command, isCompressed, data);
	}
}
