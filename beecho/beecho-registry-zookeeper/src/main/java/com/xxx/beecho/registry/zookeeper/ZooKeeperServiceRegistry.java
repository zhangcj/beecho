package com.xxx.beecho.registry.zookeeper;

import com.xxx.beecho.registry.ServiceRegistry;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 Zookeeper 实现服务注册接口的实现
 *
 * Created by Administrator on 2017/6/5.
 */
public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final ZkClient zkClient;

    public ZooKeeperServiceRegistry(String zkAddress){
        //创建 Zookeeper 客户端
        zkClient = new ZkClient(zkAddress,Constant.ZK_SESSION_TIMEOUT,Constant.ZK_CONNECTION_TIMEOUT);
        System.out.println("connect zookeeper");
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        //创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;

        if(!zkClient.exists(registryPath)){
            zkClient.createPersistent(registryPath);
        }

        //创建 service 节点（持久）
        String servicePath = registryPath+"/"+serviceName;
        if(!zkClient.exists(servicePath)){
            zkClient.createPersistent(servicePath);
        }

        //创建 address 节点（临时）
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath,serviceAddress);
    }
}
