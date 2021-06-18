package me.project.rpc.client.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 发送处理类，定义 Netty 入站处理规则
 */
public class SendHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SendHandler.class);

    private final CountDownLatch countDownLatch;

    private Object readMsg;

    private final byte[] data;

    public SendHandler(byte[] data) {
        countDownLatch = new CountDownLatch(1);
        this.data = data;
    }

    /**
     * 连接服务端成功后，发送请求数据
     *
     * @param ctx 上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Successfully connected to server: {}", ctx);
        ByteBuf requestBuffer = Unpooled.buffer(data.length);
        requestBuffer.writeBytes(data);
        logger.debug("Client sends message: {}", requestBuffer);
        ctx.writeAndFlush(requestBuffer);
    }

    /**
     * 读取数据，读取完毕后释放锁
     *
     * @param ctx 上下文
     * @param msg ByteBuf
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.debug("Client reads message: {}", msg);
        ByteBuf msgBuffer = (ByteBuf) msg;
        byte[] response = new byte[msgBuffer.readableBytes()];
        msgBuffer.readBytes(response);

        // 手动回收
        ReferenceCountUtil.release(msgBuffer);

        readMsg = response;
        countDownLatch.countDown();
    }

    /**
     * 等待读取数据完成
     *
     * @return 响应数据
     */
    public Object responseData() throws InterruptedException {
        countDownLatch.await();
        return readMsg;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.error("Exception occurred: {}", cause.getMessage());
        ctx.close();
    }
}
