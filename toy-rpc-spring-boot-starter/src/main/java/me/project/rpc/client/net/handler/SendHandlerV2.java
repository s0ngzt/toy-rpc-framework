package me.project.rpc.client.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import me.project.rpc.client.net.NettyNetClient;
import me.project.rpc.client.net.RpcFuture;
import me.project.rpc.common.exception.RpcException;
import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;
import me.project.rpc.common.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SendHandlerV2 extends ChannelInboundHandlerAdapter {

    static final int CHANNEL_WAIT_TIME = 4; // 等待通道建立最大时间
    static final int RESPONSE_WAIT_TIME = 8; // 等待响应最大时间
    private static final Logger logger = LoggerFactory.getLogger(SendHandlerV2.class);
    private static final Map<String, RpcFuture<RpcResponse>> requestMap = new ConcurrentHashMap<>();
    private final String remoteAddress;
    private final MessageProtocol messageProtocol;
    private final CountDownLatch cdl = new CountDownLatch(1);
    private volatile Channel channel;

    public SendHandlerV2(MessageProtocol messageProtocol, String remoteAddress) {
        this.messageProtocol = messageProtocol;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        cdl.countDown();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Connect to server successfully:{}", ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Client reads message:{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] resp = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(resp);
        // 手动回收
        ReferenceCountUtil.release(byteBuf);
        RpcResponse response = messageProtocol.unmarshallingResponse(resp);
        RpcFuture<RpcResponse> future = requestMap.get(response.getRequestId());
        future.setResponse(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.error("Exception occurred:{}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.error("channel inactive with remoteAddress:[{}]", remoteAddress);
        NettyNetClient.connectedServerNodes.remove(remoteAddress);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    public RpcResponse sendRequest(RpcRequest request) {
        RpcResponse response;
        RpcFuture<RpcResponse> future = new RpcFuture<>();
        requestMap.put(request.getRequestId(), future);
        try {
            byte[] data = messageProtocol.marshallingRequest(request);
            ByteBuf reqBuf = Unpooled.buffer(data.length);
            reqBuf.writeBytes(data);
            if (cdl.await(CHANNEL_WAIT_TIME, TimeUnit.SECONDS)) {
                channel.writeAndFlush(reqBuf);
                // 等待响应
                response = future.get(RESPONSE_WAIT_TIME, TimeUnit.SECONDS);
            } else {
                throw new RpcException("establish channel time out");
            }
        } catch (Exception e) {
            throw new RpcException(e.getMessage());
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response;
    }
}
