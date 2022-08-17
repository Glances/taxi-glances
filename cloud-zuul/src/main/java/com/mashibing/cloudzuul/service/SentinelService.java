package com.mashibing.cloudzuul.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.sun.deploy.security.BlockedException;
import org.springframework.stereotype.Service;

/**
 * @author 马士兵教育:晁鹏飞
 * @date
 */
//@Service
public class SentinelService {

    @SentinelResource(value = "resourceLimit1",blockHandler = "block")
    public String call(){
        System.out.println("go");
        return "go";
    }

    public String block(BlockedException ex){
        System.out.println("block");
        return "block";
    }
}
