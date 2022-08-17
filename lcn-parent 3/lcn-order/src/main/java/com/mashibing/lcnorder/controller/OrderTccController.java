package com.mashibing.lcnorder.controller;

import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.codingapi.txlcn.tc.annotation.TccTransaction;
import com.mashibing.lcnorder.dao.TblOrderDao;
import com.mashibing.lcnorder.entity.TblOrder;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderTccController {

    @Autowired
    private TblOrderDao tblOrderDao;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/add-order-tcc")
    @Transactional(rollbackFor = Exception.class)
    @TccTransaction
    public String add(@RequestBody TblOrder bean){
        System.out.println("add 正常线程名："+Thread.currentThread().getName());
        JSONObject date = new JSONObject();
        date.put("payName",bean.getOrderName()+"pay");

        restTemplate.postForEntity("http://lcn-pay/add-pay-tcc",date,String.class);

        tblOrderDao.insert(bean);
        Integer id = bean.getId();
//        maps.put("a",id);
        ids.set(id);
//        int i = 1/0;
        return "新增订单成功";
    }

    public String confirmAdd(TblOrder bean){
        System.out.println("add 确认线程名："+Thread.currentThread().getName());
        System.out.println("order confirm ");
        return "新增订单成功";
    }

//    private static Map<String,Integer> maps = new HashMap<>();

    private ThreadLocal<Integer> ids = new ThreadLocal<>();

    public String cancelAdd(TblOrder bean){
        System.out.println("add 取消线程名："+Thread.currentThread().getName());
//        Integer a = maps.get("a");
        Integer a = ids.get();
        System.out.println("a:"+a);
        tblOrderDao.deleteByPrimaryKey(a);
        System.out.println("order cancel ");
        return "新增订单成功";
    }
}
