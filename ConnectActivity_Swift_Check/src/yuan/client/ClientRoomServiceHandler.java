package yuan.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

import yuan.client.SignalClient.SignalEvents;
import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;

/**
 * 该类是房间的
 * 
 * @author Jack Yuan
 *
 */
public class ClientRoomServiceHandler extends ChannelInboundHandlerAdapter {
	// 用于回调给调用该client的类
	private SignalEvents events;

	public ClientRoomServiceHandler(SignalEvents events) {
		super();
		this.events = events;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SignalMsg signalmsg = (SignalMsg) msg;
		if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.FORWARD
						.getTypeCode()) {
			// 如果消息头不为空,并且消息类型为ForWard则为其他client转发过来的消息
			// 现在没有放到业务线程池，或者回调出去
			Map<String, Object> attatchment = signalmsg.getHeader()
					.getAttachment();
			String remoteclient = (String) attatchment.get("sourceaddress");
			System.out.println(remoteclient + ":" + signalmsg.getBody());
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.JOINROOM
						.getTypeCode()) {
			// 如果是服务器对于加入房间的回应
			// 回应的消息体会是1或者2,1表示是发起者，2表示是第二位进入房间的人，其他数字表示进入房间失败
			events.joinRoomResult(signalmsg);
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.ANSWER
						.getTypeCode()) {
			// 服务器将另外的客户端的answer sdp转发过来
			events.forWardAnswer(signalmsg);

		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.CANDIDATE
						.getTypeCode()) {
			// 服务器将另外的客户端的candidate 转发过来
			events.forWardCandidate(signalmsg);
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.LEAVEROOM
						.getTypeCode()) {
			// 客户端收到服务端发送的其它客户端转发的离开信令
			events.leaveRoomResult(signalmsg);
		} else {
			ctx.fireChannelRead(msg);
		}
	}
}
