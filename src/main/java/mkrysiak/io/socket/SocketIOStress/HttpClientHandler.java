package mkrysiak.io.socket.SocketIOStress;

import java.net.URI;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;


public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected!");
    }
	
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;

            if (!response.headers().isEmpty()) {
            	String sc = response.headers().get(HttpHeaders.Names.SET_COOKIE);
            	String sid = ClientCookieDecoder.LAX.decode(sc).value();
   
	            try {
	              	URI uri = ctx.channel().attr(HttpClient.SERVER_URI).get();
	                HttpRequest request = new DefaultFullHttpRequest(
	                		HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getPath() + "?" + uri.getQuery() + "&sid=" + sid);
	                request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
	                request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
	                request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
	                ctx.channel().writeAndFlush(request);
	            } catch (Exception e) {
	            	System.err.println(e.getMessage());
	            }
	        }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

