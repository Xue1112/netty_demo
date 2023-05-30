package com.itheima.reactor;

import com.itheima.reactor.cnxn.NIOServerCnxnFactory;
import com.itheima.reactor.cnxn.ServerCnxnFactory;
import com.itheima.reactor.executor.WorkerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

    private final ServerConfig config;

    protected ServerCnxnFactory serverCnxnFactory;

    private WorkerService workerPool;

    public Server(ServerConfig config) {
        this.config = config;
    }

    public void setServerCnxnFactory(NIOServerCnxnFactory nioServerCnxnFactory) {
        this.serverCnxnFactory = nioServerCnxnFactory;
    }

    public void setWorkerPool(WorkerService workerPool) {
        this.workerPool = workerPool;
    }

    public void startup() {

    }

    public void process(NIOServerCnxn nioServerCnxn, Object msg) {
        log.info("Server process msg={}",msg);
        // 提交给 workerPool
        WorkerService.WorkRequest workRequest = new WorkerService.WorkRequest(this,nioServerCnxn,msg);
        workerPool.schedule(workRequest);
    }
}
