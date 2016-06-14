package yuan.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.logging.Logger;

import yuan.codec.signal.SignalMsgDecoder;
import yuan.codec.signal.SignalMsgEncoder;
import yuan.server.room.RoomServiceHandler;

/**
 * 该类是服务端的启动类
 * @author Jack Yuan
 *
 */
public class SignalServer {
	
	private  static Logger logger=Logger.getLogger(SignalServer.class.getName());
	
	private void start(int port) {
		EventLoopGroup bossgroup = new NioEventLoopGroup();
		EventLoopGroup workgroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossgroup, workgroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							//在pipline增加断开检测的handler
							ch.pipeline().addLast(new OutboundHandler());
							//在pipline增加信令消息的解码器
							ch.pipeline().addLast(
									new SignalMsgDecoder(1024 * 1024, 4, 4, -8,
											0));
							//在pipline增加信令消息的编码器
							ch.pipeline().addLast(new SignalMsgEncoder());
							ch.pipeline().addLast(new ReadTimeoutHandler(50));
							ch.pipeline().addLast(new RoomServiceHandler());
							ch.pipeline().addLast(new HeartBeatRepHandler());
							
							/**
							 * 要在下面增加一些具体的业务型的handler
							 */
							
						}
					});
			
			logger.info("The signalserver is working");
			ChannelFuture future = bootstrap.bind(ServerConfig.SERVER_HOST,port).sync();
			future.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossgroup.shutdownGracefully();
			workgroup.shutdownGracefully();
		}
	}
	
	/**
	 * 程序的启动入口
	 * @param args
	 */
	public static void main(String[] args) {
		//从配置的端口监听来自客户端的消息
		new SignalServer().start(ServerConfig.SERVER_PORT);
	}
}
