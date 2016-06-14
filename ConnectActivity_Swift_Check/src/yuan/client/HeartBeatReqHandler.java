package yuan.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;
import yuan.message.SignalMsgHeader;

/**
 * 心跳检测的请求包
 * 
 * @author lenovo
 *
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

	// heartbeat
	private volatile ScheduledFuture<?> heartbeat;
	// 控制心跳检测是否已经启动
	private boolean isWorking = false;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		if (!isWorking) {
			// 开启一个每五秒种执行一次心跳任务的定时任务
			heartbeat = ctx.executor().scheduleWithFixedDelay(
					new HeartBeatTask(ctx), 0, 5, TimeUnit.SECONDS);
			isWorking = true;
		}
	}

	/**
	 * 处理从pipline里传递过来解码化过的消息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SignalMsg signalmsg = (SignalMsg) msg;

		if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.HBREP
						.getTypeCode()) {
//			logger.info("rev a heartbeat from server");
		}else{
			ctx.fireChannelRead(msg);
		}
	}

	/**
	 * 异常捕捉,当有异常的时候会停止心跳检测
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if(heartbeat!=null){
			heartbeat.cancel(true);
			heartbeat=null;
			isWorking=false;
		}
		ctx.fireExceptionCaught(cause);
	}

	private class HeartBeatTask implements Runnable {
		// 用于发送消息的netty上下文
		private ChannelHandlerContext ctx;
		// 心跳消息
		private SignalMsg hbmsg;

		public HeartBeatTask(ChannelHandlerContext ctx) {
			this.ctx = ctx;
			hbmsg = buildHeartBeatMsg();
		}

		@Override
		public void run() {
//			logger.info("send a heartbeat msg to server");
			ctx.writeAndFlush(hbmsg);
		}

		private SignalMsg buildHeartBeatMsg() {
			SignalMsg msg = new SignalMsg();
			SignalMsgHeader header = new SignalMsgHeader();
			header.setMsgType(SignalMessageType.HBREQ.getTypeCode());
			msg.setHeader(header);
			return msg;
		}
	}
}
