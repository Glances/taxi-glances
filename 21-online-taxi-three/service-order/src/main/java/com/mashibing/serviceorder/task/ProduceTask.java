package com.mashibing.serviceorder.task;

import com.mashibing.serviceorder.dao.TblOrderEventDao;
import com.mashibing.serviceorder.entity.TblOrderEvent;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.List;

/**
 * @author 马士兵教育:chaopengfei
 * @date 2020/7/30
 */
@Component
public class ProduceTask {

    @Autowired
    private TblOrderEventDao tblOrderEventDao;

    @Autowired
    private Queue queue;

    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    @Scheduled(cron="0/5 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void task(){
        System.out.println("定时任务");

        List<TblOrderEvent> tblOrderEventList = tblOrderEventDao.selectByOrderType("1");
        for (int i = 0; i < tblOrderEventList.size(); i++) {
            TblOrderEvent event = tblOrderEventList.get(i);

            // 更改这条数据的orderType为2
            tblOrderEventDao.updateEvent(event.getOrderType());
            System.out.println("修改数据库完成");

            jmsMessagingTemplate.convertAndSend(queue,JSONObject.fromObject(event).toString());

        }


    }

}
