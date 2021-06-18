package me.project.rpc.client;

import me.project.rpc.client.balance.LoadBalance;
import me.project.rpc.client.cache.ServiceDiscoveryCache;
import me.project.rpc.client.discovery.ServiceDiscoverer;
import me.project.rpc.client.net.NetClient;
import me.project.rpc.common.protocol.MessageProtocol;
import me.project.rpc.common.model.RpcService;
import me.project.rpc.common.exception.RpcException;
import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * 客户端代理工厂，用于创建远程服务代理类
 * 封装编组请求、请求发送、编组响应等操作
 */
public class ClientProxyFactory {

    private ServiceDiscoverer serviceDiscoverer;

    private Map<String, MessageProtocol> supportedMessageProtocols;

    private NetClient netClient;

    private final Map<Class<?>, Object> objectCache = new HashMap<>();

    private LoadBalance loadBalance;

    /**
     * 通过 Java 动态代理获取代理服务类
     *
     * @param clazz 被代理类 Class
     * @param <T>   泛型参数
     * @return 服务代理类
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) this.objectCache.computeIfAbsent(clazz,
                cls -> newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new ClientInvocationHandler(cls)));
    }

    /**
     * 客户端服务代理类 invoke() 函数细节实现
     */
    private class ClientInvocationHandler implements InvocationHandler {

        private final Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {

            if (method.getName().equals("toString")) return proxy.getClass().toString();
            if (method.getName().equals("hashCode")) return 0;

            // 1. 获取服务信息
            String serviceName = this.clazz.getName();
            List<RpcService> rpcServices = serviceDiscoverer.getServices(serviceName);
            if (rpcServices == null || rpcServices.isEmpty()) throw new RpcException("No provider available!");
            var rpcService = loadBalance.chooseOne(rpcServices);

            // 2. 构造 request 对象
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setServiceName(rpcService.getName());
            request.setMethod(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);

            // 3. 协议层编组
            // 获得方法对应的协议
            MessageProtocol protocol = supportedMessageProtocols.get(rpcService.getProtocol());
            // 编组
            // byte[] requestData = protocol.marshallingRequest(request);

            // 4. 发送请求
            // byte[] responseData = netClient.sendRequest(requestData, rpcService);

            // 5. 解组响应消息
            // RpcResponse response = protocol.unmarshallingResponse(responseData);
            RpcResponse response = netClient.sendRequest(request, rpcService, protocol);
            if (response == null) throw new RpcException("the response is null.");

            // 6. 处理结果
            if (response.getException() != null) {
                throw response.getException();
            }
            return response.getReturnValue();
        }
    }

    /**
     * 根据服务名获取可用的服务地址列表
     *
     * @param serviceName 服务名称
     * @return 服务列表
     */
    private synchronized List<RpcService> getServiceList(String serviceName) {
        List<RpcService> services;
        // TODO
        synchronized (serviceName) {
            if (ServiceDiscoveryCache.isEmpty(serviceName)) {
                services = serviceDiscoverer.getServices(serviceName);
                if (services == null || services.size() == 0) {
                    throw new RpcException("No provider available!");
                }
                ServiceDiscoveryCache.put(serviceName, services);
            } else {
                services = ServiceDiscoveryCache.getServiceList(serviceName);
            }
        }
        return services;
    }

    public void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        this.supportedMessageProtocols = supportMessageProtocols;
    }

    public void setNetClient(NetClient netClient) {
        this.netClient = netClient;
    }

    public void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer) {
        this.serviceDiscoverer = serviceDiscoverer;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public ServiceDiscoverer getServiceDiscoverer() {
        return serviceDiscoverer;
    }
}
