package com.cscecee.basesite.core.udp.common;

/**
 * 请求消息
 * @author zhangdy
 *
 */
public class RpcMsgReq extends RpcMsg{
	/**
	 * 
	 * @param requestId
	 * @param fromId
	 * @param command
	 * @param isCompressed
	 * @param data 
	 *        压缩/解压后的数据
	 */
	public RpcMsgReq(String requestId, String fromId, String command, Boolean isCompressed,
			byte[] data) {
		super(requestId, false, fromId, command, isCompressed, data);
	}

}
