package com.cscecee.basesite.core.udp.common;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;


/**
 * 处理发送消息，接收消息
 */
public class UdpChannelHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final static Logger logger = LoggerFactory.getLogger(UdpChannelHandler.class);
	
	// 业务线程池
	private ThreadPoolExecutor executor;
	
	private UdpEndPoint udpEndPoint;
	// 记录udp客户端信息, <formId, <ip,port>>
	private ConcurrentMap<String, Map<String, String>> peersMap = new ConcurrentHashMap<>();
	
	// 记录异步请求任务, <reqId, RpcFuture>
	private ConcurrentMap<Long, RpcFuture> pendingSendTasks = new ConcurrentHashMap<>();
	// 记录请求消息,<reqId_index, RpcMsg>>
	private ConcurrentMap<String, HasSendUdpMsg> hasSendMap = new ConcurrentHashMap<>();

	// 记录接收udp多包信息,<reqId,<index, RpcMsg>>
	private ConcurrentMap<Long, Map<Integer, RpcMsg>> assemPkgMap = new ConcurrentHashMap<>();
	// 记录接收udp多包信息,<reqId, time>
	private ConcurrentMap<Long, Long> assemPkgTimeMap = new ConcurrentHashMap<>();

	private Throwable ConnectionClosed = new Exception("rpc connection not active error");
	
	private Throwable NoResponse = new Exception("no response");
	
	public UdpChannelHandler(UdpEndPoint udpEndPoint, int workerThreads) {
		// 业务队列最大1000,避免堆积
		// 如果子线程处理不过来,io线程也会加入业务逻辑(callerRunsPolicy)
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
		// 给业务线程命名
		ThreadFactory factory = new ThreadFactory() {
			AtomicInteger seq = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("rpc-" + seq.getAndIncrement());
				logger.info("new rpc thread:" + t.getName());
				return t;
			}
		};
		// 闲置时间超过30秒的线程就自动销毁
		this.executor = new ThreadPoolExecutor(1, workerThreads, 30, TimeUnit.SECONDS, queue, factory,
				new CallerRunsPolicy());
		this.udpEndPoint = udpEndPoint;
		
		//检查发送超时，重发线程
		new Thread("resend-thread") {
			public void run() {
				while(true) {
					List<String> removeList = new ArrayList<>();//删除
					List<String> reSendList = new ArrayList<>();//重发
					try {
						for (String  key: hasSendMap.keySet()) {
							HasSendUdpMsg hasSendUdpMsg = hasSendMap.get(key);
							if(hasSendUdpMsg.getSendCount() > 3) {//发送3次失败，需要删除
								removeList.add(key);
							}else if(System.currentTimeMillis() - hasSendUdpMsg.getLastSendtime() > 3000) {//3秒还没响应，需要重发
								reSendList.add(key);
							}
						}
						// 删除
						for (String key : removeList) {
							//从已发送队列中删除
							hasSendMap.remove(key);
							//从已发送任务中删除，返回无响应错误信息
							String reqId = key.substring(0, key.indexOf("_"));
							RpcFuture future = (RpcFuture) pendingSendTasks.remove(reqId);
							if (future != null) {
								future.fail(NoResponse);								
							}
						}
						// 重发
						for (String key : reSendList) {
							HasSendUdpMsg hasSendUdpMsg = hasSendMap.get(key);
							hasSendUdpMsg.setSendCount(hasSendUdpMsg.getSendCount() + 1);
							hasSendUdpMsg.setLastSendtime(System.currentTimeMillis());
							getChannel().writeAndFlush(hasSendUdpMsg.getUpdPkg());
							TimeUnit.MILLISECONDS.sleep(50);//间隔
						}

					} catch (Exception e) {
						logger.error("failed", e);
					}finally {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
					}
				}
			}
		}.start();
		
		//检查接收超时线程
		new Thread("checkRecieve-thread") {
			public void run() {
				while(true) {
					List<Long> removeList = new ArrayList<>();//删除
					try {
						for (Long  lastUpdateTime: assemPkgTimeMap.keySet()) {
								if(System.currentTimeMillis() - lastUpdateTime > 30000) {//距离最后一个分组，30秒还没响应，删除
								removeList.add(lastUpdateTime);
							}
						}
						// 删除
						for (Long reqId : removeList) {
							assemPkgTimeMap.remove(reqId);
							assemPkgMap.remove(reqId);
						}

					} catch (Exception e) {
						logger.error("failed", e);
					}finally {
						try {
							Thread.sleep(5000);
						} catch (Exception e) {
						}
					}
				}
			}
		}.start();
		
		
	}

	public Channel getChannel() {
		return udpEndPoint.getChannel();
	}

	public void closeGracefully() {
		// 优雅一点关闭,先通知,再等待,最后强制关闭
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		this.executor.shutdownNow();
	}

	/**
	 * 处理接收到的消息, 响应的和请求的
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
		try {
			InetSocketAddress sender = datagramPacket.sender();
			ByteBuf in = datagramPacket.content();
			RpcMsg messageInput = RpcMsg.fromByteBuf(in);

			// 保存对端ip地址，用于服务器端->client
			Map<String, String> clientMap = peersMap.get(messageInput.getFromId());
			if (clientMap == null || clientMap.isEmpty()) {
				clientMap = new HashMap<>();
				peersMap.put(messageInput.getFromId(), clientMap);//放入缓存，记录对端的ip，port
			}
			clientMap.put("ip", sender.getAddress().getHostAddress());
			clientMap.put("port", sender.getPort() + "");
			clientMap.put("time", System.currentTimeMillis() + "");
			
			// 用业务线程处理消息
			this.executor.execute(() -> {
				// 处理压缩
				byte[] tmpData = messageInput.getData(); 
				if (messageInput.isCompressed()) {//进行解压
					try {
						tmpData = GzipUtils.ungzip(tmpData);
						messageInput.setData(tmpData);
						messageInput.setCompressed(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				switch (messageInput.getDirection()) {
				case 0://req
					
					break;
				case 1://rsp
					
					break;
				case 2://ack
					String ackKey = messageInput.getReqId() + "_" + messageInput.getFragmentIndex();
					logger.info("ack reqId=" + ackKey);
					hasSendMap.remove(ackKey);
					break;

				default:
					break;
				}
				
				//req , rsp
				if (messageInput.getTotalFragment() == 1 ) {//只有一个包，不需要组包
					dealRpcMsg(ctx, sender, messageInput);//
				}else if(messageInput.getTotalFragment() > 1) {//分包发送的，需要重新组包
					logger.info("recieve fragment reqId=" +  messageInput.getReqId()  + "  |  "+ (messageInput.getFragmentIndex()+1) + "/" + messageInput.getTotalFragment());
					Map<Integer, RpcMsg> fragementMap = assemPkgMap.get(messageInput.getReqId());
					if (fragementMap == null || fragementMap.isEmpty()) {
						fragementMap = new HashMap<>();
						assemPkgMap.put(messageInput.getReqId(), fragementMap);
					}
					fragementMap.put(messageInput.getFragmentIndex(), messageInput);
					assemPkgTimeMap.put(messageInput.getReqId(), System.currentTimeMillis());//记录时间
					//所有分组收全了
					logger.info("fragementMap.size()=" + fragementMap.size());
					logger.info("messageInput.getTotalFragment()=" + messageInput.getTotalFragment());
					if (fragementMap.size() == messageInput.getTotalFragment()) {//收全了
						byte[] totalBytes = fragementMap.get(0).getData();//第一个
						for (int i = 1; i < fragementMap.size(); i++) {
							totalBytes = byteMergerAll(totalBytes, fragementMap.get(i).getData());
						}
						messageInput.setData(totalBytes);//设置全部data
						logger.info("recieve total fragment " + fragementMap.size());
						assemPkgMap.remove(messageInput.getReqId());//删除缓存
						assemPkgTimeMap.remove(messageInput.getReqId());//记录时间
						dealRpcMsg(ctx, sender, messageInput);
					}
					
				}
				//发ack消息 ,2
				RpcMsg ackMsg = new RpcMsg(messageInput.getReqId(), 2, "0", "ack", false, new byte[0]);
				ctx.writeAndFlush(new DatagramPacket(ackMsg.toByteBuf(), sender));
			});

		} catch (Exception e) {
			logger.error("failed", e);
		}
	}

	private void dealRpcMsg(ChannelHandlerContext ctx, InetSocketAddress sender, RpcMsg messageInput) {
		if (messageInput.getDirection() == 1) {//rsp消息
			RpcFuture future = (RpcFuture) pendingSendTasks.remove(messageInput.getReqId());
			if (future == null) {
				logger.error("future is null");
				return;
			}
			future.success(messageInput.getData());					
		}else if(messageInput.getDirection() == 0){// 0请求
			this.handleMessage(ctx, sender, messageInput);
		}
	}
	/**
	 * 合并多个byte数组
	 * @param values
	 * @return
	 */
	private byte[] byteMergerAll(byte[]... values) {
		int length_byte = 0;
		for (int i = 0; i < values.length; i++) {
			length_byte += values[i].length;
		}
		byte[] all_byte = new byte[length_byte];
		int countLength = 0;
		for (int i = 0; i < values.length; i++) {
			byte[] b = values[i];
			System.arraycopy(b, 0, all_byte, countLength, b.length);
			countLength += b.length;
		}
		return all_byte;
	}

	/**
	 * 
	 * @param ctx
	 * @param sender
	 * @param messageInput
	 * 未压缩数据包
	 */
	private void handleMessage(ChannelHandlerContext ctx, InetSocketAddress sender, RpcMsg messageInput) {
		// 业务逻辑在这里
		RpcMsgHandler handler = udpEndPoint.getHandlers().get(messageInput.getCommand());
		if (handler != null) {
			handler.handle(ctx, sender, messageInput.getReqId(),  messageInput.getData());
		} else {
			logger.error("not found handler of " + messageInput.getCommand());
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		logger.error(cause.getMessage(), cause);
	}

	/**
	 * 发送消息,服务器端-->客户端
	 * 因为客户端可能是局域网，不能指定ip
	 * @param peerId
	 * @param msgReq
	 * @return
	 */
	public RpcFuture send(String peerId, RpcMsg msgReq) {
		RpcFuture future = new RpcFuture();
		Map<String, String> peerMap = peersMap.get(peerId);
		if (peerMap == null || peerMap.isEmpty()) {
			future.fail(ConnectionClosed);
			return future;
		}
		String ip = peerMap.get("ip");
		int port = Integer.valueOf(peerMap.get("port"));
		long time = Long.valueOf(peerMap.get("time"));
		if (System.currentTimeMillis() - time > 30000 ) {
			future.fail(ConnectionClosed);
			return future;
		}
		InetSocketAddress remoteSocketAddress = new InetSocketAddress(ip, port);
		return send(remoteSocketAddress, msgReq);
	}
	/**
	 * 通用
	 * 发送消息,客户端-->服务器端
	 * https://blog.csdn.net/yiyefangzhou24/article/details/63704942
	 * @param msgReq
	 * @return
	 */
	public  RpcFuture send(InetSocketAddress remoteSocketAddress, RpcMsg msgReq) {
		RpcFuture future = new RpcFuture();
		if (getChannel() != null) {
			getChannel().eventLoop().execute(() -> {
				int fragmentSize = RpcMsg.FRAGMENT_SIZE;
				pendingSendTasks.put(msgReq.getReqId(), future);
				logger.info("send req " + msgReq.getCommand() +  " >>>>>  " + remoteSocketAddress);
				byte[] orignalData =  msgReq.getData();
				int orignalDataLen = orignalData.length;
				if (msgReq.getTotalFragment() == 1) {//未超长
					logger.info("send fragment len : " + orignalDataLen + " bytes | " + (1) +  "/" + msgReq.getTotalFragment());
					/**** send *****/
					DatagramPacket updPkg= new DatagramPacket(msgReq.toByteBuf(), remoteSocketAddress);
					HasSendUdpMsg hasSendUdpMsg = new HasSendUdpMsg(); 
					hasSendUdpMsg.setUpdPkg(updPkg);
					hasSendMap.put(msgReq.getReqId()+ "_" + msgReq.getFragmentIndex(), hasSendUdpMsg);
					getChannel().writeAndFlush(updPkg);
				} else {//超长
					int fragmentTotal = msgReq.getTotalFragment() ;
					for(int i=0 ; i<fragmentTotal  ;i++) {//循环发包
						try {
							int nextDataLen = fragmentSize;//下一帧数据长度
							if (i == fragmentTotal - 1 ) {//最后一帧
								nextDataLen = orignalDataLen - i * fragmentSize;
							}
							RpcMsg tempMsgReq =(RpcMsg)msgReq.clone();
							byte[] tempData = new byte[nextDataLen];
							System.arraycopy(orignalData, i * fragmentSize, tempData, 0 , nextDataLen);
							tempMsgReq.setFragmentIndex(i);//帧序号，从0开始
							tempMsgReq.setData(tempData);
							logger.info("send fragment len : " + nextDataLen + " bytes | " + (tempMsgReq.getFragmentIndex()+1) +  "/" + tempMsgReq.getTotalFragment());
							/**** send *****/
							DatagramPacket updPkg= new DatagramPacket(tempMsgReq.toByteBuf(), remoteSocketAddress);
							HasSendUdpMsg hasSendUdpMsg = new HasSendUdpMsg(); 
							hasSendUdpMsg.setUpdPkg(updPkg);
							hasSendMap.put(tempMsgReq.getReqId()+ "_" + tempMsgReq.getFragmentIndex(), hasSendUdpMsg);							
							getChannel().writeAndFlush(updPkg);							
							
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							//设置发送间隔，防止太快
							try {
								TimeUnit.MILLISECONDS.sleep(1 +(new Random()).nextInt(10));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}					
						
					}
				}

				
				
				
			});
		} else {
			future.fail(ConnectionClosed);
		}		
		return future;
	}
		
}
