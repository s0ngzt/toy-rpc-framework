package me.project.rpc.common.protocol;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import me.project.rpc.annotation.MessageSerializationMethod;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;

@MessageSerializationMethod(RpcConstant.PROTOCOL_PROTOSTUFF)
public class ProtostuffMessageProtocol implements MessageProtocol {

    /**
     * 将目标类序列化为 byte 数组
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T source) {
        Schema<T> schema = RuntimeSchema.getSchema((Class<T>) source.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        final byte[] result;
        try {
            result = ProtobufIOUtil.toByteArray(source, schema, buffer);
        } finally {
            buffer.clear();
        }
        return result;
    }

    /**
     * 将byte数组序列化为目标类
     */
    public static <T> T deserialize(byte[] source, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(source, t, schema);
        return t;
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) {
        return serialize(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) {
        return deserialize(data, RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) {
        return serialize(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) {
        return deserialize(data, RpcResponse.class);
    }
}
