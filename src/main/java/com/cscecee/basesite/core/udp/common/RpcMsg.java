package com.cscecee.basesite.core.udp.common;

/**
 * 通用消息， requestId string isRsp byte fromId string command string isCompressed
 * byte data byte[]
 * 
 * @author zhangdy
 *
 */
public class RpcMsg {
	/**
	 * 消息号
	 */
	protected String requestId;
	/**
	 * 请求0，响应1，
	 */
	protected Boolean isRsp;
	/**
	 * 消息来源,server端=0，client=appid
	 */
	protected String fromId;
	/**
	 * 消息功能
	 */
	protected String command;
	/**
	 * 非压缩0，压缩1
	 */
	protected Boolean isCompressed;
	/**
	 * 消息净荷
	 */
	protected byte[] data;

	public RpcMsg(String requestId, Boolean isRsp, String fromId, String command, Boolean isCompressed, byte[] data) {
		this.requestId = requestId;
		this.isRsp = isRsp;
		this.fromId = fromId;
		this.command = command;
		this.isCompressed = isCompressed;
		this.data = data;
		
//		if (isRsp) {// 响应，压缩标记->解压，
//			if (isCompressed) {
//				try {
//					this.data = GzipUtils.ungzip(data);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}else {// 請求的
//			if (isCompressed) {// 压缩标记->压缩
//				try {
//					this.data = GzipUtils.gzip(data);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		}
	}

	public Boolean getIsRsp() {
		return isRsp;
	}

	public void setIsRsp(Boolean isRsp) {
		this.isRsp = isRsp;
	}

	public Boolean getIsCompressed() {
		return isCompressed;
	}

	public void setIsCompressed(Boolean isCompressed) {
		this.isCompressed = isCompressed;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
