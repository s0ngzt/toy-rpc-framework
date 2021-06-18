package me.project.rpc.config;

import me.project.rpc.annotation.LoadBalanceStrategy;
import me.project.rpc.annotation.MessageSerializationMethod;
import me.project.rpc.client.ClientProxyFactory;
import me.project.rpc.client.balance.LoadBalance;
import me.project.rpc.client.discovery.ZookeeperServiceDiscoverer;
import me.project.rpc.client.net.NettyNetClient;
import me.project.rpc.common.protocol.MessageProtocol;
import me.project.rpc.common.exception.RpcException;
import me.project.rpc.properties.RpcConfig;
import me.project.rpc.server.NettyRpcServer;
import me.project.rpc.server.RequestHandler;
import me.project.rpc.server.RpcServer;
import me.project.rpc.server.register.DefaultRpcProcessor;
import me.project.rpc.server.register.ServiceRegister;
import me.project.rpc.server.register.ZookeeperExportServiceRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * spring boot 自动配置类
 */
@Configuration
@EnableConfigurationProperties(RpcConfig.class)
public class RpcAutoConfiguration {

    @Bean
    public RpcConfig rpcConfig() {
        return new RpcConfig();
    }

    @Bean
    public ServiceRegister serviceRegister(@Autowired RpcConfig rpcConfig) {
        return new ZookeeperExportServiceRegister(
                rpcConfig.getRegisterAddress(),
                rpcConfig.getServerPort(),
                rpcConfig.getProtocol(),
                rpcConfig.getWeight());
    }

    @Bean
    public RequestHandler requestHandler(@Autowired ServiceRegister serviceRegister,
                                         @Autowired RpcConfig rpcConfig) {
        return new RequestHandler(getMessageProtocol(rpcConfig.getProtocol()), serviceRegister);
    }

    private MessageProtocol getMessageProtocol(String name) {
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        for (MessageProtocol messageProtocol : loader) {
            MessageSerializationMethod ano = messageProtocol.getClass().getAnnotation(MessageSerializationMethod.class);
            Assert.notNull(ano, "message protocol name can not be empty!");
            if (name.equals(ano.value())) {
                return messageProtocol;
            }
        }
        throw new RpcException("invalid message protocol config!");
    }

    @Bean
    public RpcServer rpcServer(@Autowired RequestHandler requestHandler, @Autowired RpcConfig rpcConfig) {
        return new NettyRpcServer(rpcConfig.getServerPort(), rpcConfig.getProtocol(), requestHandler);
    }

    @Bean
    public DefaultRpcProcessor rpcProcessor(@Autowired ClientProxyFactory clientProxyFactory,
                                            @Autowired ServiceRegister serviceRegister,
                                            @Autowired RpcServer rpcServer) {
        return new DefaultRpcProcessor(clientProxyFactory, serviceRegister, rpcServer);
    }

    @Bean
    public ClientProxyFactory proxyFactory(@Autowired RpcConfig rpcConfig) {
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        // 设置服务发现者
        clientProxyFactory.setServiceDiscoverer(new ZookeeperServiceDiscoverer(rpcConfig.getRegisterAddress()));

        // 设置支持的协议
        Map<String, MessageProtocol> supportMessageProtocols = buildSupportMessageProtocols();
        clientProxyFactory.setSupportMessageProtocols(supportMessageProtocols);
        // 设置负载均衡算法
        LoadBalance loadBalance = getLoadBalance(rpcConfig.getLoadBalance());
        clientProxyFactory.setLoadBalance(loadBalance);
        // 设置网络层实现
        clientProxyFactory.setNetClient(new NettyNetClient());

        return clientProxyFactory;
    }

    /**
     * 使用 spi 匹配符合配置的负载均衡算法
     */
    private LoadBalance getLoadBalance(String name) {
        ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
        for (LoadBalance loadBalance : loader) {
            LoadBalanceStrategy strategy = loadBalance.getClass().getAnnotation(LoadBalanceStrategy.class);
            Assert.notNull(strategy, "load balance name can not be empty!");
            if (name.equals(strategy.value())) {
                return loadBalance;
            }
        }
        throw new RpcException("Invalid load balance configuration.");
    }

    private Map<String, MessageProtocol> buildSupportMessageProtocols() {
        Map<String, MessageProtocol> supportMessageProtocols = new HashMap<>();
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        for (MessageProtocol messageProtocol : loader) {
            MessageSerializationMethod ano = messageProtocol.getClass().getAnnotation(MessageSerializationMethod.class);
            Assert.notNull(ano, "message protocol name can not be empty!");
            supportMessageProtocols.put(ano.value(), messageProtocol);
        }
        return supportMessageProtocols;
    }
}
