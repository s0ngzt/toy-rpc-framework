package me.project.rpc.client.net;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.project.rpc.client.net.handler.SendHandler;
import me.project.rpc.client.net.handler.SendHandlerV2;
import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;
import me.project.rpc.common.model.RpcService;
import me.project.rpc.common.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Netty 网络请求客户端实现
 */
public class NettyNetClient implements NetClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    // TODO
    private static final ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 200,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactoryBuilder()
            .setNameFormat("rpcClient-%d")
            .build());
    /**
     * 已连接的服务缓存
     * key: 服务地址，格式：ip:port
     */
    public static Map<String, SendHandlerV2> connectedServerNodes = new ConcurrentHashMap<>();
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    /**
     * 发送请求
     *
     * @param data       请求数据
     * @param rpcService 服务信息
     * @return 响应数据
     */
    @Override
    public byte[] sendRequest(byte[] data, RpcService rpcService) throws InterruptedException {
        String[] addressInfo = rpcService.getAddress().split(":");
        String serverAddress = addressInfo[0];
        String serverPort = addressInfo[1];

        SendHandler sendHandler = new SendHandler(data);
        byte[] responseData;
        // 配置客户端
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(sendHandler);
                        }
                    });
            // 启动客户端连接
            bootstrap.connect(serverAddress, Integer.parseInt(serverPort)).sync();
            responseData = (byte[]) sendHandler.responseData();
            logger.info("Got reply: {}", responseData);
        } finally {
            // 释放线程组资源
            group.shutdownGracefully();
        }
        return responseData;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request, RpcService service, MessageProtocol messageProtocol) {
        String address = service.getAddress();
        synchronized (address) {
            if (connectedServerNodes.containsKey(address)) {
                SendHandlerV2 handler = connectedServerNodes.get(address);
                logger.info("使用现有的连接");
                return handler.sendRequest(request);
            }

            String[] addressInfo = address.split(":");
            final String serverAddress = addressInfo[0];
            final String serverPort = addressInfo[1];
            final SendHandlerV2 handler = new SendHandlerV2(messageProtocol, address);
            threadPool.submit(() -> {
                        // 配置客户端
                        Bootstrap b = new Bootstrap();
                        b.group(eventLoopGroup).channel(NioSocketChannel.class)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) {
                                        ChannelPipeline pipeline = socketChannel.pipeline();
                                        pipeline.addLast(handler);
                                    }
                                });
                        // 启用客户端连接
                        ChannelFuture channelFuture = b.connect(serverAddress, Integer.parseInt(serverPort));
                        channelFuture.addListener(
                                (ChannelFutureListener) channelFuture1 -> connectedServerNodes.put(address, handler));
                    }
            );
            logger.info("使用新的连接。。。");
            return handler.sendRequest(request);
        }
    }
}
