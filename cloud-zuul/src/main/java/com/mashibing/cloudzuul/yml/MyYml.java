package com.mashibing.cloudzuul.yml;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author 马士兵教育:晁鹏飞
 * @date
 */
@Component
@PropertySource(value = {"classpath:application-my.yml"})
@ConfigurationProperties(prefix = "mashibing")
@Data
public class MyYml {

    @Value("${dizhi}")
    private String dizhi;
}
