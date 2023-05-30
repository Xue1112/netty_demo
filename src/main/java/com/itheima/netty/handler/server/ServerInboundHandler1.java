package com.itheima.netty.handler.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerInboundHandler1 extends ChannelInboundHandlerAdapter {


    /**
     * 通道准备就绪
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerInboundHandler1 channelActive-----");
        super.channelActive(ctx);
    }

    /**
     * 从通道读到了数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("ServerInboundHandler1 channelRead----,remoteAddress={}",ctx.channel().remoteAddress());
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String data = new String(bytes, Charset.defaultCharset());
        log.info("ServerInboundHandler1:received client data = {}",data);
        super.channelRead(ctx, msg);
    }

    /**
     * 通道内数据已读取完毕
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerInboundHandler1 channelReadComplete----");
        //向客户端写点儿数据
        Channel channel = ctx.channel();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes("hello nettyclient,i am nettyserver".getBytes(StandardCharsets.UTF_8));
        // channel.writeAndFlush(buffer);
        ChannelFuture future = ctx.writeAndFlush(buffer);
       /* future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.get();
            }
        });*/

        super.channelReadComplete(ctx);
    }

    /**
     * 异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("ServerInboundHandler1 exceptionCaught----,cause={}",cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }
}
