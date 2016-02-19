package mkrysiak.io.socket.SocketIOStress;

import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

public class WebSocketClient {
	
	EventLoopGroup group = new NioEventLoopGroup();
	
	public void open(URI uri) throws Exception {
		
		try {
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));
            
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(
                             new HttpClientCodec(),
                             new HttpObjectAggregator(8192),
                             handler);
                 }
             });

            ChannelFuture channelFuture = b.connect(uri.getHost(), uri.getPort()).sync();
            
            channelFuture.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                	if (!future.isSuccess()) {
                		future.cause().printStackTrace();
                	}
                }
            });
            
            handler.handshakeFuture().addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                	if (future.isSuccess()) {
                		//System.out.println("Handshake successful");
                	} else {
                		//System.err.println("Handshake attempt failed");
                		future.cause().printStackTrace();
                	}
                }
                    
            });
            
            //channelFuture.channel().closeFuture().sync();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		} finally {
			//group.shutdownGracefully();
		}
	}
}
