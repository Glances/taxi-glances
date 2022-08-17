package com.mashibing.apipassenger.gray;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 马士兵教育:晁鹏飞
 * @date 2020/07/02
 */
public class GrayRule extends AbstractLoadBalancerRule {

    /**
     * 根据用户选出一个服务
     * @param iClientConfig
     * @return
     */
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {


    }

    @Override
    public Server choose(Object key) {

        return choose(getLoadBalancer(),key);
    }

    public Server choose(ILoadBalancer lb, Object key){

        System.out.println("灰度  rule");
        Server server = null;
        while (server == null){
            // 获取所有 可达的服务
            List<Server> reachableServers = lb.getReachableServers();

            // 获取 当前线程的参数 用户id verion=1
            Map<String,String> map = RibbonParameters.get();
            String version = "";
            if (map != null && map.containsKey("version")){
                version = map.get("version");
            }
            System.out.println("当前rule version:"+version);

            // 根据用户选服务
            for (int i = 0; i < reachableServers.size(); i++) {
                server = reachableServers.get(i);
                // 用户的version我知道了，服务的自定义meta我不知道。


                // eureka:
                //  instance:
                //    metadata-map:
                //      version: v2
                // 不能调另外 方法实现 当前 类 应该实现的功能，尽量不要乱尝试

                Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();

                String version1 = metadata.get("version");

                // 服务的meta也有了，用户的version也有了。
                if (version.trim().equals(version1)){
                    return server;

                }

            }

        }

        // 怎么让server 取到合适的值。
        return server;

    }


}
