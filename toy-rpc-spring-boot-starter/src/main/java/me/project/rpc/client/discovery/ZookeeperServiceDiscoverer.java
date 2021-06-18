package me.project.rpc.client.discovery;

import com.alibaba.fastjson.JSON;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.serializer.ZookeeperSerializer;
import me.project.rpc.common.model.RpcService;
import org.I0Itec.zkclient.ZkClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 服务发现实现，使用 zookeeper
 */
public class ZookeeperServiceDiscoverer implements ServiceDiscoverer {

    private final ZkClient zkClient;

    public ZookeeperServiceDiscoverer(String zkAddress) {
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
    }

    /**
     * 使用 zookeeper 客户端，通过服务名（接口完整路径）获取服务列表
     *
     * @param name 服务名
     * @return 服务列表
     */
    @Override
    public List<RpcService> getServices(String name) {
        String servicePath = RpcConstant.ZK_SERVICE_PATH + "/" + name + "/service";
        List<String> children = zkClient.getChildren(servicePath);
        return Optional.ofNullable(children).orElse(new ArrayList<>()).stream().map(
                str -> {
                    String decodedChild = URLDecoder.decode(str, StandardCharsets.UTF_8);
                    return JSON.parseObject(decodedChild, RpcService.class);
                }).collect(Collectors.toList());
    }

    public ZkClient getZkClient() {
        return zkClient;
    }
}
