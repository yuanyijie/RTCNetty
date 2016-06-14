package yuan.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;
import yuan.message.SignalMsgHeader;

public class HeartBeatRepHandler extends ChannelHandlerAdapter {
	private static Logger logger = Logger.getLogger(HeartBeatRepHandler.class
			.getName());

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SignalMsg sigmsg = (SignalMsg) msg;
		if (sigmsg != null
				&& sigmsg.getHeader() != null
				&& sigmsg.getHeader().getMsgType() == SignalMessageType.HBREQ
						.getTypeCode()) {
			// logger.info("rev heartbeatreq from client"+ctx.channel().remoteAddress());
			ctx.writeAndFlush(buildHbResp());
			// logger.info("send a heartbeat rep to client");
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	private SignalMsg buildHbResp() {
		SignalMsg hbmsg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.HBREP.getTypeCode());
		hbmsg.setHeader(header);
		return hbmsg;
	}
}
