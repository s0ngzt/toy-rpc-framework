package me.project.rpc.server.register;

/**
 * 服务持有对象，保存具体的服务信息
 */
public class ServiceObject {

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务 Class
     */
    private Class<?> clazz;

    /**
     * 具体服务
     */
    private Object obj;

    public ServiceObject(String name, Class<?> clazz, Object obj) {
        super();
        this.name = name;
        this.clazz = clazz;
        this.obj = obj;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getObj() {
        return obj;
    }
}
