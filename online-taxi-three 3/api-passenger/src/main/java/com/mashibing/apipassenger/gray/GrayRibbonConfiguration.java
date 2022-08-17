package com.mashibing.apipassenger.gray;

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 马士兵教育:晁鹏飞
 * @date
 */
public class GrayRibbonConfiguration {

    @Bean
    public IRule ribbonRule(){
        return new GrayRule();
    }

}
