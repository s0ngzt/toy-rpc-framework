package me.project.rpc.client.discovery;

import me.project.rpc.client.cache.ServiceDiscoveryCache;
import org.I0Itec.zkclient.IZkChildListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 子节点时间监听处理类
 */
public class ZkChildListener implements IZkChildListener {
    private static final Logger logger = LoggerFactory.getLogger(ZkChildListener.class);

    /**
     * 监听子节点的删除和新增事件
     *
     * @param parentPath /rpc/serviceName/service
     * @param childList  unused
     */
    @Override
    public void handleChildChange(String parentPath, List<String> childList) {
        logger.debug("Child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
        // 只要子节点有改动就清空缓存
        String[] arr = parentPath.split("/");
        ServiceDiscoveryCache.removeAll(arr[2]);
    }
}
