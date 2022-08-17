package com.mashibing.servicepay.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.Queue;


/**
 * 消息消费端 的配置
 * @author 马士兵教育:chaopengfei
 * @date 2020/7/30
 */
@Configuration
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    /**
     * 连接工厂
     * @param redeliveryPolicy
     * @return
     */
    @Bean
    public ActiveMQConnectionFactory connectionFactory(RedeliveryPolicy redeliveryPolicy){
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("admin","admin",brokerUrl);
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        return activeMQConnectionFactory;
    }

    /**
     * 重发配置
     * @return
     */
    @Bean
    public RedeliveryPolicy redeliveryPolicy(){
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        return redeliveryPolicy;
    }

    /**
     * 设置消息队列 确认机制
     * @param activeMQConnectionFactory
     * @return
     */
    @Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory){
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setConnectionFactory(activeMQConnectionFactory);
        // 1: 自动确认，2： 客户端手动确认，3：自动批量确认，4 事务提交并确认。
        bean.setSessionAcknowledgeMode(2);
        return bean;
    }

}