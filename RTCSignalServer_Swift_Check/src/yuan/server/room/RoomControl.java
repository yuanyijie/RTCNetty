package yuan.server.room;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;
import yuan.server.ServerConfig;

/**
 * 房间综合控制类 单例模式 改用数据库存储房间的话就不需要单例模式(好像目前不需要使用单例模式)
 * 
 * @author Jack Yuan
 *
 */
public class RoomControl {
	// 存储服务器里所有的房间
	private static Map<String, Room> roommap = new HashMap<String, Room>();
	// 控制房间容器的同步对象锁
	private static Object maplock = new Object();

	private static Logger logger = Logger
			.getLogger(RoomControl.class.getName());

	private static int MAXMEMBER = ServerConfig.MAXMEMBER;

	/**
	 * 私有方法，添加房间
	 * 
	 * @param roomname
	 * @param room
	 */
	private void addRoom(String roomname, Room room) {
		synchronized (maplock) {
			roommap.put(roomname, room);
		}

	}

	/**
	 * 私有方法，移除房间
	 * 
	 * @param roomname
	 */
	private void removeRoom(String roomname) {
		synchronized (maplock) {
			roommap.remove(roomname);
		}

	}

	/**
	 * 构造函数
	 */
	public RoomControl() {
		// roommap = new HashMap<String, Room>();
	}

	/**
	 * 加入房间,并且返回加入房间后，房间的人数
	 * 
	 * @param roomname
	 *            member
	 */
	public int joinRoom(String roomname, Channel member) {
		int size = 0;
		// 根据房间名创建同步方法块
		synchronized (roomname) {
			// 如果房间已经被创建
			if (roommap.containsKey(roomname)) {
				size = roommap.get(roomname).memberSize();
				if (size < MAXMEMBER && size > 0) {
					// 如果房间里有成员，且房间成员小于上限
					roommap.get(roomname).addMember(member);
					size++;
				} else {
					// 如果房间成员已经满了或者其他，则返回一个-1
					size = -1;
				}
			} else {
				// 创建房间添加成员
				Room newRoom = new Room(roomname);
				newRoom.addMember(member);
				addRoom(roomname, newRoom);
				size = 1;
			}
		}
		logger.info("Client " + member.remoteAddress() + " join room "
				+ roomname + " current size " + size);
		return size;
	}

	/**
	 * 离开房间，并且返回离开房间后，房间的人数
	 * 
	 * @param roomname
	 * @param member
	 * @return
	 */
	public int leaveRoom(String roomname, Channel member) {
		int size = 0;
		// 根据房间名创建同步方法块
		synchronized (roomname) {
			if (!roommap.containsKey(roomname))
				return 0;
			Room currentRoom = roommap.get(roomname);
			// 从房间里移除成员
			currentRoom.rmvMember(member);
			/**
			 * 成员离开时，移除掉留在房间的sdp和candidatelist
			 */
			currentRoom.removeInfo(member);
			// 关闭member端口，不知道效用
			member.close();
			size = currentRoom.memberSize();
			// 最后一个成员离开房间时将移除房间
			if (size == 0) {
				removeRoom(roomname);
			}
			forWardMsg(ServerMsgFactory.buildLeaveFor(member), roomname, member);
		}
		logger.info("Client " + member.remoteAddress() + " leave room "
				+ roomname + " current size " + size);
		// 将member置为null，好让gc回收
		member = null;
		return size;
	}

	/**
	 * 离开房间的重载函数，并且返回离开房间后，房间的人数
	 * 
	 * @param room
	 * @param member
	 * @return
	 */
	public int leaveRoom(Room room, Channel member) {
		String roomname = room.RoomName();
		// 直接调用函数参数是roomname的重载函数
		return this.leaveRoom(roomname, member);
	}

	/**
	 * 判断某个channel是否在存在一个于一个房间内 如果存在则返回房间对象，不存在则返回null
	 * 
	 * @param channel
	 * @return
	 */
	public Room isInaRoom(Channel channel) {
		Room ownroom = null;
		for (Map.Entry<String, Room> room : roommap.entrySet()) {
			if (room.getValue().hasMember(channel)) {
				ownroom = room.getValue();
				break;
			}
		}
		return ownroom;
	}

