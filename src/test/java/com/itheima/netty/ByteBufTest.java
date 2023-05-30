package com.itheima.netty;

import io.netty.buffer.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

@Slf4j
/**
 * @description
 * @author: ts
 * @create:2021-04-25 11:08
 */
public class ByteBufTest {

   // private static final Logger log = LoggerFactory.getLogger(ByteBufTest.class);

    @Test
    public void testRead() {
        //构造有数据的bytebuf
        ByteBuf buf = Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8));
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        //按字节读取
        while (buf.isReadable()) {
            log.info(""+buf.readByte());
        }
        //通过下标读取
        for (int i=0;i<buf.readableBytes();i++) {
            log.info(""+buf.getByte(i));//此种方式不会变更readerIndex,即后续还可继续读取
        }
        //直接读到字节数组
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        log.info("---"+new String(bytes));
    }


    @Test
    public void testWrite() {
        //准备好要写入的数据
        byte[] bytes = "hello netty".getBytes(StandardCharsets.UTF_8);

        //构造bytebuf,给定初始容量和最大容量
        ByteBuf buf = Unpooled.buffer(10, 1024);

        //每次写1个字节进去
        for (int i=0;i<100;i++) {
            buf.writeByte(i);
        }
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        log.info("-------------------华丽的分割线--------------------");

        //直接将bytes中的数据写入bytebuf
        buf.writeBytes(bytes);
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
    }

    @Test
    public void testDiscard() {
        //创建带数据的bytebuf
        ByteBuf buf = Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8));
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        log.info("-------------------华丽的分割线--------------------");
        for (int i=0;i<5;i++) {
            buf.readByte();
        }
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        log.info("-------------------华丽的分割线--------------------");
        //丢弃已读取的字节空间---可写空间变多
        buf.discardReadBytes();
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        log.info("-------------------华丽的分割线--------------------");
        //clear 将readerIndex=writerIndex=0;只是指针变化,数据并没有清除,支持继续写入
        buf.clear();
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        log.info("-------------------华丽的分割线--------------------");
        buf.writeBytes("hello netty".getBytes(StandardCharsets.UTF_8));
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
        log.info("-------------------华丽的分割线--------------------");

        //释放消息
        //buf.release();
        ReferenceCountUtil.release(buf);//里面的数据已经被释放了 readerIndex=0,writerIndex在最后,数组容量为0
        log.info("bytebuf容量为:{}",buf.capacity());
        log.info("bytebuf可读容量为:{}",buf.readableBytes());//此时已经没有数据了
        log.info("bytebuf可写容量为:{}",buf.writableBytes());
    }

    @Test
    public void testWrap() {
        byte[] bytes = "123456".getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        bytes[0] = 1;
        //写入
        log.info("原始数据:{}",new String(bytes,StandardCharsets.UTF_8));
        log.info("buf中的数据:{}",buf.toString(StandardCharsets.UTF_8));
        log.info("-------------------华丽的分割线--------------------");

        byte[] bytes1 = "hello".getBytes(StandardCharsets.UTF_8);
        ByteBuf buf1 = Unpooled.copiedBuffer(bytes1);
        bytes1[0] = 1;
        //写入
        log.info("原始数据:{}",new String(bytes1,StandardCharsets.UTF_8));
        log.info("buf中的数据:{}",buf1.toString(StandardCharsets.UTF_8));

        ByteBuf byteBuf = new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT,10,100);
        byteBuf.writeBytes("hello".getBytes(StandardCharsets.UTF_8));
        log.info("byteBuf类型:{}",byteBuf.getClass().getTypeName());

    }

    @Test
    public void testUnpooledByteBuf() {
        ByteBuf buf = new UnpooledDirectByteBuf(UnpooledByteBufAllocator.DEFAULT,100,1024);
        buf.writeBytes("hello".getBytes(StandardCharsets.UTF_8));
        log.info("---buf的类型：{}",buf.getClass().getTypeName());

        ByteBuf buf1 = new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT,100,1024);
        buf1.writeBytes("hello word".getBytes(StandardCharsets.UTF_8));
        log.info("---buf1的类型是:{}",buf1.getClass().getTypeName());
    }

}
