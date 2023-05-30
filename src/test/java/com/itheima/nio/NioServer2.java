package com.itheima.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioServer2 {

    public static void main(String[] args) throws Exception{
        //打开一个serversocketchannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //绑定端口监听,设置非阻塞
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));
        serverSocketChannel.configureBlocking(false);

        //打开一个selector
        Selector selector = Selector.open();

        //将serversocketchannel注册到selector上，监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //创建单reactor线程 让selector执行多路复用程序
        new Thread(new SingleReactor(selector) ).start();
    }

    static class SingleReactor implements Runnable {

        private final Selector selector;

        public SingleReactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            //轮询selector 处理事件即可
            while (true) {
                try {
                    selector.select(1000);
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        processSelectionKey(selectionKey);
                        iterator.remove();
                    }
                } catch (Exception e) {

                }
            }
        }

        private void processSelectionKey(SelectionKey selectionKey) throws Exception {
            if (selectionKey.isValid()) {
                //根据事件类型分发处理
                if (selectionKey.isAcceptable()) { // 连接事件准备就绪了
                    ServerSocketChannel serverSocketChannel  = (ServerSocketChannel) selectionKey.channel();
                    //接收一个连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //配置非阻塞模式
                    socketChannel.configureBlocking(false);
                    //将其注册到selector上，监听可读事件
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }

                if (selectionKey.isReadable()) { //通道可读事件准备就绪，可以读取数据了
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //从通道内读数据即可
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    socketChannel.read(byteBuffer);

                    //从byteBuffer中拿出数据 转换模式
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    //将数据转换成string
                    String msg = new String(bytes,Charset.defaultCharset());
                    System.out.println("服务端收到来自客户端的数据:"+msg);

                    //像客户端写数据  可以重新创建一个buffer
                    byteBuffer.clear();
                    byteBuffer.put("hello nioclient,i am nioserver".getBytes(StandardCharsets.UTF_8));
                    byteBuffer.flip();
                    socketChannel.write(byteBuffer);
                }

            }
        }
    }
}
