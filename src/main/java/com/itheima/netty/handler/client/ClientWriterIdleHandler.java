package com.itheima.netty.handler.client;

import com.itheima.netty.pojo.UserInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientWriterIdleHandler extends IdleStateHandler {

    public ClientWriterIdleHandler() {
        super(0, 5, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT) {
            //发送keepalive消息
            UserInfo userInfo = new UserInfo();
            userInfo.setName("keeepalive");
            ctx.channel().writeAndFlush(userInfo);
        }
        super.channelIdle(ctx, evt);
    }
}
