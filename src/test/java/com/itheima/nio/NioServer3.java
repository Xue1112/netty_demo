package com.itheima.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Slf4j
public class NioServer3 {

    // 基于nio编写一个服务端程序
    public static void main(String[] args) throws IOException {
        //打开一个ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置成非阻塞
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));

        // 打开一个selector
        Selector selector = Selector.open();
        // 将serverSocketChannel注册到selector上，监听连接事件
        SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //让selector执行多路复用程序
        new Thread(new SingleReactor(selector)).start();
    }

    private static class SingleReactor implements Runnable {

        private final Selector selector;

        public SingleReactor (Selector selector) {
            this.selector = selector;
        }
        @Override
        public void run() {
            //执行多路复用程序
            while (true) {
                try {
                    selector.select();// select是阻塞的
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        processSelectionkey(key);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processSelectionkey(SelectionKey key) throws IOException {
            if (key.isValid()) {
                // 根据事件类型判断
                if (key.isAcceptable()) {
                    //连接事件
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    //接收一个连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //设置非阻塞
                    socketChannel.configureBlocking(false);
                    // 将 socketChannel注册到 selector，监听可读事件
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    //可读事件,通道内数据准备就绪，可以从通道内读数据了
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 开始读数据
                    socketChannel.read(buffer);// 从socketChannel中读出数据,读到buffer中,对于buffer而言是写操作

                    //从 buffer 中拿到数据  对于 buffer 而言是读操作
                    buffer.flip();//切换为读模式
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    // 后续就是业务操作
                    String msg = new String(bytes, Charset.defaultCharset());
                    log.info("服务端收到了来自客户端的数据:{}",msg);

                    // 其他操作 业务操作

                    // 服务端需要向客户端回写数据
                    buffer.clear();
                    buffer.put("hello nioclient,i am nioserver".getBytes(StandardCharsets.UTF_8));
                    buffer.flip();
                    socketChannel.write(buffer);
                }

            }
        }
    }


}
