package me.project.rpc.common.constants;

public class RpcConstant {

    /**
     * Zookeeper服务注册地址
     */
    public static final String ZK_SERVICE_PATH = "/leisure-toy.rpc";

    /**
     * java序列化协议
     */
    public static final String PROTOCOL_JAVA = "java";

    /**
     * protobuf序列化协议
     */
    public static final String PROTOCOL_PROTOSTUFF = "protostuff";

    /**
     * kryo序列化协议
     */
    public static final String PROTOCOL_KRYO = "kryo";

    /**
     * 随机
     */
    public static final String BALANCE_RANDOM = "random";

    /**
     * 轮询
     */
    public static final String BALANCE_ROUND = "round";

    /**
     * 加权轮询
     */
    public static final String BALANCE_WEIGHTED_ROUND = "weighted-round";

    /**
     * 平滑加权轮询
     */
    public static final String BALANCE_SMOOTH_WEIGHTED_ROUND = "smooth-weighted-round";
}
