package com.itheima.reactor;

import java.nio.ByteBuffer;

public abstract class ServerCnxn {

    final Server server;

    public ServerCnxn(final Server server) {
        this.server = server;
    }

    public abstract void write(ByteBuffer response);
}
