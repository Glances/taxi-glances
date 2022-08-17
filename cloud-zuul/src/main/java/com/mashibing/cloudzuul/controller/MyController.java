package com.mashibing.cloudzuul.controller;

import com.mashibing.cloudzuul.service.SentinelService;
import com.mashibing.cloudzuul.yml.MyYml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author 马士兵教育:晁鹏飞
 * @date
 */
@RestController
public class MyController {

    @Autowired
    private MyYml myYml;

//    @Autowired
//    private SentinelService sentinelService;

    private static int count = 0;

    @GetMapping("/myController")
    public String myForward(){
//        System.out.println("my controller"+count++);
//        return sentinelService.call();
        return "my controller : "+myYml.getDizhi();
    }
}
