package com.itheima.reactor.cnxn;

import com.itheima.reactor.Server;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public abstract class ServerCnxnFactory {

    public static final String SERVER_CNXN_FACTORY = "serverCnxnFactory";

    public static ServerCnxnFactory createFactory() throws IOException {
        String serverCnxnFactoryName = System.getProperty(SERVER_CNXN_FACTORY);
        if (serverCnxnFactoryName == null) {
            serverCnxnFactoryName = NIOServerCnxnFactory.class.getName();
        }
        try {
            ServerCnxnFactory instance = (ServerCnxnFactory)Class.forName(serverCnxnFactoryName).getDeclaredConstructor().newInstance();
            return instance;
        }  catch (Exception e) {
            log.info("couldn‘n instantiate {},exception={}",serverCnxnFactoryName,e);
            throw new IOException("");
        }
    }

    public abstract void configure(InetSocketAddress clientPortAddress) throws IOException;

    public abstract void startup(Server server);
}
