package me.project.rpc.client.balance;

import me.project.rpc.annotation.LoadBalanceStrategy;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcService;

import java.util.List;
import java.util.Random;

/**
 * 随机选择
 */
@LoadBalanceStrategy(RpcConstant.BALANCE_RANDOM)
public class RandomBalance implements LoadBalance {

    private static final Random random = new Random();

    @Override
    public RpcService chooseOne(List<RpcService> services) {
        return services.get(random.nextInt(services.size()));
    }
}
