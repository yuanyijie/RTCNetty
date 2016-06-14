package yuan.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingEncoder;

/**
 * –≈¡Ó±‡¬Î∆˜
 * @author Jack Yuan
 *
 */
public class SignalMarshallingEncoder extends MarshallingEncoder{

	public SignalMarshallingEncoder(MarshallerProvider provider) {
		super(provider);
	}
	
	@Override
	public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		super.encode(ctx, msg, out);
	}

}
