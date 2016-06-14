package yuan.server.room;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;
import yuan.message.SignalMsgHeader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class ServerMsgFactory {

	/**
	 * 构建一个用于转发给其它客户端的文本消息(主要就是将handler那边的来的msg加一个remoteaddress)
	 * 
	 * @param message
	 * @param channel
	 * @return
	 */
	public static SignalMsg buildTextMsg(SignalMsg message, Channel channel) {
		message.getHeader().getAttachment()
				.put("sourceaddress", channel.remoteAddress().toString());
		return message;
	}

	/**
	 * 构建一条对于客户端加入房间的响应消息 消息体位当前房间的人数0,1,2 附件一，附件二，分别为房间成员的sdp,candidatelist
	 * 
	 * @return
	 */
	public static SignalMsg buildJoinResponse(int membernum,
			Map<String, String> sdp,
			Map<String, List<Map<String, String>>> candidatelist) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.JOINROOM.getTypeCode());
		/**
		 * 集合对象并不能直接使用jbossmarshalling编解码，需要遍历编解码 工作量比较大，所以这边复杂结构的附件解析成json
		 * 这边使用fastjson 据说Android客户端不同版本
		 */
		// 将sdpmap 转化成jsonstring
		String sdpString = JSON.toJSONString(sdp);
		// 将candidate转化成jsontring,后面的参数可以避免ref错误
		String candidateString = JSON.toJSONString(candidatelist,
				SerializerFeature.DisableCircularReferenceDetect);
		Map<String, Object> attatchment = new HashMap<String, Object>();
		// 添加sdp附件
		attatchment.put("offer", sdpString);
		// 添加candidatelist附件
		attatchment.put("candidatelist", candidateString);
		header.setAttachment(attatchment);
		msg.setHeader(header);
		msg.setBody(membernum);
		return msg;
	}
	/**
	 * 构建一个加入房间失败的消息
	 * @param membernum
	 * @return
	 */
	public static SignalMsg buildJoinFailResponse(int membernum){
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.JOINROOM.getTypeCode());
		msg.setHeader(header);
		msg.setBody(membernum);
		return msg;
	}
	/**
	 * 构建一条离开的消息，用于发送给房间里的其它客户端
	 * body存放离开客户端的ID
	 * @param member
	 * @return
	 */
	public static SignalMsg buildLeaveFor(Channel member) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.LEAVEROOM.getTypeCode());
		msg.setHeader(header);
		msg.setBody(member.id().asShortText());
		return msg;
	}

	/**
	 * 这边使用main方法来测试一下
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("1", "string1");
		map.put("2", "string2");
		map.put("3", "string3");
		Map<String, List<Map<String, String>>> mapf = new HashMap<String, List<Map<String, String>>>();
		for (int i = 0; i < 3; i++) {
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			list.add(map);
			mapf.put("list" + i, list);
		}
		String jsonString = JSON.toJSONString(mapf,
				SerializerFeature.DisableCircularReferenceDetect);
		System.out.println(jsonString);
		
	}
}
