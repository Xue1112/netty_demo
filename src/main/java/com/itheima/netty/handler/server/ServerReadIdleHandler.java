package com.itheima.netty.handler.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerReadIdleHandler extends IdleStateHandler {

    public ServerReadIdleHandler() {
        super(10, 0, 0, TimeUnit.SECONDS);
    }


    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info("server channel idle----");
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
            ctx.close();
            log.info("server read idle , close channel.....");
            return;
        }
        super.channelIdle(ctx, evt);
    }
}
