# eureka 生产优化

## 引入 eureka server

![](https://tva1.sinaimg.cn/large/e6c9d24ely1h5hwd7csj7j20uv0kgq4d.jpg)

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
// 引入 InitializerConfiguration
@Import(EurekaServerInitializerConfiguration.class)
// 使用开关标记是否自动配置
@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties({EurekaDashboardProperties.class, InstanceRegistryProperties.class})
@PropertySource("classpath:/eureka/server.properties")
public class EurekaServerAutoConfiguration implements WebMvcConfigurer

pom文件中引入:
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
          
在 maven 包 eureka-server. spring.factories 文件中 :
/Users/wangxinze/.m2/repository
/org/springframework/cloud/spring-cloud-netflix-eureka-server/2.2.2.RELEASE/spring-cloud-netflix-eureka-server-2.2.2.RELEASE.jar!/META-INF/spring.factories
  
文件内容为, 注入 EurekaServerAutoConfiguration:
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration

所以: @EnableEurekaServer 和 pom 组成了 EurekaServer
```







## EurekaServerInitializerConfiguration 源码

```java

在 EurekaServerAutoConfiguration 中 @Import 了 EurekaServerInitializerConfiguration
干的事情:
1. 从peer拉取注册表
2. 启动定时剔除任务
3. 自我保护
  
EurekaServerInitializerConfiguration 里面有个 start() 方法
因为 EurekaServerInitializerConfiguration 实现了 SmartLifecycle, 所以可以执行 start()

EurekaServerInitializerConfiguration # start()
->
urekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
->
initEurekaServerContext();
->
// 从 eureka server 其他的注册节点peer拉取同步注册表
// 也就是cap中没有满足c的地方 -- consistency 一致性 / availability 可用性 / partition tolerance 分区容忍性
// 因为启动的时候才去拉数据, 所以数据不是强一致性的, 后注册的拉不到
int registryCount = this.registry.syncUp();  

下一步
  
this.registry.openForTraffic(this.applicationInfoManager, registryCount);
跳转到 PeerAwareInstanceRegistryImpl # openForTraffic()
->
// Renewals happen every 30 seconds and for a minute it should be a factor of 2.
this.expectedNumberOfClientsSendingRenews = count; // 期望客户端发送的续约的次数
this.updateRenewsPerMinThreshold(); // 更新续约的每分钟的阈值 numberOfRenewsPerMinThreshold
->
super.postInit();
->
// 定期剔除没有心跳的服务
evictionTaskRef.set(new EvictionTask()); // 设置剔除任务
evictionTimer.schedule(evictionTaskRef.get(),
                       // 剔除时间间隔的毫秒数 换成1s. <快速下线>
                       // 剔除得比较慢, 客户端拉取服务的时候, 还可以拉到. 拉取到不可用服务
                       serverConfig.getEvictionIntervalTimerInMs(), 
                       serverConfig.getEvictionIntervalTimerInMs());
->
EvictionTask 的 run() 方法中
evict(compensationTimeMs);
->
    public boolean isLeaseExpirationEnabled() {
        // 自我保护关闭 false -> 返回 true -> 剔除服务
        if (!isSelfPreservationModeEnabled()) {
            // The self preservation mode is disabled, hence allowing the instances to expire.
            return true;
        }
  		  // 自我保护开启true && 每分钟续约的阈值 > 0 && 
  			// 最后一分钟的心跳数 > 每分钟续约的阈值 ? 返回true 剔除服务 ; 返回false 不剔除自我保护正式开启
        return numberOfRenewsPerMinThreshold > 0 && getNumOfRenewsInLastMin() > numberOfRenewsPerMinThreshold;
    }  

// 优化点: 自我保护机制, 设置阈值为0.85, 10台挂掉3台 和 100台挂掉3台的区别. <不同数量服务的自我保护>
int registrySize = (int) getLocalRegistrySize();
int registrySizeThreshold = (int) (registrySize * serverConfig.getRenewalPercentThreshold());
int evictionLimit = registrySize - registrySizeThreshold;

int toEvict = Math.min(expiredLeases.size(), evictionLimit);

----------------------------------------------------------------------------

// 注意, 在 eureka 中使用 Timer (任务剔除时), 是不建议的, 说明:
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
```

## 优化的相关参数

```java
  server:
    // 自我保护, 看自己情况
    enable-self-preservation: true
    // 续约阈值，和自我保护机制相关, 设置阈值为0.85
    renewal-percent-threshold: 0.85
    // server剔除过期服务的时间间隔, 设置为1s, 快速下线
    eviction-interval-timer-in-ms: 1000
    // 是否开启 readOnlyCacheMap 读缓存, 关闭从 readOnlyCacheMap 读注册表
    // use-read-only-response-cache 默认是true, 可以从yaml点进去, 改成false
    use-read-only-response-cache: false
    // readWriteCacheMap 和 readOnlyCacheMap 同步时间间隔
    // 默认30s, 从 readWriteCacheMap 同步到 readOnlyCacheMap, 提高服务被发现的速度
    response-cache-update-interval-ms: 1000 
```

## EurekaServerAutoConfiguration 源码

```java
EurekaServerAutoConfiguration import EurekaServerInitializerConfiguration 干的事情:
1. 从peer拉取注册表
2. 启动定时剔除任务
3. 自我保护

其中 EurekaController 是服务管理后台使用的
  
peerEurekaNodes 封装其他节点的一个bean, 从 int registryCount = this.registry.syncUp(); 拉取节点时候需要用到
  
eurekaServerContext()方法中
->
new DefaultEurekaServerContext()
->
@PostConstruct
initialize()
->
registry.init(peerEurekaNodes);
PeerAwareInstanceRegistryImpl # init()
->
// 初始化三级缓存
initializedResponseCache();
->
responseCache = new ResponseCacheImpl(serverConfig, serverCodecs, this);
->
  注意此处:
	this.readWriteCacheMap = CacheBuilder.newBuilder().build();
	其中
    .build(new CacheLoader<Key, Value>() {
      @Override
      public Value load(Key key) throws Exception {
        if (key.hasRegions()) {
          Key cloneWithNoRegions = key.cloneWithoutRegions();
          regionSpecificKeys.put(cloneWithNoRegions, key);
        }
        Value value = generatePayload(key);
        return value;
      }
    });
  
  // 定时更新缓存
  // readWriteCacheMap 和 readOnlyCacheMap 之间数据怎么同步?
  // 在 ResponseCacheImpl 的构造函数启动, 该构造函数从 DefaultEurekaServerContext Bean 中启动
  if (shouldUseReadOnlyResponseCache) {
    timer.schedule(getCacheUpdateTask(),
                   // x = responseCacheUpdateIntervalMs
                   new Date(((System.currentTimeMillis() / x) * x) + x), x);
  }


// springcloud 定义标准, eureka 实现了这套标准, springcloud 和原生的 eureka 的一个胶水代码
EurekaServerBootstrap 
  
// jersey 框架, 对 eureka server 所有的操作都是通过 http 请求完成的
FilterRegistrationBean jerseyFilterRegistration()
->
  server: 
		1. 接受注册
    2. 接受心跳
    3. 下线
    4. 获取注册列表(服务发现)
    5. 集群同步 (n 多个resource 当中都会调用集群同步) :
  类 ApplicationResource 都是用来接受请求的
      addInstance 注册的
  InstanceResource
      renewLease 续约的
  		statusUpdate 改变状态
      updateMetadata 改变自定义数据的
      cancelLease 下线的
  什么时候会调用集群同步? 后来的服务 都通过 集群同步 给同步到别的 eureka server 上 <主动推送>
  启动的时候拉取, 没有同步的(启动完才注册的), 就通过集群同步同步过去
```



## ApplicationResource 源码

```java

断点打到 ApplicationResource # addInstance()
->
// 注意 "true".equals(isReplication); 集群同步的时候 isReplication 是个null, 方法进去第二个参数是false
registry.register(info, "true".equals(isReplication));
->
InstanceRegistry # register()
super 跳到 PeerAwareInstanceRegistryImpl # register()
super 跳到 AbstractInstanceRegistry # register() // 服务注册进来更新注册表
  
map<服务名, map<实例id, 实例信息>>
private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry
            = new ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>();
  
->
  	// private ConcurrentLinkedQueue<RecentlyChangedItem> recentlyChangedQueue = 
  	// 																				new ConcurrentLinkedQueue<RecentlyChangedItem>();
    recentlyChangedQueue.add(new RecentlyChangedItem(lease)); // 失效缓存

		// 集群同步, 此处isReplication传进来是fasle
    回到 PeerAwareInstanceRegistryImpl # 
      replicateToPeers(Action.Register, info.getAppName(), info.getId(), info, null, isReplication); 
    ->
    PeerAwareInstanceRegistryImpl # replicateToPeers()

        if (isReplication) {
          numberOfReplicationsLastMin.increment();
        }
        // If it is a replication already, do not replicate again as this will create a poison replication
        if (peerEurekaNodes == Collections.EMPTY_LIST || isReplication) {
          return;
        }
        // case: ServerB 的peer是 ServerA, ServerA 的peer是 ServerC. 示例: ServerB -> ServerA -> ServerA
        // 服务B 注册到 ServerB, 那么只同步 ServerB 到 ServerA 一次, ServerA 并不会给 ServerC 同步
        为什么三个地址的时候, defaultZone要写上三个地址?
        理解: ServerC 也是 ServerB 的 peer, 会从 ServerB 同步到 ServerA 和 ServerC. 
      
    // addInstance 返回结果就是204
    addInstance # return Response.status(204).build();  // 204 to be backwards compatible

下一步 -----------------------------------------------
  
invalidateCache(registrant.getAppName(), registrant.getVIPAddress(), registrant.getSecureVipAddress());
AbstractInstanceRegistry # invalidateCache()
responseCache.invalidate(appName, vipAddress, secureVipAddress);
->
ResponseCacheImpl # invalidate()
invalidate(new Key(Key.EntityType.VIP, vipAddress, type, v, EurekaAccept.full));
->
readWriteCacheMap.invalidate(key);
->
// 服务注册进 register, 并且让 readWriteCacheMap 失效
LocalCache # invalidate() {
  localCache.remove(key);
}













