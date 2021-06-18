package me.project.rpc.server.register;

/**
 * 服务注册接口
 */
public interface ServiceRegister {

    void register(ServiceObject serviceObject) throws Exception;

    ServiceObject getServiceObject(String name) throws Exception;
}
