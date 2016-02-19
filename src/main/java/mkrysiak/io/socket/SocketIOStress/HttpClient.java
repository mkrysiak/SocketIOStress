package mkrysiak.io.socket.SocketIOStress;

import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;


public class HttpClient {

	EventLoopGroup group = new NioEventLoopGroup();
	
	final static AttributeKey<URI> SERVER_URI = AttributeKey.newInstance("uri");
	
	public void open(URI uri) throws Exception {
		
		try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(
                             new HttpClientCodec(),
                             new HttpObjectAggregator(32 * 1024),
                             new HttpClientHandler());
                 }
             });
            b.option(ChannelOption.SO_KEEPALIVE, true)
            	.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            b.attr(SERVER_URI, uri);

            ChannelFuture channelFuture = b.connect(uri.getHost(), uri.getPort()).sync();
            
            channelFuture.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                	if (!future.isSuccess()) {
                		future.cause().printStackTrace();
                	}
                }
            });
            
            HttpRequest request = new DefaultFullHttpRequest(
            		HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getPath() + "?" + uri.getQuery());
            request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
            
            channelFuture.channel().writeAndFlush(request);
                     
            //channelFuture.channel().closeFuture().sync();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		} finally {
			//group.shutdownGracefully();
		}
	}
}
