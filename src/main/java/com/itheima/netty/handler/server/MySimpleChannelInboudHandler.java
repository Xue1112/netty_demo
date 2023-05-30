package com.itheima.netty.handler.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MySimpleChannelInboudHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("MySimpleChannelInboudHandler---channelRead0");
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        String data = new String(bytes, Charset.defaultCharset());
        log.info("收到的数据:{}",data);
    }
}
