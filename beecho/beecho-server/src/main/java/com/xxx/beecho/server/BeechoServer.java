package com.xxx.beecho.server;

import com.xxx.beecho.common.bean.RpcRequest;
import com.xxx.beecho.common.bean.RpcResponse;
import com.xxx.beecho.common.codec.RpcDecoder;
import com.xxx.beecho.common.codec.RpcEncoder;
import com.xxx.beecho.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 服务端（用于发布RPC服务）
 * Created by Administrator on 2017/6/5.
 *
 * 加载Spring配置文件时，如果Spring配置文件中所定义的Bean类实现了ApplicationContextAware 接口，那么在加载Spring配置文件时，会自动调用ApplicationContextAware 接口中的 setApplicationContext
 *
 * 当一个类实现了这个接口（ApplicationContextAware）之后，这个类就可以方便获得ApplicationContext中的所有bean。换句话说，就是这个类可以直接获取spring配置文件中，所有有引用到的bean对象
 *
 * InitializingBean接口为bean提供了初始化方法的方式，它只包括afterPropertiesSet方法，凡是继承该接口的类，在初始化bean的时候会执行该方法
 */
public class BeechoServer implements InitializingBean,ApplicationContextAware {

    private String serviceAddress;
    private ServiceRegistry serviceRegistry;

    /*
    * 在 Spring 框架装载 Bean 之后开始存放 服务名 与 服务对象 之间的映射关系 起缓存作用
    * */
    private Map<String,Object> hadlerMap = new HashMap<>();

    public BeechoServer(String serviceAddress){
        this.serviceAddress = serviceAddress;
    }

    public BeechoServer(String serviceAddress,ServiceRegistry serviceRegistry){
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Spring 加载 Bean 后首先执行此方法
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //扫描带有 EchoService 注解的类，并初始化 handlerMap 对象
        Map<String,Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(EchoService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for (Object serviceBean : serviceBeanMap.values()){
                EchoService echoService = serviceBean.getClass().getAnnotation(EchoService.class);
                String serviceName = echoService.value().getName();
                String serviceVersion = echoService.version();
                if(com.xxx.beecho.common.util.StringUtil.isNotEmpty(serviceVersion)) {
                    serviceName += "-" + serviceVersion;
                }
                hadlerMap.put(serviceName,serviceBean);
            }
        }
    }

    /**
     * 装载 Bean 之后建立 Netty 服务端
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
         // 创建并初始化 Netty 服务端 Bootstrap 对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new RpcDecoder(RpcRequest.class)); // 解码 RPC 请求
                    pipeline.addLast(new RpcEncoder(RpcResponse.class)); // 解码 RPC 请求
                    pipeline.addLast(new RpcServerHandler(hadlerMap)); // 处理 RPC 请求
                }

            });

            bootstrap.option(ChannelOption.SO_BACKLOG,1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);
            // 获取 RPC 服务器的 IP 地址和端口号
            String[] addressArray = StringUtil.split(serviceAddress,':');
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);

            // 启动 RPC 服务地址
            ChannelFuture future = bootstrap.bind(ip,port).sync();
            // 注册 RPC 服务地址
            if(serviceRegistry != null){
                for (String interfaceName : hadlerMap.keySet()){
                    serviceRegistry.register(interfaceName,serviceAddress);
                }
            }

            // 关闭 RPC 服务器
            future.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
