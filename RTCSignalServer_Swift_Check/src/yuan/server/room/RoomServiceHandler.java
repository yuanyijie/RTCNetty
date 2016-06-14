package yuan.server.room;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;

/**
 * 
 * @author Jack Yuans
 *
 */
public class RoomServiceHandler extends ChannelHandlerAdapter {
	// 服务器的房间控制
	private static RoomControl control = new RoomControl();

	// 复杂的业务放到业务线程池中
	// 新建一个自适应的线程池
	private static ExecutorService executors = Executors.newCachedThreadPool();

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		final SignalMsg signalmsg = (SignalMsg) msg;
		// 根据不同消息的类型进行不同的操作
		// 这边不适合用switch语句,用if else
		// 如果是客户端请求加入房间的信令
		if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.JOINROOM
						.getTypeCode()) {
			executors.execute(new Runnable() {
				// 加入房间的时候需要一个延时等待的操作
				@Override
				public void run() {
					// 房间名从message 的 body中取
					String roomname = (String) signalmsg.getBody();
					// 注意这边membernum是成员加入房间之后的成员数
					int membernum = control.joinRoom(roomname, ctx.channel());
					if (membernum == -1) {
						// -1代表加入房间失败
						control.sendJoinFailResponse(membernum, ctx.channel());
					} else
						control.sendJoinSuccessResponse(membernum,
								ctx.channel());
				}
			});

		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.LEAVEROOM
						.getTypeCode()) {
			// 正常的用户主动离开房间
			String roomname = (String) signalmsg.getBody();
			control.leaveRoom(roomname, ctx.channel());
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.FORWARD
						.getTypeCode()) {
			// 普通的转发消息给房间内其他成员
			Map<String, Object> attatchment = signalmsg.getHeader()
					.getAttachment();
			String roomname = (String) attatchment.get("roomname");
			control.forWardMsg(
					ServerMsgFactory.buildTextMsg(signalmsg, ctx.channel()),
					roomname, ctx.channel());
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.OFFER
						.getTypeCode()) {
			// 客户端向服务端发送offer消息
			control.saveRoomSdpAndForWard(signalmsg, ctx.channel());
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.ANSWER
						.getTypeCode()) {
			// 客户端向服务端发送answer消息
			control.saveRoomSdpAndForWard(signalmsg, ctx.channel());
		} else if (signalmsg.getHeader() != null
				&& signalmsg.getHeader().getMsgType() == SignalMessageType.CANDIDATE
						.getTypeCode()) {
			// 客户端向服务端发送candidate
			// 服务端保存客户端传递过来的数据并且
			control.saveRoomCandidateAndForWard(signalmsg, ctx.channel());
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	/**
	 * 成员的加入和离开都应该向客户端们发送提示，以边让客户端动态地添加peerconnection和渲染器
	 */

}
