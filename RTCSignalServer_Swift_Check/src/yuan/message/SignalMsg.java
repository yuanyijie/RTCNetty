package yuan.message;
/**
 * 信令消息结构体
 * @author Jack Yuan
 *
 */
public class SignalMsg {
	//消息头
	private SignalMsgHeader header;
	//消息体
	private Object body;
	
	//构造函数
	public SignalMsg(){
		
	}

	public final SignalMsgHeader getHeader() {
		return header;
	}

	public final void setHeader(SignalMsgHeader header) {
		this.header = header;
	}

	public final Object getBody() {
		return body;
	}

	public final void setBody(Object body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "SignalMsg [header=" + header + ", body=" + body + "]";
	}
	
	
}
