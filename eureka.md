

```java
@EnableEurekaServer
->
@Import(EurekaServerMarkerConfiguration.class)
public @interface EnableEurekaServer {
}
->
public class EurekaServerMarkerConfiguration {
	@Bean
	public Marker eurekaServerMarkerBean() {
    // new 了一个 marker 开关
		return new Marker();
	}
	class Marker {
	}
}
->
@Configuration(proxyBeanMethods = false)
@Import(EurekaServerInitializerConfiguration.class)
// 使用开关标记是否自动配置
@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties({ EurekaDashboardProperties.class,
		InstanceRegistryProperties.class })
@PropertySource("classpath:/eureka/server.properties")
public class EurekaServerAutoConfiguration implements WebMvcConfigurer

pom文件中引入:
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
在 maven 包 eureka-server. spring.factories 文件中 注入 EurekaServerAutoConfiguration:
/Users/wangxinze/.m2/repository
/org/springframework/cloud/spring-cloud-netflix-eureka-server/2.2.2.RELEASE/spring-cloud-netflix-eureka-server-2.2.2.RELEASE.jar!/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration

所以: @EnableEurekaServer 和 pom 组成了 EurekaServer

  ---------------------------------------------------------------------
  
  

  
在 EurekaServerAutoConfiguration 中 @Import 了 EurekaServerInitializerConfiguration
EurekaServerInitializerConfiguration 里面有个 start() 方法
因为 EurekaServerInitializerConfiguration 实现了 SmartLifecycle, 所以可以执行 start()
->
urekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
->
initEurekaServerContext();
->
this.registry.openForTraffic(this.applicationInfoManager, registryCount);
跳转到 PeerAwareInstanceRegistryImpl # openForTraffic()
->
// Renewals happen every 30 seconds and for a minute it should be a factor of 2.
this.expectedNumberOfClientsSendingRenews = count; // 期望客户端发送的续约的次数
this.updateRenewsPerMinThreshold(); // 更新续约的每分钟的阈值
->
postInit()
->
// 定期剔除没有心跳的服务
evictionTaskRef.set(new EvictionTask()); // 设置剔除任务
evictionTimer.schedule(evictionTaskRef.get(),
                       serverConfig.getEvictionIntervalTimerInMs(), // 剔除时间间隔的毫秒数 换成1s. <快速下线>
                       serverConfig.getEvictionIntervalTimerInMs()); // 剔除得比较慢, 客户端拉取服务的时候, 还可以拉到. 拉取到不可用服务
->
EvictionTask 的 run 方法中
evict(compensationTimeMs);
->
// 优化点: 自我保护机制, 设置阈值为0.85, 10台挂掉3台 和 100台挂掉3台的区别. <不同数量服务的自我保护>
int registrySize = (int) getLocalRegistrySize();
int registrySizeThreshold = (int) (registrySize * serverConfig.getRenewalPercentThreshold());
int evictionLimit = registrySize - registrySizeThreshold;

int toEvict = Math.min(expiredLeases.size(), evictionLimit);

// eureka 中使用 Timer, 是不建议的, 说明:
使用ScheduledExecutorService代替Timer吧 
Inspection info: 
多线程并行处理定时任务时，Timer运行多个TimeTask时，只要其中之一没有捕获抛出的异常，其它任务便会自动终止运行，使用ScheduledExecutorService则没有这个问题。  
    //org.apache.commons.lang3.concurrent.BasicThreadFactory
    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
        new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
    executorService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
            //do something
        }
    },initialDelay,period, TimeUnit.HOURS);
  
---------------------------------------------------------------------

  server:
    // 自我保护看自己情况
    enable-self-preservation: true
    // 续约阈值，和自我保护相关, 自我保护机制, 阈值0.85
    renewal-percent-threshold: 0.85
    // server剔除过期服务的时间间隔, 快速下线, 1s
    eviction-interval-timer-in-ms: 1000
    // 是否开启readOnly读缓存, 关闭 readOnly
    // 关闭从readOnly 读注册表
    // use-read-only-response-cache 默认是true, 可以从yaml点进去, 改成false
    use-read-only-response-cache: false
    // readWrite 和 readOnly 同步时间间隔
    // 默认30s, 从ReadWriteCacheMap同步到, 提高服务被发现的速度
    response-cache-update-interval-ms: 1000 
      
 	


  
eureka实现了ap没有实现c
eureka 的三级缓存:
register 注册表
readWriteMap
useReadOnlyCache
断点打到 ApplicationResource # addInstance() -----------------------------------------------
->
registry.register(info, "true".equals(isReplication));
->
InstanceRegistry # register()
super 跳到 PeerAwareInstanceRegistryImpl # register()
super 跳到 AbstractInstanceRegistry # register() // 服务注册进来更新注册表
  
map<服务名, map<实例id, 实例信息>>
private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry
            = new ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>();
  
->
invalidateCache(registrant.getAppName(), registrant.getVIPAddress(), registrant.getSecureVipAddress());
AbstractInstanceRegistry # invalidateCache()
responseCache.invalidate(appName, vipAddress, secureVipAddress);
->
ResponseCacheImpl # invalidate()
invalidate(new Key(Key.EntityType.VIP, vipAddress, type, v, EurekaAccept.full));
->
readWriteCacheMap.invalidate(key);
->
// 1. 服务注册进 register, 并且让 readWriteCacheMap 失效
LocalCache # invalidate() {
  localCache.remove(key);
}

断点打到 ApplicationResource # getApplication() -----------------------------------------------
  
取服务 String payLoad = responseCache.get(cacheKey);
->
ResponseCacheImpl # get()
->
Value payload = getValue(key, useReadOnlyCache);
->
  // useReadOnlyCache 默认为true
  if (useReadOnlyCache) {
    final Value currentPayload = readOnlyCacheMap.get(key);
    if (currentPayload != null) {
      payload = currentPayload;
    } else {
      // 这里的 readOnlyCacheMap 和 readWriteCacheMap 30s 同步一次, 不是强一致性的, 所以CAP中没有实现C
      // 将 useReadOnlyCache 设置为false, 可以加快客户端拉取服务的速度
      // readWriteCacheMap 是最准确的
      payload = readWriteCacheMap.get(key);
      readOnlyCacheMap.put(key, payload);
    }
  } else {
    payload = readWriteCacheMap.get(key);
  }

在 ResponseCacheImpl() 的构造函数中, 
if (shouldUseReadOnlyResponseCache) {
  timer.schedule(getCacheUpdateTask(),
                 // 注意这里 除以 和 乘以的 是一个东西 responseCacheUpdateIntervalMs
                 new Date(((System.currentTimeMillis() / responseCacheUpdateIntervalMs) * responseCacheUpdateIntervalMs)
                          + responseCacheUpdateIntervalMs),
                 responseCacheUpdateIntervalMs);
}



-------------------------------------
问题总结

cap, 为什么是ap?
缓存机制, 三级缓存
  服务注册进来, 先注册进入 registry, 然后 invalidate 使得缓存失效 readWriteCache
  
  定时任务, 间隔 .. 时间之后同步
  这个任务 从readWrite取出来, put 到only 里面去
  
  父类load Cache. 掉的时候是 getOrLoad() 方法

```

