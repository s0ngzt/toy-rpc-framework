package me.project.rpc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 参数配置类，实现用户自定义参数
 */
@EnableConfigurationProperties(RpcConfig.class)
@ConfigurationProperties("toy.rpc")
public class RpcConfig {

    /**
     * 服务注册中心
     */
    private String registerAddress;

    /**
     * 服务端暴露端口
     */
    private Integer serverPort;

    /**
     * 服务协议
     */
    private String protocol;

    /**
     * 负载均衡算法
     */
    private String loadBalance;

    /**
     * 权重，default to 1
     */
    private Integer weight;

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
