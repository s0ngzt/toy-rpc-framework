package me.project.rpc.server.register;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认服务注册器
 */
public class DefaultServiceRegister implements ServiceRegister {

    private final Map<String, ServiceObject> serviceMap = new HashMap<>();

    protected String protocol;

    protected Integer port;

    /**
     * 权重
     */
    protected Integer weight;

    @Override
    public void register(ServiceObject serviceObject) throws Exception {
        if (serviceObject == null) {
            throw new IllegalArgumentException("Parameter cannot be empty!");
        }
        this.serviceMap.put(serviceObject.getName(), serviceObject);
    }

    @Override
    public ServiceObject getServiceObject(String name) {
        return serviceMap.get(name);
    }
}
