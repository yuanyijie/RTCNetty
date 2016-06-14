package yuan.client.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

public class JsonDecoder extends LengthFieldBasedFrameDecoder {

	public JsonDecoder(int maxObjectsize) {
		//这样配置的原因
		super(maxObjectsize, 0, 4, 0, 4);
	}

	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		// 调用父类解码器，frame是完成的obj的byte[]
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if (frame == null) {
			return null;
		}
		// frame中可读的byte数
		int size = frame.readableBytes();
		// 创建byte数组
		byte[] jsonbytes = new byte[size];
		// 将byte读取到数组中
		frame.readBytes(jsonbytes);
		Object obj = JSON.parse(jsonbytes,
				Feature.DisableCircularReferenceDetect);
		return obj;
	}
}
