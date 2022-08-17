package com.mashibing.lcnpay.controller;

import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.mashibing.lcnpay.dao.TblPayDao;
import com.mashibing.lcnpay.entity.TblPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayController {


    @Autowired
    private TblPayDao tblPayDao;

    @PostMapping("/add-pay")
    @Transactional(rollbackFor = Exception.class)
    @LcnTransaction
    public String addPay(@RequestBody TblPay bean){
        tblPayDao.insert(bean);
        return "新增支付成功";

    }
}
