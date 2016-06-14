package yuan.client;

import java.util.HashMap;
import java.util.Map;

import yuan.message.SignalMessageType;
import yuan.message.SignalMsg;
import yuan.message.SignalMsgHeader;

/**
 * 这是一个构建客户端消息的工厂类
 * 
 * @author Jack Yuan
 *
 */
public class ClientMsgFactory {
	/**
	 * 构建加入房间的消息
	 * 
	 * @param roomname
	 * @return
	 */
	public static SignalMsg buildJoinMsg(String roomname) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.JOINROOM.getTypeCode());
		msg.setHeader(header);
		msg.setBody(roomname);
		return msg;
	}

	/**
	 * 构建离开房间的消息
	 * 
	 * @param roomname
	 * @return
	 */
	public static SignalMsg buildLeaveMsg(String roomname) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.LEAVEROOM.getTypeCode());
		msg.setHeader(header);
		msg.setBody(roomname);
		return msg;
	}

	/**
	 * 构建转发给房间里其他人的消息
	 * 
	 * @param roomname
	 * @param formsg
	 * @return
	 */
	public static SignalMsg buildForWardMsg(String roomname, String formsg) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setMsgType(SignalMessageType.FORWARD.getTypeCode());
		// 房间名作为扩展附件传入
		Map<String, Object> attatchment = new HashMap<String, Object>();
		attatchment.put("roomname", roomname);
		header.setAttachment(attatchment);
		msg.setHeader(header);
		msg.setBody(formsg);
		return msg;
	}

	/**
	 * 构建sdp offer消息 结构：body：sdp header.type:offer
	 * 
	 * @param offersdp
	 * @return
	 */
	public static SignalMsg buildOfferMsg(String offersdp) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		// 消息的类型为offer
		header.setMsgType(SignalMessageType.OFFER.getTypeCode());
		msg.setHeader(header);
		// 把sdp放到body里
		msg.setBody(offersdp);
		return msg;
	}

	/**
	 * 构建sdp answer消息 结构：body:sdp header.type:answer
	 * 
	 * @param answersdp
	 * @return
	 */
	public static SignalMsg buildAnswerMsg(String answersdp) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		// 消息的类型为answer
		header.setMsgType(SignalMessageType.ANSWER.getTypeCode());
		msg.setHeader(header);
		// 把sdp放到body里
		msg.setBody(answersdp);
		return msg;
	}

	public static SignalMsg buildCandidateMsg(String id, int label,
			String candidate) {
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		//消息类型设置为Candidate
		header.setMsgType(SignalMessageType.CANDIDATE.getTypeCode());
		//将candidate放到map里作为attachment
		Map<String,Object> attachment=new HashMap<String,Object>();
		attachment.put("id", id);
		attachment.put("label", label);
		attachment.put("candidate", candidate);
		header.setAttachment(attachment);
		msg.setHeader(header);
		return msg;
	}
}
