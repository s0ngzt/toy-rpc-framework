package me.project.rpc.client.balance;

import me.project.rpc.common.model.RpcService;

import java.util.List;

/**
 * 负载均衡算法接口
 */
public interface LoadBalance {
    /**
     * @param services services 服务列表
     * @return 选择
     */
    RpcService chooseOne(List<RpcService> services);
}
