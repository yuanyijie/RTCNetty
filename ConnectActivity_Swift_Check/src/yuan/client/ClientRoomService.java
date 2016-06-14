package yuan.client;

import android.util.Log;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 这个类定义了一些客户端房间业务的服务 如果应用于Android的话，需要开一个线程池来处理（因为会涉及到网络的访问）
 * 
 * @author Jack Yuan
 *
 */
public class ClientRoomService {
	// 属性需要传递一个channel以向服务端发送消息
	private Channel channel;

	public ClientRoomService(Channel channel) {
		this.channel = channel;
	}

	/**
	 * 加入房间
	 */
	public void joinRoom(String roomname) {
		channel.writeAndFlush(ClientMsgFactory.buildJoinMsg(roomname));
	}

	/**
	 * 离开房间
	 */
	public void leaveRoom(String roomname) {
		//在发完主动离开的消息之后，才会选择关闭真正离开
		channel.writeAndFlush(ClientMsgFactory.buildLeaveMsg(roomname))
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 将消息转发给房间里的其他人
	 * 
	 * @param roomname
	 * @param msg
	 */
	public void forWardMsg(String roomname, String msg) {
		channel.writeAndFlush(ClientMsgFactory.buildForWardMsg(roomname, msg));
	}

	/**
	 * 给服务端发送offer
	 * 
	 * @param sdp
	 */
	public void sendOffSdp(String sdp) {
		channel.writeAndFlush(ClientMsgFactory.buildOfferMsg(sdp));
	}

	/**
	 * 给服务端发送answer
	 * 
	 * @param sdp
	 */
	public void sendAnswerSdp(String sdp) {
		channel.writeAndFlush(ClientMsgFactory.buildAnswerMsg(sdp));
	}

	/**
	 * 给服务端发送candidate
	 * 
	 * @param id
	 * @param label
	 * @param candidate
	 */
	public void sendCandidate(String id, int label, String candidate) {
		Log.i("tesst", "sendcan"+candidate);
		channel.writeAndFlush(ClientMsgFactory.buildCandidateMsg(id, label,
				candidate));
	}

}
