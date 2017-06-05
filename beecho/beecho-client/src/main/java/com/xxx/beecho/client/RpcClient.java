package com.xxx.beecho.client;

import com.xxx.beecho.common.bean.RpcRequest;
import com.xxx.beecho.common.bean.RpcResponse;
import com.xxx.beecho.common.codec.RpcDecoder;
import com.xxx.beecho.common.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.channels.*;

/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * Created by Administrator on 2017/6/5.
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private final String host;
    private final int port;

    private RpcResponse rpcResponse;

    public RpcClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.rpcResponse = rpcResponse;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建并初始化 Netty 客户端 Bootstrap 对象
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new RpcEncoder(RpcRequest.class)); // 编码 RPC 请求
                    pipeline.addLast(new RpcDecoder(RpcResponse.class)); // 解码 RPC 响应
                    pipeline.addLast(RpcClient.this); // 处理 RPC 响应
                }
            });

            bootstrap.option(ChannelOption.TCP_NODELAY,true);
            // 连接 RPC 服务端
            ChannelFuture future = bootstrap.connect(host,port).sync();
            // 写入 RPC 请求数据并关闭连接
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            // 返回 RPC 响应结果
            return rpcResponse;
        }finally {
            group.shutdownGracefully();
        }
    }
}
