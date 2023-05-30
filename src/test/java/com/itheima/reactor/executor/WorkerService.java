package com.itheima.reactor.executor;

import com.itheima.reactor.NIOServerCnxn;
import com.itheima.reactor.Server;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作线程池/业务线程池
 *
 */
@Slf4j
public class WorkerService {
    private final String threadNamePrefix;
    private int numWorkerThreads;

    private final ExecutorService executorService;

    public WorkerService(String name, int numThreads) {
        this.threadNamePrefix = (name == null ? "" : name) + "Thread";
        this.numWorkerThreads = numThreads;
        /**
         * 初始化线程池
         */
        this.executorService = Executors.newFixedThreadPool(this.numWorkerThreads,new DaemonThreadFactory(threadNamePrefix));
    }

    public void schedule(WorkRequest workRequest) {
        executorService.execute(workRequest);
    }

    @Slf4j
    public static class WorkRequest implements Runnable {

        private final Server server;
        private Object msg;
        private NIOServerCnxn cnxn;

        public WorkRequest(Server server,NIOServerCnxn cnxn, Object msg) {
            this.server = server;
            this.cnxn = cnxn;
            this.msg = msg;
        }
        @Override
        public void run() {
            log.info("开始处理业务,msg={}",msg);
            //

            byte[] response = "hello client,i am reactor server".getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(response.length);
            buffer.put(response);
            buffer.flip();
            log.info("业务处理完成向客户端回写数据");
            cnxn.write(buffer);
        }
    }




     static class DaemonThreadFactory implements ThreadFactory {

        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        DaemonThreadFactory(String name) {
            this(name, 1);
        }

        DaemonThreadFactory(String name, int firstThreadNum) {
            threadNumber.set(firstThreadNum);
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = name + "-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

    }

}