断点打到 ApplicationResource # getApplication() -----------------------------------------------
  
取服务 String payLoad = responseCache.get(cacheKey);
->
ResponseCacheImpl # get()
->
return get(key, shouldUseReadOnlyResponseCache);
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
                 // 注意这里 除以 和 乘以的 是一个东西 x = responseCacheUpdateIntervalMs
                 new Date(((System.currentTimeMillis() / x) * x) + x),
                 responseCacheUpdateIntervalMs);
}     
```



## 总结



```

CAP没有满足C的地方:
一. eureka 的三级缓存:
    register 注册表
    readWriteCacheMap
    readOnlyCacheMap
      服务注册进来, 先注册进入 registry, 然后 invalidate 使得缓存失效 readWriteCacheMap

      定时任务, 间隔 .. 时间之后同步
      这个任务 从 readWriteCacheMap 取出来, put 到 readOnlyCacheMap 里面去

      父类 load Cache. 调的时候是 getOrLoad() 方法
二. 集群间同步
  	从其他 peer (application.yaml 文件中的 defaultZone 就是 peer) 拉取注册表
  	int registryCount = this.registry.syncUp()
  	
  	
--------------------------------------------------------------------------
  	
自我保护剔除:
	1. 开关
	2. 阈值

服务测算:

server源码:
	注册
	剔除
	
	
	1h 14min
```

