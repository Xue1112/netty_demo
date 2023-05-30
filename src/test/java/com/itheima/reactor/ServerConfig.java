package com.itheima.reactor;

import java.net.InetSocketAddress;

public class ServerConfig {

    protected InetSocketAddress clientPortAddress ;


    public ServerConfig() {
        clientPortAddress = new InetSocketAddress(8888);
    }

    public void parse(String[] args) {
        /*if (args.length < 2 || args.length > 4) {
            throw new IllegalArgumentException("Invalid number of arguments:" + Arrays.toString(args));
        }
        clientPortAddress = new InetSocketAddress(Integer.parseInt(args[0]));*/
    }

    public InetSocketAddress getClientPortAddress() {
        return clientPortAddress;
    }
}
