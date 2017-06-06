package com.xxx.beecho.example.server;

import com.xxx.beecho.example.api.EchoService;
import com.xxx.beecho.example.api.Person;
import com.xxx.beecho.server.Echo;

/**
 * Created by Administrator on 2017/6/6.
 */
@Echo(value = EchoService.class,version = "echo2")
public class EchoServiceImpl_v2 implements EchoService {

    @Override
    public String echo(String s) {
        return "echo : " + s;
    }

    @Override
    public String echo(Person person) {
        return "echo : "+person.getFirstName()+" "+person.getLastName();
    }
}
