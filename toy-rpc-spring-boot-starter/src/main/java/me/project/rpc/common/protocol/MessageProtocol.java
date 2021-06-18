package me.project.rpc.common.protocol;

import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;

/**
 * 消息协议，定义编组请求、解组请求、编组响应、解组响应规范
 */
public interface MessageProtocol {

    /**
     * 编组请求
     *
     * @param request 请求消息
     * @return 请求字节数组
     * @throws Exception 编组异常
     */
    byte[] marshallingRequest(RpcRequest request) throws Exception;

    /**
     * 解组请求
     *
     * @param data 请求字节数组
     * @return 请求消息
     * @throws Exception 解组异常
     */
    RpcRequest unmarshallingRequest(byte[] data) throws Exception;

    /**
     * 编组响应
     *
     * @param response 响应消息
     * @return 响应字节数组
     * @throws Exception 编组异常
     */
    byte[] marshallingResponse(RpcResponse response) throws Exception;

    /**
     * 解组响应
     *
     * @param data 响应字节数组
     * @return 响应消息
     * @throws Exception 解组异常
     */
    RpcResponse unmarshallingResponse(byte[] data) throws Exception;
}
