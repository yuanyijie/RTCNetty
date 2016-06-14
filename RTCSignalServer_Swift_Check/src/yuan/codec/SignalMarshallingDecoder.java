package yuan.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;
/**
 * –≈¡ÓΩ‚¬Î∆˜
 * @author Jack Yuan
 *
 */
public class SignalMarshallingDecoder extends MarshallingDecoder {

	public SignalMarshallingDecoder(UnmarshallerProvider provider,
			int maxObjectSize) {
		super(provider, maxObjectSize);
	}

	public SignalMarshallingDecoder(UnmarshallerProvider provider) {
		super(provider);
	}
	
	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		return super.decode(ctx, in);
	}
	@Override
	protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer,
			int index, int length) {
		return super.extractFrame(ctx, buffer, index, length);
	}
	

}
