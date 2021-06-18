package me.project.rpc.server.register;

import com.alibaba.fastjson.JSON;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcService;
import me.project.rpc.common.serializer.ZookeeperSerializer;
import org.I0Itec.zkclient.ZkClient;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Zookeeper 服务注册器，提供服务注册、服务暴露的能力
 */
public class ZookeeperExportServiceRegister extends DefaultServiceRegister {

    private final ZkClient zkClient;

    public ZookeeperExportServiceRegister(String zkAddress, Integer port, String protocol, Integer weight) {
        zkClient = new ZkClient(zkAddress);
        zkClient.setZkSerializer(new ZookeeperSerializer());
        this.port = port;
        this.protocol = protocol;
        this.weight = weight;
    }

    /**
     * 服务注册
     *
     * @param serviceObject 服务持有者
     * @throws Exception 注册异常
     */
    @Override
    public void register(ServiceObject serviceObject) throws Exception {
        super.register(serviceObject);
        RpcService rpcService = new RpcService();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + port;
        rpcService.setAddress(address);
        rpcService.setName(serviceObject.getClazz().getName());
        rpcService.setProtocol(protocol);
        rpcService.setWeight(weight);
        this.exportService(rpcService);
    }

    /**
     * 服务暴露
     *
     * @param rpcServiceResource 需要暴露的服务信息
     */
    private void exportService(RpcService rpcServiceResource) {
        String serviceName = rpcServiceResource.getName();
        String uri = JSON.toJSONString(rpcServiceResource);
        uri = URLEncoder.encode(uri, StandardCharsets.UTF_8);

        String servicePath = RpcConstant.ZK_SERVICE_PATH + "/" + serviceName + "/service";
        if (!zkClient.exists(servicePath)) {
            // 没有节点即创建
            zkClient.createPersistent(servicePath, true);
        }
        String uriPath = servicePath + "/" + uri;
        if (zkClient.exists(uriPath)) {
            // 删除之前的节点
            zkClient.delete(uriPath);
        }
        // 创建一个临时节点，会话失效即被清理
        zkClient.createEphemeral(uriPath);
    }
}
