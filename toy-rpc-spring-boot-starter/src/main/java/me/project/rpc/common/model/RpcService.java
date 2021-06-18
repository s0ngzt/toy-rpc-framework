package me.project.rpc.common.model;

import java.util.Objects;

/**
 * 服务信息，记录注册的服务信息
 */
public class RpcService {

    /**
     * 名称
     */
    private String name;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 地址 IP:Port
     */
    private String address;

    /**
     * 权重，从大到小
     */
    private Integer weight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "RpcService[" +
                "name='" + name + '\'' +
                ", protocol='" + protocol + '\'' +
                ", address='" + address + '\'' +
                ", weight=" + weight +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcService anotherService = (RpcService) o;
        return name.equals(anotherService.name) &&
                protocol.equals(anotherService.protocol) &&
                address.equals(anotherService.address) &&
                weight.equals(anotherService.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, protocol, address, weight);
    }
}
