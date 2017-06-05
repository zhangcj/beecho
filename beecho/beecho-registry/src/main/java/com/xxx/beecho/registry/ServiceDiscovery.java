package com.xxx.beecho.registry;

/**
 *服务发现接口
 *
 * Created by Administrator on 2017/6/5.
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称获取服务地址
     * @param serviceName
     * @return
     */
    String discover(String serviceName);
}
