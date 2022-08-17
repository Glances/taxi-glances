package com.mashibing.lcnpay.controller;

import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.codingapi.txlcn.tc.annotation.TccTransaction;
import com.mashibing.lcnpay.dao.TblPayDao;
import com.mashibing.lcnpay.entity.TblPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PayTccController {


    @Autowired
    private TblPayDao tblPayDao;

    @PostMapping("/add-pay-tcc")
    @Transactional(rollbackFor = Exception.class)
    @TccTransaction
    public String addPay(@RequestBody TblPay bean){
        tblPayDao.insert(bean);
        Integer id = bean.getId();
        maps.put("a",id);
        int i = 1/0;
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
        Integer a = maps.get("a");
        System.out.println("a:"+a);
        System.out.println("pay cancel");
        tblPayDao.deleteByPrimaryKey(a);
        return "取消支付成功";

    }
}
