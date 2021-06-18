package me.project.rpc.client.balance;

import me.project.rpc.annotation.LoadBalanceStrategy;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平滑加权轮询
 */
@LoadBalanceStrategy(RpcConstant.BALANCE_SMOOTH_WEIGHTED_ROUND)
public class SmoothWeightedRoundBalance implements LoadBalance {

    /**
     * map (key: service, value: current weight)
     */
    private static final Map<String, Integer> map = new HashMap<>();

    @Override
    public synchronized RpcService chooseOne(List<RpcService> services) {
        services.forEach(service ->
                map.computeIfAbsent(service.toString(), key -> service.getWeight())
        );
        RpcService maxWeightServer = null;
        int allWeight = services.stream().mapToInt(RpcService::getWeight).sum();
        for (RpcService service : services) {
            Integer currentWeight = map.get(service.toString());
            if (maxWeightServer == null || currentWeight > map.get(maxWeightServer.toString())) {
                maxWeightServer = service;
            }
        }

        assert maxWeightServer != null;

        map.put(maxWeightServer.toString(), map.get(maxWeightServer.toString()) - allWeight);

        for (RpcService service : services) {
            Integer currentWeight = map.get(service.toString());
            map.put(service.toString(), currentWeight + service.getWeight());
        }
        return maxWeightServer;
    }
}
