package me.project.rpc.client.cache;

import me.project.rpc.common.model.RpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务发现本地缓存
 */
public class ServiceDiscoveryCache {

    /**
     * key: serviceName
     */
    private static final Map<String, List<RpcService>> SERVER_MAP = new ConcurrentHashMap<>();
    /**
     * 客户端注入的远程服务 service class
     */
    public static final List<String> SERVICE_CLASS_NAMES = new ArrayList<>();

    public static void put(String serviceName, List<RpcService> serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }

    /**
     * 去除指定的值
     *
     * @param serviceName 服务名称
     * @param service     服务
     */
    public static void remove(String serviceName, RpcService service) {
        SERVER_MAP.computeIfPresent(serviceName, (key, value) ->
                value.stream().filter(o -> !o.equals(service)).collect(Collectors.toList())
        );
    }

    public static void removeAll(String serviceName) {
        SERVER_MAP.remove(serviceName);
    }

    public static boolean isEmpty(String serviceName) {
        return SERVER_MAP.get(serviceName) == null || SERVER_MAP.get(serviceName).size() == 0;
    }

    public static List<RpcService> getServiceList(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }
}
