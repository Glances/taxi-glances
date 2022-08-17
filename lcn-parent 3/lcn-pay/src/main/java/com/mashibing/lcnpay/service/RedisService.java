package com.mashibing.lcnpay.service;

import com.codingapi.txlcn.tc.annotation.TccTransaction;
import com.mashibing.lcnpay.entity.TblPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @TccTransaction
    public String addPay(@RequestBody TblPay bean){
        BoundValueOperations<String, String> pay = redisTemplate.boundValueOps("pay");
        pay.set("pay-value");
//        int i = 1/0;
        return "新增支付成功";

    }
    public String confirmAddPay(TblPay bean){
        System.out.println("pay confirm");
        return "新增支付成功";

    }
    private static Map<String,Integer> maps = new HashMap<>();

    /**
     * 逆sql
     * @param bean
     * @return
     */
    public String cancelAddPay(TblPay bean){

        redisTemplate.delete("pay");
        System.out.println("pay cancel");
        return "取消支付成功";

    }
}
