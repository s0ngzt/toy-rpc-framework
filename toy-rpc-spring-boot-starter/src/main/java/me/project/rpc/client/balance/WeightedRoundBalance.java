package me.project.rpc.client.balance;

import me.project.rpc.annotation.LoadBalanceStrategy;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcService;

import java.util.List;

/**
 * 加权轮询
 */
@LoadBalanceStrategy(RpcConstant.BALANCE_WEIGHTED_ROUND)
public class WeightedRoundBalance implements LoadBalance {

    private static int index;

    @Override
    public synchronized RpcService chooseOne(List<RpcService> services) {
        int allWeightSum = services.stream().mapToInt(RpcService::getWeight).sum();
        int number = (index++) % allWeightSum;
        for (RpcService service : services) {
            if (service.getWeight() > number) {
                return service;
            }
            number -= service.getWeight();
        }
        return null;
    }
}