	/**
	 * 将客户端发过来的msg转发给房间里的其它客户端
	 * 
	 * @param msg
	 */
	public void forWardMsg(SignalMsg msg, String roomname, Channel imember) {
		// 取到当前客户端所在房间
		Room tgroom = roommap.get(roomname);
		if (tgroom == null) {
			// 如果房间不存在了
			return;
		}
		List<Channel> memberlist = tgroom.memberList();
		for (Channel smember : memberlist) {
			// 如果
			if (smember != imember) {
				smember.writeAndFlush(msg);
			}
		}
	}

	/**
	 * 给客户端发送一个加入房间成功的回应
	 * 
	 * @param membernum
	 * @param channel
	 */
	public void sendJoinSuccessResponse(int membernum, Channel channel) {
		// 首先需要得到channel对应的room对象
		Room room = isInaRoom(channel);
		// 构建回应消息
		SignalMsg responsemsg = ServerMsgFactory.buildJoinResponse(membernum,
				room.getSdpMap(), room.getCandidatesMap());
		System.out.println("房间的candidate："+room.getCandidatesMap());
		// 向原有信道写入回应消息
		channel.writeAndFlush(responsemsg);
		System.out.println("发送一条加入回应给：" + channel.remoteAddress());
	}
	/**
	 * 给客户端发送一个加入房间失败的回应
	 * @param membernum
	 */
	public void sendJoinFailResponse(int membernum,Channel channel){
		//构建回应消息
		SignalMsg responsemsg=ServerMsgFactory.buildJoinFailResponse(membernum);
		channel.writeAndFlush(responsemsg);
	}
	/**
	 * 将客户端发送过来的sdp保存在对应房间的容器里,如果是则answer转发给其它客户端
	 * 
	 * @param sdp
	 * @param channel
	 */
	public void saveRoomSdpAndForWard(SignalMsg msg, Channel channel) {
		Room room = isInaRoom(channel);
		// 这边没有考虑到异常的情况
		room.addSdp(channel, (String) msg.getBody());
		logger.info("添加了一条来自" + channel.remoteAddress() + "的sdp");
		if (room.memberSize() > 1) {
			// 如果此时房间成员大于1，都要向之前的房间转发sdp answer
			// 转发之前先添加一下channelID
			addChannelId(msg, channel);
			forWardMsg(msg, room.RoomName(), channel);
		}
	}

	/**
	 * 将客户端发送过来的candidate保存在对应房间的容器里,如果不是第一个进入房间的人则转发给其它客户端
	 * 
	 * @param msg
	 * @param channel
	 */
	public void saveRoomCandidateAndForWard(SignalMsg msg, Channel channel) {
		Room room = isInaRoom(channel);
		Map<String, String> candidate = new HashMap<String, String>();
		candidate.put("id", (String) msg.getHeader().getAttachment().get("id"));
		candidate.put("label", msg.getHeader().getAttachment().get("label")
				.toString());
		candidate.put("candidate", (String) msg.getHeader().getAttachment()
				.get("candidate"));
		room.addCandidate(channel, candidate);
		logger.info("保存了一条来自" + channel.remoteAddress() + "的candidate"+candidate);
		if (room.memberSize() > 1) {
			// 转发之前先添加一下candidate
			addChannelId(msg, channel);
			forWardMsg(msg, room.RoomName(), channel);
		}
	}

	/**
	 * 内部方法，用于在向其它客户端转发sdp和candidate时加入服务端-客户端这的channelid，以区别
	 * 
	 * @param msg
	 * @param channel
	 *            sdp的话会放在attatchment里，candidate会放在body里
	 */
	private void addChannelId(SignalMsg msg, Channel channel) {
		String channelid = channel.id().asShortText();
		if (msg.getHeader().getMsgType() == SignalMessageType.ANSWER
				.getTypeCode()) {
			// 如果该消息是转发的answer
			msg.getHeader().getAttachment().put("channelid", channelid);
		} else if (msg.getHeader().getMsgType() == SignalMessageType.CANDIDATE
				.getTypeCode()) {
			// 如果该消息时转发的candidate
			msg.setBody(channelid);

		} else {
			return;
		}
	}
}
