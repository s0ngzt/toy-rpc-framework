package me.project.rpc.client.discovery;

import me.project.rpc.common.model.RpcService;

import java.util.List;

/**
 * 服务发现接口
 */
public interface ServiceDiscoverer {
    List<RpcService> getServices(String serviceName);
}
