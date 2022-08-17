package com.mashibing.lcnorder.controller;

import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import com.codingapi.txlcn.tc.annotation.TccTransaction;
import com.mashibing.lcnorder.dao.TblOrderDao;
import com.mashibing.lcnorder.entity.TblOrder;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderTccRedisController {

    @Autowired
    private TblOrderDao tblOrderDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @PostMapping("/add-order-tcc-redis")
    @Transactional(rollbackFor = Exception.class)
    @LcnTransaction
    public String add(@RequestBody TblOrder bean){

        JSONObject date = new JSONObject();
        date.put("payName",bean.getOrderName()+"pay");
        restTemplate.postForEntity("http://lcn-pay/add-pay-tcc-redis",date,String.class);

        TblOrder tblOrder = new TblOrder();
        tblOrder.setId(1);
        tblOrder.setOrderName("新");

//        BoundValueOperations<String, String> order = redisTemplate.boundValueOps("order");
//        order.set("order-value");

        tblOrderDao.updateByPrimaryKey(tblOrder);
//        int i = 1/0;
        return "新增订单成功";
    }

//    public String confirmAdd(TblOrder bean){
//        System.out.println("add 确认线程名："+Thread.currentThread().getName());
//        System.out.println("order confirm ");
//        return "新增订单成功";
//    }
//
////    private static Map<String,Integer> maps = new HashMap<>();
//
//    private ThreadLocal<Integer> ids = new ThreadLocal<>();
//
//    public String cancelAdd(TblOrder bean){
//        System.out.println("add 取消线程名："+Thread.currentThread().getName());
//        TblOrder tblOrder = new TblOrder();
//        tblOrder.setId(1);
//        tblOrder.setOrderName("旧");
//
//        tblOrderDao.updateByPrimaryKey(tblOrder);
//
//        redisTemplate.delete("order");
//        System.out.println("order cancel ");
//        return "新增订单成功";
//    }
}
