package me.project.rpc.server;

import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;
import me.project.rpc.common.model.RpcStatusEnum;
import me.project.rpc.common.protocol.MessageProtocol;
import me.project.rpc.server.register.ServiceObject;
import me.project.rpc.server.register.ServiceRegister;

import java.lang.reflect.Method;

/**
 * 请求处理
 */
public class RequestHandler {

    private MessageProtocol protocol;

    private ServiceRegister serviceRegister;

    public RequestHandler(MessageProtocol protocol, ServiceRegister serviceRegister) {
        this.protocol = protocol;
        this.serviceRegister = serviceRegister;
    }


    public byte[] handleRequest(byte[] data) throws Exception {
        // 1.解组消息
        RpcRequest req = this.protocol.unmarshallingRequest(data);

        // 2.查找服务对应
        ServiceObject so = serviceRegister.getServiceObject(req.getServiceName());
        RpcResponse response;

        if (so == null) {
            response = new RpcResponse(RpcStatusEnum.NOT_FOUND);
        } else {
            try {
                // 3.反射调用对应的方法过程
                Method method = so.getClazz().getMethod(req.getMethod(), req.getParameterTypes());
                Object returnValue = method.invoke(so.getObj(), req.getParameters());
                response = new RpcResponse(RpcStatusEnum.SUCCESS);
                response.setReturnValue(returnValue);
            } catch (Exception e) {
                response = new RpcResponse(RpcStatusEnum.ERROR);
                response.setException(e);
            }
        }
        // 编组响应消息
        response.setRequestId(req.getRequestId());
        return this.protocol.marshallingResponse(response);
    }


    public MessageProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(MessageProtocol protocol) {
        this.protocol = protocol;
    }
}
