package com.itheima.reactor;

import com.itheima.reactor.cnxn.ServerCnxnFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * 参考 zk 的写法
 */
@Slf4j
public class ServerMain {

    public static void main(String[] args) {
        ServerMain main = new ServerMain();
        main.initializeAndRun(args);
    }

    protected void initializeAndRun(String[] args) {
        // 根据参数解析配置
        ServerConfig config = new ServerConfig();
        config.parse(args);

        //根据配置运行
        try {
            runFromConfig(config);
        } catch (IOException e) {
            log.error("exception={}",e.getMessage());
        }
    }

    private ServerCnxnFactory cnxnFactory;

    public void runFromConfig(ServerConfig config) throws IOException {
        // 创建Server
        Server server = new Server(config);
        CountDownLatch latch = new CountDownLatch(1);
        //创建连接管理器
        if (config.getClientPortAddress()!=null) {
            cnxnFactory = ServerCnxnFactory.createFactory();
            cnxnFactory.configure(config.getClientPortAddress());
            cnxnFactory.startup(server);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
