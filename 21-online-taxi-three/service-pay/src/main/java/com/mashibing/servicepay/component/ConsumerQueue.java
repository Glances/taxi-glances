package com.mashibing.servicepay.component;

import com.mashibing.servicepay.dao.TblOrderEventDao;
import com.mashibing.servicepay.entity.TblOrderEvent;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * @author 马士兵教育:chaopengfei
 * @date 2020/7/30
 */
@Component
public class ConsumerQueue {

    @Autowired
    private TblOrderEventDao tblOrderEventDao;

    @JmsListener(destination = "ActiveMQQueue",containerFactory = "jmsListenerContainerFactory")
    public void receive(TextMessage textMessage, Session session) throws JMSException {
        try {
            System.out.println("收到的消息："+textMessage.getText());
            String content = textMessage.getText();

            TblOrderEvent tblOrderEvent = (TblOrderEvent) JSONObject.toBean(JSONObject.fromObject(content),TblOrderEvent.class);
            tblOrderEventDao.insert(tblOrderEvent);
            // 业务完成，确认消息 消费成功
            textMessage.acknowledge();
        }catch (Exception e){
            // 回滚消息
            e.printStackTrace();
//            e.getMessage(); // 放到log中。
            System.out.println("异常了");
            session.recover();
        }

    }

    /**
     * 补偿 处理（人工，脚本）。自己根据自己情况。
     * @param text
     */
    @JmsListener(destination = "DLQ.ActiveMQQueue")
    public void receive2(String text){
        System.out.println("死信队列:"+text);
    }
}
