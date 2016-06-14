package yuan.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yuan.codec.signal.SignalMsgDecoder;
import yuan.codec.signal.SignalMsgEncoder;
import yuan.message.SignalMsg;
import android.util.Log;

/**
 * 客户端运行的主类
 * 
 * @author Jack Yuan
 *
 */
public class SignalClient {

	// 创建一个只有一个线程的线程池，用于断线重连
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	// Channel属性提供给外部使用
	private Channel clientchannel;

	private Object channelLock = new Object();

	// 回调的接口
	// 弱引用易于销毁资源
	private WeakReference<SignalEvents> events;
	// I/O 线程池
	private EventLoopGroup workgroup;

	// 用于回调的接口
	/**
	 * 暂时还未启用
	 * 
	 * @author Jack Yuan
	 *
	 */
	public static interface SignalEvents {
		// 加入房间的回调函数，会回调房间的一些信息
		void joinRoomResult(SignalMsg resultmsg);

		// 离开房间的回调函数
		void leaveRoomResult(SignalMsg resultmsg);

		// 收到其它客户端转发过来的answer的回调函数
		void forWardAnswer(SignalMsg resultmsg);

		// 收到其它客户端转发过来的candidate的回调函数
		void forWardCandidate(SignalMsg resultmsg);

		// 意外情况channel关闭的回调函数
		void onChannelClose();
	}

	/**
	 * 构造函数，需要传入一个回调的接口作为参数
	 * 
	 * @param events
	 */
	public SignalClient(SignalEvents events) {
		this.events=new WeakReference<SignalClient.SignalEvents>(events);
		// 决定在构造函数的时候就配置并且异步连接上信令服务器
		executor.execute(new Runnable() {

			@Override
			public void run() {
				SignalClient.this.connect(ClientConfig.REMOTEHOST,
						ClientConfig.REMOTEPORT);
			}
		});

	}

	/**
	 * 给外部一个channel属性
	 * 
	 * @return
	 */
	public Channel getChannel() {
		synchronized (channelLock) {
			if (clientchannel == null)
				try {
					channelLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			return clientchannel;
		}

	}

	/**
	 * 必须先connect才能获取clientchannel
	 * 
	 * @param host
	 * @param port
	 */
	private void connect(String host, int port) {
		 workgroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workgroup).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(
									new SignalMsgDecoder(1024 * 1024, 4, 4, -8,
											0));
							ch.pipeline().addLast(new SignalMsgEncoder());
							ch.pipeline().addLast(new ReadTimeoutHandler(50));
							ch.pipeline().addLast(new HeartBeatReqHandler());
							ch.pipeline().addLast(
									new ClientRoomServiceHandler(events.get()));
							/**
							 * 下面还需要添加一些业务级的handler
							 */
						}
					});

			ChannelFuture future = bootstrap.connect(host, port).sync();
			synchronized (channelLock) {
				clientchannel = future.channel();
				channelLock.notify();
			}
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.w("newTag", e.toString());
		} finally {
			workgroup.shutdownGracefully();
			executor.shutdown();
			if(clientchannel!=null)
				//在连接服务器成功后，才会告知Channel的关闭
			events.get().onChannelClose();
		}
	}
	
	/**
	 * 主动销毁连接
	 * 该方法目前只适用于在没连接上服务器之前，销毁的情况
	 */
	public void disconnect(){
		workgroup.shutdownGracefully();
		executor.shutdownNow();
	}
}
