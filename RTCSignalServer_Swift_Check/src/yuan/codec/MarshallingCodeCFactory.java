package yuan.codec;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 *静态构建Marshalling编解码器
 * @author Jack Yuan
 *
 */
public class MarshallingCodeCFactory {
	/**
	 * 静态构建marshallingdecoder
	 * @return
	 */
	public static SignalMarshallingDecoder buildMarshallingDecoder(){
		final MarshallerFactory factory=Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration config=new MarshallingConfiguration();
		config.setVersion(5);
		UnmarshallerProvider provider=new DefaultUnmarshallerProvider(factory,config);
		SignalMarshallingDecoder decoder=new SignalMarshallingDecoder(provider, 1024<<2);
		return decoder;
	}
	/**
	 * 静态构建marshallingencoder
	 * @return
	 */
	public static SignalMarshallingEncoder buildMarshallingEncoder(){
		final MarshallerFactory factory=Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration config=new MarshallingConfiguration();
		config.setVersion(5);
		MarshallerProvider provider=new DefaultMarshallerProvider(factory, config);
		SignalMarshallingEncoder encoder=new SignalMarshallingEncoder(provider);
		return encoder;
	}
}
