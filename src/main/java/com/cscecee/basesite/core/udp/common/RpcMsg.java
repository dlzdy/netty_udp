package com.cscecee.basesite.core.udp.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.DecoderException;

/**
 * 通用消息， requestId string isRsp byte fromId string command string isCompressed
 * byte data byte[]
 * 
 * @author zhangdy
 *
 */
public class RpcMsg implements Cloneable {
	/**
	 * 消息号
	 */
	protected String requestId;
	/**
	 * 分片序号1,2,3
	 */
	protected int fragmentIndex = 1;
	/**
	 * 分片总数，默认1
	 */
	protected int totalFragment = 1;
	/**
	 * 请求0，响应1，
	 */
	protected Boolean isRsp=false;
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
	protected Boolean isCompressed = false;
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

	public int getFragmentIndex() {
		return fragmentIndex;
	}

	public void setFragmentIndex(int fragmentIndex) {
		this.fragmentIndex = fragmentIndex;
	}

	public int getTotalFragment() {
		return totalFragment;
	}

	public void setTotalFragment(int totalFragment) {
		this.totalFragment = totalFragment;
	}

	public ByteBuf toByteBuf( ) {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		writeStr(buf, this.getRequestId());// requestId
		buf.writeInt(this.getFragmentIndex());//fragmentIndex
		buf.writeInt(this.getTotalFragment());//totalFragment
		buf.writeBoolean(this.getIsRsp());// isRsp
		writeStr(buf, this.getFromId() );// fromId
		writeStr(buf, this.getCommand());//command
		buf.writeBoolean(this.getIsCompressed());// isCompressed
		buf.writeInt(this.getData().length);
		buf.writeBytes(this.getData());//data
		
		return buf;
	}
	
	public static RpcMsg fromByteBuf(ByteBuf in) {
		String requestId = readStr(in);//requestId
		int fragmentIndex = in.readInt();//fragmentIndex
		int totalFragment = in.readInt();//totalFragment
		Boolean isRsp =in.readBoolean();//isRsp
		String fromId = readStr(in);//fromId
		String command = readStr(in);//command
		Boolean isCompressed =in.readBoolean();//isCompressed
		int dataLen = in.readInt();
		byte[] data = new byte[dataLen];
		in.readBytes(data);
		//
		RpcMsg rpcMsg = new RpcMsg(requestId, isRsp, fromId, command, isCompressed, data);
		rpcMsg.setFragmentIndex(fragmentIndex);
		rpcMsg.setTotalFragment(totalFragment);
		return rpcMsg;
	}
	private  static String readStr(ByteBuf in) {
		int len = in.readInt();
		if (len < 0 || len > (1 << 20)) {
			throw new DecoderException("string too long len=" + len);
		}
		byte[] bytes = new byte[len];
		in.readBytes(bytes);
		return new String(bytes, Charsets.UTF8);
	}	
	private static void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}	
    public Object clone() throws CloneNotSupportedException{
        Object obj=super.clone();
//        Address a=((Person)obj).getAddress();
//        ((Person)obj).setAddress((Address) a.clone());
        return obj;

   }
}
