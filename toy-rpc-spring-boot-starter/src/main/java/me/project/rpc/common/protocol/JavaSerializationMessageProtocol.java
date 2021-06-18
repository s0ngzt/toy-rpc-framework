package me.project.rpc.common.protocol;

import me.project.rpc.annotation.MessageSerializationMethod;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 消息协议（Java 序列化实现，性能较差）
 */
@MessageSerializationMethod(RpcConstant.PROTOCOL_JAVA)
public class JavaSerializationMessageProtocol implements MessageProtocol {

    private byte[] serialize(Object obj) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(obj);
        return bout.toByteArray();
    }

    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return this.serialize(request);
    }

    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcRequest) in.readObject();
    }

    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return this.serialize(response);
    }

    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        return (RpcResponse) in.readObject();
    }
}
