package com.itheima.netty.handler.server;

import com.itheima.netty.pojo.MessageProto;
import com.itheima.netty.pojo.UserInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TcpStickHalfHandler1 extends ChannelInboundHandlerAdapter {

    int count =0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf buf = (ByteBuf) msg;
       // String data = (String) msg;
//        MessageProto.Message message = (MessageProto.Message) msg;
        UserInfo data = (UserInfo) msg;
        count++;
        //log.info("---服务端收到的第{}个数据:{}",count,buf.toString(StandardCharsets.UTF_8));
        log.info("---服务端收到的第{}个数据:{}",count,data);
        super.channelRead(ctx, msg);
    }
}
