package me.project.rpc.client.balance;

import me.project.rpc.annotation.LoadBalanceStrategy;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcService;

import java.util.List;

/**
 * 轮询算法
 */
@LoadBalanceStrategy(RpcConstant.BALANCE_ROUND)
public class FullRoundBalance implements LoadBalance {

    private int index;

    @Override
    public synchronized RpcService chooseOne(List<RpcService> services) {
        // 加锁防止多线程情况下，index 超出 services.size()
        if (index == services.size()) {
            index = 0;
        }
        return services.get(index++);
    }
}
