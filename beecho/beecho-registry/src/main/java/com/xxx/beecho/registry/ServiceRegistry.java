package com.xxx.beecho.registry;

/**
 * 服务注册接口
 *
 * Created by Administrator on 2017/6/5.
 */
public interface ServiceRegistry {

    /**
     * 注册服务名称与服务地址
     * @param serviceName
     * @param serviceAddress
     */
    void register(String serviceName,String serviceAddress);
}
