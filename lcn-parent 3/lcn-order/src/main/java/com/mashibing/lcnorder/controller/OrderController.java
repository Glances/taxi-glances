package com.mashibing.lcnorder.controller;

import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.mashibing.lcnorder.dao.TblOrderDao;
import com.mashibing.lcnorder.entity.TblOrder;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {

    @Autowired
    private TblOrderDao tblOrderDao;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test")
    public String test(){
        return "hello world";
    }

    @PostMapping("/add-order")
    @Transactional(rollbackFor = Exception.class)
    @LcnTransaction
    public String add(@RequestBody TblOrder bean){

        JSONObject date = new JSONObject();
        date.put("payName",bean.getOrderName()+"pay");

        restTemplate.postForEntity("http://lcn-pay/add-pay",date,String.class);
        int i = 1/0;
        tblOrderDao.insert(bean);
        return "新增订单成功";
    }
}
