package me.project.rpc.client.cache;

import me.project.rpc.common.model.RpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务发现本地缓存
 */
public class ServiceDiscoveryCache {

    /**
     * 客户端注入的远程服务 service class
     */
    public static final List<String> SERVICE_CLASS_NAMES = new ArrayList<>();
    /**
     * key: serviceName
     */
    private static final Map<String, List<RpcService>> SERVER_MAP = new ConcurrentHashMap<>();

    public static void put(String serviceName, List<RpcService> serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }

    /**
     * 移除服务
     *
     * @param serviceName 服务名称
     */
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
