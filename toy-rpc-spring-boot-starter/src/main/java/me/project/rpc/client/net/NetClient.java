package me.project.rpc.client.net;

import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;
import me.project.rpc.common.model.RpcService;
import me.project.rpc.common.protocol.MessageProtocol;

/**
 * 网络请求客户端接口
 */
public interface NetClient {

    byte[] sendRequest(byte[] data, RpcService rpcService) throws InterruptedException;

    RpcResponse sendRequest(RpcRequest request, RpcService service, MessageProtocol messageProtocol);
}
