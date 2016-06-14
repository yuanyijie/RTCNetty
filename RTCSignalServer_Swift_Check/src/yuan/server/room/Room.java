package yuan.server.room;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房间类
 * 
 * @author Jack Yuan
 *
 */
public class Room {

	// 房间里的成员使用Netty的Channel对象来代替
	// 成员列表
	private List<Channel> memberlist;
	// 房间名
	private String roomname = "defaultname";
	// 存储着房间所有成员的sdp信息
	// 这里使用ChannelId的shortString来作为key，netty5保证了唯一性
	private Map<String, String> allsdps;
	// 存储着房间所有成员的candidates(这样的存储结构有点担忧)
	// 这里使用ChannelId的shortString来作为key，netty5保证了唯一性
	private Map<String, List<Map<String, String>>> allcandidates;
	// 给房间的sdp加一个锁
	private Object sdplock=new Object();
	
	public Room(String roomname) {
		memberlist = new ArrayList<Channel>();
		allsdps = new HashMap<String, String>();
		allcandidates = new HashMap<String, List<Map<String, String>>>();
		this.roomname = roomname;
	}

	/**
	 * 添加对应客户端的sdp信息
	 * 
	 * @param member
	 */
	public void addSdp(Channel member, String sdp) {
		synchronized (sdplock) {
			allsdps.put(member.id().asShortText(), sdp);
			//唤醒其它阻塞的加入房间的线程
			sdplock.notifyAll();
		}
		/**
		 * 这边需要用到同步等待机制
		 */
	}

	/**
	 * 返回房间的sdp map
	 * 
	 * @return
	 */
	public Map<String, String> getSdpMap() {
		
		synchronized (sdplock) {
			
			while(memberSize()-allsdps.size()>1){
				//如果房间里的其他成员还没发送完sdp，则当前加入房间的线程一直阻塞
				try {
					sdplock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return allsdps;
			/**
			 *这边需要用到同步等待机制
			 */
		}
		
	}

	/**
	 * 对应的客户端添加一条candidate candidate 是以map的形式定义的
	 * 
	 * @param member
	 * @param candidate
	 */
	public void addCandidate(Channel member, Map<String, String> candidate) {
		// 如果成员还未在房间创建candidate list,则先创建
		List<Map<String, String>> candidatelist = allcandidates.get(member.id().asShortText());
		if (candidatelist == null) {
			candidatelist = new ArrayList<Map<String, String>>();
			allcandidates.put(member.id().asShortText(), candidatelist);
		}
		candidatelist.add(candidate);
	}

	/**
	 * 返回所有客户端的candidatelist
	 * 
	 * @return
	 */
	public Map<String, List<Map<String, String>>> getCandidatesMap() {
		return allcandidates;
	}

	/**
	 * 返回房间的名字
	 * 
	 * @return
	 */
	public String RoomName() {
		return roomname;
	}

	/**
	 * 房间增加成员
	 * 
	 * @param member
	 */
	public void addMember(Channel member) {
		// 如果房间里没有该成员则添加该成员
		if (!memberlist.contains(member))
			memberlist.add(member);
	}

	/**
	 * 房间减少成员
	 * 
	 * @param member
	 */
	public void rmvMember(Channel member) {
		memberlist.remove(member);
	}

	/**
	 * 返回房间成员的个数
	 * 
	 * @return
	 */
	public int memberSize() {
		return memberlist.size();
	}

	public List<Channel> memberList() {
		return memberlist;
	}

	/**
	 * 判断成员是否是该房间里的成员
	 * 
	 * @param tgmember
	 * @return
	 */
	public boolean hasMember(Channel tgmember) {
		boolean isIn = false;
		for (Channel member : memberlist) {
			// 如果该房间有该成员
			if (tgmember == member) {
				isIn = true;
				break;
			}
		}
		return isIn;
	}

	/**
	 * 在成员离开房间时，需要移除掉成员信息
	 * 
	 * @param member
	 */
	public void removeInfo(Channel member) {
		String memberid = member.id().asShortText();
		synchronized (sdplock) {
			allsdps.remove(memberid);
		}
		allcandidates.remove(memberid);
	}
}
