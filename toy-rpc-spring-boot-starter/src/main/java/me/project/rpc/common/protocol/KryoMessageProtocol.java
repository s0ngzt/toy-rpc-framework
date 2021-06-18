package me.project.rpc.common.protocol;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import me.project.rpc.annotation.MessageSerializationMethod;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.common.model.RpcRequest;
import me.project.rpc.common.model.RpcResponse;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@MessageSerializationMethod(RpcConstant.PROTOCOL_KRYO)
public class KryoMessageProtocol implements MessageProtocol {

    private static final ThreadLocal<Kryo> kryoLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        DefaultInstantiatorStrategy strategy = (DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
        strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    /**
     * 获得当前线程的 Kryo 实例
     *
     * @return 当前线程的 Kryo 实例
     */
    public static Kryo getInstance() {
        return kryoLocal.get();
    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) {
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        Output output = new Output(bout);
//        Kryo kryo = getInstance();
//        kryo.writeClassAndObject(output, request);
//        byte[] bytes = output.toBytes();
//        output.flush();
//        return bytes;
        return marshalling(request);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) {
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        Output output = new Output(bout);
//        Kryo kryo = getInstance();
//        kryo.writeClassAndObject(output, response);
//        byte[] bytes = output.toBytes();
//        output.flush();
//        return bytes;
        return marshalling(response);
    }

    private static <T> byte[] marshalling(T message) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Output output = new Output(bout);
        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, message);
        byte[] bytes = output.toBytes();
        output.flush();
        return bytes;
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) {
//        ByteArrayInputStream bin = new ByteArrayInputStream(data);
//        Input input = new Input(bin);
//        Kryo kryo = getInstance();
//        RpcRequest request = (RpcRequest) kryo.readClassAndObject(input);
//        input.close();
//        return request;
        return unmarshalling(data);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) {
//        ByteArrayInputStream bin = new ByteArrayInputStream(data);
//        Input input = new Input(bin);
//        Kryo kryo = getInstance();
//        RpcResponse response = (RpcResponse) kryo.readClassAndObject(input);
//        input.close();
//        return response;
        return unmarshalling(data);
    }

    private static <T> T unmarshalling(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Input input = new Input(bin);
        Kryo kryo = getInstance();
        T message = (T) kryo.readClassAndObject(input);
        input.close();
        return message;
    }
}
