package yuan.message;

//消息类型枚举类型
public enum SignalMessageType {

	JOINROOM((byte) 1),  //加入房间的类型，服务端与客户端都能收到该类型的消息
	LEAVEROOM((byte) 2), //离开房间的类型
	FORWARD((byte) 3),   //向房间里的人其他客户端转发消息
	HBREQ((byte) 4),     //心跳请求
	HBREP((byte) 5),     //心跳回应
	OFFER((byte) 6),     //offer类型，服务端与客户端收到此类型消息时的意义是不一样的
	ANSWER((byte) 7),    //answer类型，服务端与客户端收到此类型消息时的意义也是不一样的
	CANDIDATE((byte) 8);  //candidate 类型,服务端与客户端收到此类型时的意义也是不一样的

	private byte typecode;

	private SignalMessageType(byte typecode) {
		this.typecode = typecode;
	}

	public byte getTypeCode() {
		return this.typecode;
	}
}
