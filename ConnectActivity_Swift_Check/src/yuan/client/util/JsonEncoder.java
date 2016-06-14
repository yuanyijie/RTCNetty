package yuan.client.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 该类是一个json编码工具类，使用FastJson,将Object编码成二进制再放到ByteBuf里
 * 
 * @author lenovo
 *
 */
public class JsonEncoder extends MessageToByteEncoder<Object> {

	// 四位byte数组用来存储jsonByte的长度
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

	@Override
	public void encode(ChannelHandlerContext ctx, Object object, ByteBuf out)
			throws Exception {
		int lengthPos = out.writerIndex();
		out.writeBytes(LENGTH_PLACEHOLDER);
		byte[] jsonbytes = JSON.toJSONBytes(object,
				SerializerFeature.DisableCircularReferenceDetect);
		out.writeBytes(jsonbytes);

		// 在长度开头的位置写下jsonbytes的长度
		out.setInt(lengthPos, out.writerIndex() - lengthPos - 4);
	}

}
