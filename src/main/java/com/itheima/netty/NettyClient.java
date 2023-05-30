package com.itheima.netty;

import com.itheima.netty.codec.ProtoStuffDecoder;
import com.itheima.netty.codec.ProtoStuffEncoder;
import com.itheima.netty.handler.client.ClientInboundHandler;
import com.itheima.netty.handler.client.ClientWriterIdleHandler;
import com.itheima.netty.pojo.MessageProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NettyClient {
    public static void main(String[] args) {
        NettyClient client = new NettyClient();
        client.start("127.0.0.1", 8888);
    }

    public void start(String host,int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ClientWriterIdleHandler());

                        pipeline.addLast(new LengthFieldPrepender(4));
                        //pipeline.addLast(new StringEncoder());
                        //pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ProtoStuffEncoder());


                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,4));
                        //pipeline.addLast(new StringDecoder());
                        //pipeline.addLast(new ProtobufDecoder(MessageProto.Message.getDefaultInstance()));
                        pipeline.addLast(new ProtoStuffDecoder());

                        // pipeline.addLast();//解码器
                        pipeline.addLast(new ClientInboundHandler());

                    }
                });
        //连接服务端
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
            //客户端可以向服务端写数据了
           /* Channel channel = future.channel();
            ByteBuf buf = channel.alloc().buffer();
            buf.writeBytes("hello nettyserver,i am nettyclient".getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(buf);*/
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }
}
