package com.xxx.beecho.client;

import com.xxx.beecho.common.bean.RpcRequest;
import com.xxx.beecho.common.bean.RpcResponse;
import com.xxx.beecho.common.util.StringUtil;
import com.xxx.beecho.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 * Created by Administrator on 2017/6/5.
 */
public class RpcProxy {

    private String serviceAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    public <T> T create(final Class<?> interfaceClass,final String serviceVersion){
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象并设置请求属性
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setServiceVersion(serviceVersion);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        // 获取 RPC 服务地址
                        if(serviceDiscovery != null){
                            String serviceName = interfaceClass.getName();
                            if(StringUtil.isNotEmpty(serviceVersion)){
                                serviceName+="-"+serviceVersion;
                            }
                            serviceAddress = serviceDiscovery.discover(serviceName);
                        }
                        if(StringUtil.isEmpty(serviceAddress)){
                            throw new RuntimeException("server address is empty");
                        }

                        // 从 RPC 服务地址中解析出主机名和端口
                        String[] array = StringUtil.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        // 创建 RPC 客户端对象并发送 RPC 请求
                        RpcClient client = new RpcClient(host, port);
                        long time = System.currentTimeMillis();
                        RpcResponse response = client.send(request);

                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }

                        // 返回 RPC 响应结果
                        if(response.hasException()){
                            throw response.getException();
                        }else {
                           return response.getResult();
                        }
                    }
                }
        );
    }
}
