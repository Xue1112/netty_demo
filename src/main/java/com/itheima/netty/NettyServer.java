package com.itheima.netty;

import com.itheima.netty.codec.ProtoStuffDecoder;
import com.itheima.netty.codec.ProtoStuffEncoder;
import com.itheima.netty.handler.server.*;
import com.itheima.netty.pojo.MessageProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;

import java.nio.charset.StandardCharsets;

public class NettyServer {

    public static void main(String[] args) {
        NettyServer server = new NettyServer();
        server.start(8888);
    }

    private void start(int port) {
        EventLoopGroup boss = new NioEventLoopGroup(1,new DefaultThreadFactory("boss"));
        EventLoopGroup worker = new NioEventLoopGroup(0,new DefaultThreadFactory("worker"));
        EventExecutorGroup business = new UnorderedThreadPoolEventExecutor(NettyRuntime.availableProcessors()*2,new DefaultThreadFactory("business"));
        // ServerInboundHandler1 serverInboundHandler1 = new ServerInboundHandler1();
        ServerInboundHandler2 inboundHandler2 = new ServerInboundHandler2();
        // 基于netty引导整个服务端程序的启动
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    //当客户端 SocketChannel初始化时回调该方法,添加handler
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        /*pipeline.addLast(new ServerOutboundHandler());


                        pipeline.addLast(new ServerInboundHandler1());
//                        pipeline.addLast(new ServerInboundHandler2());
                        pipeline.addLast(inboundHandler2);
                        pipeline.addLast(new MySimpleChannelInboudHandler());*/



                        //pipeline.addLast(new FixedLengthFrameDecoder(1024));
                        // pipeline.addLast(new LineBasedFrameDecoder(65536));
                        /*ByteBuf buf = ch.alloc().buffer().writeBytes("$".getBytes(StandardCharsets.UTF_8));
                        pipeline.addLast(new DelimiterBasedFrameDecoder(65536,buf));*/
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        pipeline.addLast(new ServerReadIdleHandler());


                        pipeline.addLast(new LengthFieldPrepender(4));
                        //pipeline.addLast(new StringEncoder());
                        //pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ProtoStuffEncoder());

                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,4));
                        //pipeline.addLast(new StringDecoder());
                        //pipeline.addLast(new ProtobufDecoder(MessageProto.Message.getDefaultInstance()));
                        pipeline.addLast("protostuffdecoder",new ProtoStuffDecoder());
                        pipeline.addLast(business,"tcptesthandler",new TcpStickHalfHandler1());
                    }
                });
        // 绑定端口并启动
        try {
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // 监听端口的关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //清理一些资源
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }

    }
}
