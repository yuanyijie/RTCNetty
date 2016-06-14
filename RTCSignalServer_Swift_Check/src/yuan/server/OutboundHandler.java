package yuan.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import yuan.server.room.Room;
import yuan.server.room.RoomControl;

/**
 * 检测处理客户端从服务端断开的情况，该handler必须放在pipline的首部
 * 
 * @author Jack Yuan
 *
 */
public class OutboundHandler extends ChannelHandlerAdapter {
	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		System.out.println("test客户端 " + ctx.channel().remoteAddress()
				+ " 与服务端断开连接");
		// 客户端因为异常从服务端退出时，会触发这里的回调函数，这里需要客户端再服务器上对应的逻辑或者资源消除掉

		Channel currentmember = ctx.channel();
		RoomControl control = new RoomControl();
		// 判断当前上下文里的成员是否存在于一个房间里
		Room room = control.isInaRoom(currentmember);

		if (room != null) {
			// 如果存在于一个房间里,则离开这个房间
			control.leaveRoom(room, currentmember);
		}

	}
}
