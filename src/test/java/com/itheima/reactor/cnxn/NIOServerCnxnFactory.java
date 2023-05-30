package com.itheima.reactor.cnxn;

import com.itheima.reactor.NIOServerCnxn;
import com.itheima.reactor.Server;
import com.itheima.reactor.executor.WorkerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class NIOServerCnxnFactory extends ServerCnxnFactory{

    //private int numIOSelectorThreads;

    private int numIOWorkerThreads;
    private int numWorkerThreads;

    private final Set<IOThread> ioThreads = new HashSet<IOThread>();

    private AcceptThread acceptThread;

    ServerSocketChannel serverSocketChannel;

    private WorkerService workerPool;

    protected Server server;

    @Override
    public void configure(InetSocketAddress clientPortAddress) throws IOException {
        log.info("NIOServerCnxnFactory-----configure");
        // 准备一些参数
        int core = Runtime.getRuntime().availableProcessors();
        numIOWorkerThreads = 2 *  core;
        numWorkerThreads = 2 * core;

        // 将IO线程创建出来，
        for (int i=0;i<numIOWorkerThreads;i++) {
            ioThreads.add(new IOThread(i));//只创建但未启动
        }

        // 创建AcceptThread
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(clientPortAddress);
        acceptThread = new AcceptThread(serverSocketChannel,ioThreads);
    }

    @Override
    public void startup(Server server) {
        start();
        setServer(server);
        server.startup();
    }

    private void start() {
        // 将相关的线程启动
        if (workerPool == null) {
            log.info("NIOServerCnxnFactory 启动 workerPool");
            workerPool = new WorkerService("worker",numWorkerThreads);
        }
        // 将IO线程启动
        log.info("NIOServerCnxnFactory 启动 IOThread");
        for (IOThread ioThread:ioThreads) {
            ioThread.start();
        }
        //启动accept线程
        log.info("NIOServerCnxnFactory 启动 acceptThread");
        acceptThread.start();
    }

    private void setServer(Server server) {
        this.server = server;
        server.setServerCnxnFactory(this);
        server.setWorkerPool(this.workerPool);
    }



    private abstract class AbstractSelectThread extends Thread{

        protected final Selector selector;

        public AbstractSelectThread(String name) throws IOException {
            super(name);
            //setDaemon(true);
            this.selector = Selector.open();
        }
    }


    private class AcceptThread extends AbstractSelectThread {

        private  final Logger log = LoggerFactory.getLogger(AcceptThread.class);

        private final ServerSocketChannel serverSocketChannel;
        private final Collection<IOThread> ioThreads;
        private Iterator<IOThread> ioThreadIterator;

        public AcceptThread(ServerSocketChannel serverSocketChannel, Set<IOThread> ioThreads) throws IOException {
            super("AcceptThread");
            this.serverSocketChannel = serverSocketChannel;
            //注册到 selector 上
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            this.ioThreads = Collections.unmodifiableList(new ArrayList<IOThread>(ioThreads));
            ioThreadIterator = this.ioThreads.iterator();
        }

        @Override
        public void run() {
            //不断接收连接
            while (!serverSocketChannel.socket().isClosed()) {
                select();
            }
        }

        private void select() {
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    log.info("AcceptThread selectedKeys");
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }
                    // 判断是否是连接事件
                    if (key.isAcceptable()) {
                        //建立连接
                        doAccept();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void doAccept() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                log.info("AcceptThread accept socketChannel={}",socketChannel);
                socketChannel.configureBlocking(false);
                //从IO线程中选一个io线程出来
                if (!ioThreadIterator.hasNext()) {
                    ioThreadIterator = ioThreads.iterator();
                }
                IOThread ioThread = ioThreadIterator.next();
                ioThread.addAcceptedConnection(socketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public class IOThread extends AbstractSelectThread {
        private  final Logger log = LoggerFactory.getLogger(IOThread.class);

        private final Queue<SocketChannel> acceptedQueue;

        public IOThread(int id) throws IOException {
            super("IOthread-"+id);
            acceptedQueue = new LinkedBlockingQueue<SocketChannel>();
        }


        @Override
        public void run() {
            /**
             * 两件事
             * 1，轮询已经注册到selector上的channel，看是否有io事件，有拿出来处理
             * 2、检测acceptedQueue是否有新连接过来，如果有，则将其注册到selector上
             */
            while (true) {
                select();
                processAcceptedConnections();
            }
        }

        private void processAcceptedConnections() {
            SocketChannel accepted;
            while ((accepted = acceptedQueue.poll()) !=null) {
                log.info("IOThread processAcceptedConnections");
                try {
                    log.info("将{}注册到selector上",accepted);
                    SelectionKey key = accepted.register(selector, SelectionKey.OP_READ);
                    //封装连接
                    NIOServerCnxn cnxn = new NIOServerCnxn(server, accepted,key,this);

                    key.attach(cnxn);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        }

        private void select() {
            try {
                selector.select();
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    log.info("IOThread selectedKeys");
                    SelectionKey key = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }
                    //根据类型判断
                    if (key.isReadable() || key.isWritable()) {
                        // 处理IO
                        handleIO(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleIO(SelectionKey key) {
            log.info("IOThread handleIO");
            //获取对应的连接
            NIOServerCnxn cnxn = (NIOServerCnxn) key.attachment();
            //根据事件类型处理io
            cnxn.doIO(key);
        }


        public boolean addAcceptedConnection(SocketChannel socketChannel) {
            if(!acceptedQueue.offer(socketChannel)) {
                return false;
            }
            wakeupSelector();
            return true;
        }


        protected void cleanupSelectionKey(SelectionKey key) {
            if (key != null) {
                try {
                    key.cancel();
                } catch (Exception ex) {
                    log.debug("ignoring exception during selectionkey cancel", ex);
                }
            }
        }

        public void wakeupSelector() {
            selector.wakeup();
        }
    }
}
