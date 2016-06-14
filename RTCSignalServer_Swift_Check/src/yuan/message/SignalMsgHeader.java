package yuan.message;

import java.util.HashMap;
import java.util.Map;

/**
 * The header of the Signal Message.
 * @author Jack Yuan
 *
 */
public class SignalMsgHeader {
	//判断是Signal消息头的标志
	private int crcCode=0xabef0101;
	//message's length
	private int length;
	//会话ID
	private long sessionId;
	//消息类型
	private byte msgType;
	//消息优先级
	private byte priority;
	//附件 用于扩展消息头
	private Map<String,Object> attachment=new HashMap<String,Object>();
	
	//空的构造函数
	public SignalMsgHeader(){
		
	}

	public final int getCrcCode() {
		return crcCode;
	}

	public final void setCrcCode(int crcCode) {
		this.crcCode = crcCode;
	}

	public final int getLength() {
		return length;
	}

	public final void setLength(int length) {
		this.length = length;
	}

	public final long getSessionId() {
		return sessionId;
	}

	public final void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public final byte getMsgType() {
		return msgType;
	}

	public final void setMsgType(byte msgType) {
		this.msgType = msgType;
	}

	public final byte getPriority() {
		return priority;
	}

	public final void setPriority(byte priority) {
		this.priority = priority;
	}

	public final Map<String, Object> getAttachment() {
		return attachment;
	}

	public final void setAttachment(Map<String, Object> attachment) {
		this.attachment = attachment;
	}

	@Override
	public String toString() {
		return "SignalMsgHeader [crcCode=" + crcCode + ", length=" + length
				+ ", sessionId=" + sessionId + ", msgType=" + msgType
				+ ", priority=" + priority + ", attachment=" + attachment + "]";
	}
	
	
}
