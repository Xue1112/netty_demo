package com.itheima.reactor;

import com.itheima.reactor.cnxn.NIOServerCnxnFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

@Slf4j
public class NIOServerCnxn extends ServerCnxn {

    private final NIOServerCnxnFactory.IOThread ioThread;
    private final SelectionKey key;
    private final SocketChannel socketChannel;

    //private final ByteBuffer lenBuffer = ByteBuffer.allocate(4);

    protected ByteBuffer incomingBuffer = ByteBuffer.allocate(1024);

    public NIOServerCnxn(Server server,SocketChannel accepted, SelectionKey key, NIOServerCnxnFactory.IOThread ioThread) {
        super(server);
        this.socketChannel = accepted;
        this.key = key;
        this.ioThread = ioThread;
    }

    public void doIO(SelectionKey key) {
        if (!isSocketOpen()) {
            log.warn("socket is closed");
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (key.isReadable()) {
            //读取客户端数据
            try {
                int read = socketChannel.read(incomingBuffer);
                // 获取具体的数据
                incomingBuffer.flip();
                byte[] bytes = new byte[incomingBuffer.remaining()];
                incomingBuffer.get(bytes);
                String msg = new String(bytes, Charset.defaultCharset());
                log.info("客户端{}发送的数据是{}",socketChannel,msg);

                // 提交给Server进行业务处理
                server.process(this,msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*if (key.isWritable()) {

        }*/
    }

    protected boolean isSocketOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public void write(ByteBuffer response) {
        if (!isSocketOpen()) {
            log.warn("socket is closed");
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            socketChannel.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
