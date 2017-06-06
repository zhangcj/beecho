package com.xxx.beecho.example.client;

import com.xxx.beecho.client.RpcProxy;
import com.xxx.beecho.example.api.EchoService;
import com.xxx.beecho.example.api.Person;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2017/6/6.
 */
public class EchoClient {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        EchoService echoService1 = rpcProxy.create(EchoService.class);
        String result1 = echoService1.echo("World");
        System.out.println(result1);


        Person p = new Person("张","春举");

        EchoService echoService2 = rpcProxy.create(EchoService.class, "echo2");
        String result2 = echoService2.echo(p);
        System.out.println(result2);

        System.exit(0);
    }
}
