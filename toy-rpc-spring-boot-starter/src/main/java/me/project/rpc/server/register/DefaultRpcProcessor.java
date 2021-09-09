package me.project.rpc.server.register;

import me.project.rpc.annotation.RpcServiceConsumer;
import me.project.rpc.annotation.RpcServiceProvider;
import me.project.rpc.client.ClientProxyFactory;
import me.project.rpc.client.cache.ServiceDiscoveryCache;
import me.project.rpc.client.discovery.ZkChildListener;
import me.project.rpc.client.discovery.ZookeeperServiceDiscoverer;
import me.project.rpc.common.constants.RpcConstant;
import me.project.rpc.server.RpcServer;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * RPC 处理器，支持服务自动暴露、自动注入 service
 */
public class DefaultRpcProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcProcessor.class);

    private final ClientProxyFactory clientProxyFactory;

    private final ServiceRegister serviceRegister;

    private final RpcServer rpcServer;

    public DefaultRpcProcessor(ClientProxyFactory clientProxyFactory, ServiceRegister serviceRegister, RpcServer rpcServer) {
        this.clientProxyFactory = clientProxyFactory;
        this.serviceRegister = serviceRegister;
        this.rpcServer = rpcServer;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // ApplicationContext 被初始化或刷新时，该事件被发布。
        if (Objects.isNull(event.getApplicationContext().getParent())) {
            ApplicationContext context = event.getApplicationContext();
            // 开启服务
            startServer(context);
            // 注入 service
            injectService(context);
        }
    }

    private void startServer(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(RpcServiceProvider.class);
        if (beans.size() > 0) {
            boolean startServerFlag = true;

            for (Object obj : beans.values()) {
                try {
                    Class<?> clazz = obj.getClass();
                    Class<?>[] interfaces = clazz.getInterfaces();
                    ServiceObject so;
                    // 如果只实现了一个接口就用父类的 className 作为服务名
                    // 如果该类实现了多个接口，则用注解里的 value 作为服务名
                    if (interfaces.length != 1) {
                        RpcServiceProvider service = clazz.getAnnotation(RpcServiceProvider.class);
                        String value = service.value();
                        if (value.equals("")) {
                            startServerFlag = false;
                            throw new UnsupportedOperationException("The exposed interface is not specific with '" + obj.getClass().getName() + "'");
                        }
                        so = new ServiceObject(value, Class.forName(value), obj);
                    } else {
                        Class<?> supperClass = interfaces[0];
                        so = new ServiceObject(supperClass.getName(), supperClass, obj);
                    }
                    serviceRegister.register(so);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 一个服务器启动一个 NettyServer
            if (startServerFlag) {
                rpcServer.start();
            }
        }
    }

    private void injectService(ApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)) {
                continue;
            }

            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                // 找出标记了 InjectService 注解的属性
                RpcServiceConsumer injectService = field.getAnnotation(RpcServiceConsumer.class);
                if (injectService == null) {
                    continue;
                }

                Class<?> fieldClass = field.getType();
                Object object = context.getBean(name);
                field.setAccessible(true);
                try {
                    field.set(object, clientProxyFactory.getProxy(fieldClass));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ServiceDiscoveryCache.SERVICE_CLASS_NAMES.add(fieldClass.getName());
            }
        }
        // 注册子节点监听
        if (clientProxyFactory.getServiceDiscoverer() instanceof ZookeeperServiceDiscoverer) {
            var serverDiscovery = (ZookeeperServiceDiscoverer) clientProxyFactory.getServiceDiscoverer();
            ZkClient zkClient = serverDiscovery.getZkClient();
            ServiceDiscoveryCache.SERVICE_CLASS_NAMES.forEach(name -> {
                String servicePath = RpcConstant.ZK_SERVICE_PATH + "/" + name + "/service";
                zkClient.subscribeChildChanges(servicePath, new ZkChildListener());
            });
            logger.info("subscribe service zk node successfully");
        }
    }
}
