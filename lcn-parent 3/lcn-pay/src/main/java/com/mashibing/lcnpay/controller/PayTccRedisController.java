package com.mashibing.lcnpay.controller;

import com.codingapi.txlcn.tc.annotation.TccTransaction;
import com.mashibing.lcnpay.dao.TblPayDao;
import com.mashibing.lcnpay.entity.TblPay;
import com.mashibing.lcnpay.service.RedisService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PayTccRedisController {


    @Autowired
    private TblPayDao tblPayDao;

    @Autowired
    private RedisService redisService;

    @PostMapping("/add-pay-tcc-redis")
    @Transactional(rollbackFor = Exception.class)
    public String addPay(@RequestBody TblPay bean){
        redisService.addPay(null);
        int i = 1/0;
        return "新增支付成功";

    }

}
