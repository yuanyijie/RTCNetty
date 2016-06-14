package yuan.codec.signal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;
import java.util.Map;

import yuan.client.util.JsonDecoder;
import yuan.message.SignalMsg;
import yuan.message.SignalMsgHeader;

public class SignalMsgDecoder extends LengthFieldBasedFrameDecoder {
	// private SignalMarshallingDecoder decoder;
	private JsonDecoder decoder;

	/**
	 * 
	 * @param maxFrameLength
	 *            buf最大的长度
	 * @param lengthFieldOffset
	 *            buf长度值开始的位置
	 * @param lengthFieldLength
	 *            buf长度
	 * @param lengthAdjustment
	 * @param initialBytesToStrip
	 *            截取丢弃之前的
	 */
	public SignalMsgDecoder(int maxFrameLength, int lengthFieldOffset,
			int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength,
				lengthAdjustment, initialBytesToStrip);
		// decoder = MarshallingCodeCFactory.buildMarshallingDecoder();
		decoder = new JsonDecoder(1024 << 2);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {

		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if (frame == null) {
			return null;
		}
		// 构建消息和消息头,要跟编码的顺序一致
		SignalMsg msg = new SignalMsg();
		SignalMsgHeader header = new SignalMsgHeader();
		header.setCrcCode(frame.readInt());
		header.setLength(frame.readInt());
		header.setSessionId(frame.readLong());
		header.setMsgType(frame.readByte());
		header.setPriority(frame.readByte());

		// 读取附件的个数
		int attachsize = frame.readInt();
		if (attachsize > 0) {
			Map<String, Object> attachment = new HashMap<String, Object>(
					attachsize);
			int keySize = 0;
			byte[] keyArray = null;
			String key = null;
			for (int i = 0; i < attachsize; i++) {
				keySize = frame.readInt();
				keyArray = new byte[keySize];
				frame.readBytes(keyArray);
				key = new String(keyArray, "utf-8");
				attachment.put(key, decoder.decode(ctx, frame));
			}
			key = null;
			keyArray = null;
			header.setAttachment(attachment);
		}

		// ByteBuf里剩余的数据大于0，说明有body对象
		if (frame.readableBytes() > 0) {
			msg.setBody(decoder.decode(ctx, frame));
		}
		msg.setHeader(header);
		return msg;
	}

}
