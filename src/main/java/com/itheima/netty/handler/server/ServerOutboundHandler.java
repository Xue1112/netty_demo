package com.itheima.netty.handler.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ServerOutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("ServerOutboundHandler  write ");
        ByteBuf buf = (ByteBuf) msg;
        log.info("ServerOutboundHandler----server send msg to client,msg ={}",buf.toString(StandardCharsets.UTF_8));
        super.write(ctx, msg, promise);
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes("append".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(buffer);
        // ctx.channel().writeAndFlush();
    }
}
