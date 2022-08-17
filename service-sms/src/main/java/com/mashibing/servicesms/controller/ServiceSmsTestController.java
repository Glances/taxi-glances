package com.mashibing.servicesms.controller;

import com.mashibing.servicesms.yml.MyYml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test")
public class ServiceSmsTestController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/sms-test")
    public String test(){
        System.out.println("sms-test");
        return "sms-test:"+port;
    }

    @GetMapping("/sms-test2")
    public String test2(){

        return "sms-test2:"+port;
    }

    @Autowired
    private MyYml myYml;

    @GetMapping("/sms-test3")
    public String test3(){

        return "sms-test3:"+port+myYml.getDizhi();
    }


    @GetMapping("/sms-test4")
    public String test4(HttpServletRequest request){

        String token = request.getHeader("token");

        return "sms-test4:"+port+":"+token;
    }

    // 服务提供方 获取 Cookie，并返回到页面
    @GetMapping("/sms-test5")
    public String test5(HttpServletRequest request){
//        int i = 1/0;
        String token = request.getHeader("Cookie");
        System.out.println("服务提供方：收到的Cookie："+token);
        return "sms-test5:"+port+": token："+token;
    }



    @GetMapping("/sms-test-feign")
    public String feign(){

        return "service-sms-feign:"+port;
    }

    @GetMapping("/sms-test-feign2")
    public String feign2(){

        return "service-sms-feign2:"+port;
    }
}
