# Eureka 生产优化

## 引入 EurekaServer

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
  EurekaController # status()
  其中 statusInfo 属性
  statusInfo = new StatusResource().getStatusInfo();
	->
    // peerEurekaNodes.getPeerEurekaNodes() 循环 节点的每个同伴
    for (PeerEurekaNode node : peerEurekaNodes.getPeerEurekaNodes()) {
      if (replicaHostNames.length() > 0) {
        replicaHostNames.append(", ");
      }
      replicaHostNames.append(node.getServiceUrl());
      // isReplicaAvailable() 判断条件, 决定哪个节点装入 "unavailable-replicas"
      if (isReplicaAvailable(node.getServiceUrl())) {
        upReplicas.append(node.getServiceUrl()).append(',');
        upReplicasCount++;
      } else {
        downReplicas.append(node.getServiceUrl()).append(',');
      }
    }

    builder.add("registered-replicas", replicaHostNames.toString());
    builder.add("available-replicas", upReplicas.toString());
    builder.add("unavailable-replicas", downReplicas.toString());
		->
    private boolean isReplicaAvailable(String url) {
      try {
        Application app = registry.getApplication(myAppName, false);
        if (app == null) {
          return false;
        }
        for (InstanceInfo info : app.getInstances()) {
          // isInstanceURL() 判断条件
          if (peerEurekaNodes.isInstanceURL(url, info)) {
            return true;
          }
        }
      } catch (Throwable e) {
        logger.error("Could not determine if the replica is available ", e);
      }
      return false;
    }
  
  
peerEurekaNodes 封装其他节点的一个bean, 从 int registryCount = this.registry.syncUp(); 拉取节点时候需要用到
  
eurekaServerContext()方法中
->
// !!!!!!!!!!!!!!!!!!!!! important
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

## readWriteCacheMap & recentlyChangedQueue 说明

```java
ResponseCacheImpl # ResponseCacheImpl() // 构造函数中

// LoadingCache Google的一个本地缓存框架 guava
private final LoadingCache<Key, Value> readWriteCacheMap;

this.readWriteCacheMap =
  CacheBuilder.newBuilder().initialCapacity(serverConfig.getInitialCapacityOfResponseCache())
  .expireAfterWrite(serverConfig.getResponseCacheAutoExpirationInSeconds(), TimeUnit.SECONDS)
  .removalListener(new RemovalListener<Key, Value>() {
    @Override
    public void onRemoval(RemovalNotification<Key, Value> notification) {
      Key removedKey = notification.getKey();
      if (removedKey.hasRegions()) {
        Key cloneWithNoRegions = removedKey.cloneWithoutRegions();
        regionSpecificKeys.remove(cloneWithNoRegions, removedKey);
      }
    }
  })
  .build(new CacheLoader<Key, Value>() {
    @Override
    public Value load(Key key) throws Exception {
      if (key.hasRegions()) {
        Key cloneWithNoRegions = key.cloneWithoutRegions();
        regionSpecificKeys.put(cloneWithNoRegions, key);
      }
      // 如果找不到值, 会走 generatePayload()
      Value value = generatePayload(key);
      return value;
    }
  });
->
  switch (key.getEntityType()) {
    case Application:
      // 全量查找
      if (ALL_APPS.equals(key.getName())) {
      // 增量查找
      } else if (ALL_APPS_DELTA.equals(key.getName())) {
        payload = getPayLoad(key, registry.getApplicationDeltas()); 中
        registry.getApplicationDeltas()
        ->
        // 如果是增量的话, 就从 recentlyChangedQueue 当中取
        // private ConcurrentLinkedQueue<RecentlyChangedItem> recentlyChangedQueue = 
        // 																								  new ConcurrentLinkedQueue<RecentlyChangedItem>();
        // recentlyChangedQueue 中的元素 什么时候过期?
        Iterator<RecentlyChangedItem> iter = this.recentlyChangedQueue.iterator();
      } else {
      }
      break;
    case VIP:
    case SVIP:
    default:
  }



recentlyChangedQueue add 的地方
ApplicationResource # addInstance()
->
PeerAwareInstanceRegistryImpl # register()
super.register(info, leaseDuration, isReplication);
->
AbstractInstanceRegistry # register()
// 注册 / 心跳, 往里面 add 数据 源码263行
recentlyChangedQueue.add(new RecentlyChangedItem(lease));



// AbstractInstanceRegistry 构造函数
protected AbstractInstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig clientConfig, ServerCodecs serverCodecs) {
  this.serverConfig = serverConfig;
  this.clientConfig = clientConfig;
  this.serverCodecs = serverCodecs;
  this.recentCanceledQueue = new CircularQueue<Pair<Long, String>>(1000);
  this.recentRegisteredQueue = new CircularQueue<Pair<Long, String>>(1000);

  this.renewsLastMin = new MeasuredRate(1000 * 60 * 1);

  // 定时任务
  this.deltaRetentionTimer.schedule(getDeltaRetentionTask(),
                                    serverConfig.getDeltaRetentionTimerIntervalInMs(),
                                    serverConfig.getDeltaRetentionTimerIntervalInMs());
}
->
    private TimerTask getDeltaRetentionTask() {
        return new TimerTask() {

            @Override
            public void run() {
                Iterator<RecentlyChangedItem> it = recentlyChangedQueue.iterator();
                while (it.hasNext()) {
                  	// serverConfig.getRetentionTimeInMSInDeltaQueue() 默认 3min
                    // 所以 recentlyChangedQueue 保留最近 3min 的注册信息
                    // 如果用的时候, 心跳是 5min(一般设置为秒级, 这种情况不会). 3min失效. 最近一次增量拉取拉取不到
                    if (it.next().getLastUpdateTime() <
                            System.currentTimeMillis() - serverConfig.getRetentionTimeInMSInDeltaQueue()) {
                      	// recentlyChangedQueue 过期
                        it.remove();
                    } else {
                        break;
                    }
                }
            }

        };
    }


-------------------------
  
  参考 下线, 也是加入这个队列 recentlyChangedQueue
  
  
```





## ApplicationResource 源码

### 服务注册

```java
断点打到 ApplicationResource # addInstance()
  
@POST
@Consumes({"application/json", "application/xml"})
public Response addInstance(InstanceInfo info, @HeaderParam(PeerEurekaNode.HEADER_REPLICATION) String isReplication) {}

// eureka 设计的 Lease<InstanceInfo>, 跟续约业务有关 !!!
1. 服务实例 InstanceInfo info
2. AbstractInstanceRegistry # register
	 租约 Lease<InstanceInfo> existingLease = gMap.get(registrant.getId());

收到服务实例, 保存. 
  自己设计一个服务实例, 有xxx时间: 心跳时间, xxx时间
  class 服务实例 { 
    long 到期time; 
    long 续约time; 
    long 心跳time; 
    string 实例字段;
    // 还有一种方式, 服务实例内有租约
  }

	eureka 设计(Lease 租约):
  public class Lease<T> {
    public static final int DEFAULT_DURATION_IN_SECS = 90;
    // 服务实例holder. 这样设计的好处: 与eureka 的业务有关
    // 后面续约是一个频繁的操作, 所以只用更改时间就可以了
    // 频繁的续约操作不会影响服务实例
    private T holder; 
    private long evictionTimestamp;
    private long registrationTimestamp;
    private long serviceUpTimestamp;
    // Make it volatile so that the expiration task would see this quicker
    private volatile long lastUpdateTimestamp;
    private long duration;
  }

  
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
```

### 获取服务

```java

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

### 续约

```java
https://github.com/Netflix/eureka/wiki/Eureka-REST-operations

Register new application instance 注册 POST /eureka/v2/apps/appID	
Send application instance heartbeat 续约 PUT /eureka/v2/apps/appID/instanceID	

断点打到 InstanceResource # renewLease() 续约
->
boolean isSuccess = registry.renew(app.getName(), id, isFromReplicaNode);
->
InstanceRegistry # renew()
->
return super.renew(appName, serverId, isReplication);
->
    public boolean renew(final String appName, final String id, final boolean isReplication) {
        if (super.renew(appName, id, isReplication)) {
        		// 这里也会调用 replicateToPeers, 同步给集群中其他的peer
            replicateToPeers(Action.Heartbeat, appName, id, null, null, isReplication);
            return true;
        }
        return false;
    }
->
  先进 super.renew(appName, id, isReplication) ------------------------------------------------------------------------
  ->
  leaseToRenew.renew();
	->
  // 续约: 只更新 lastUpdateTimestamp 时间
  lastUpdateTimestamp = System.currentTimeMillis() + duration;

	同理
    /**
     * Cancels the lease by updating the eviction time.
     */
    public void cancel() {
        if (evictionTimestamp <= 0) {
            evictionTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Mark the service as up. This will only take affect the first time called,
     * subsequent calls will be ignored.
     */
    public void serviceUp() {
        if (serviceUpTimestamp == 0) {
            serviceUpTimestamp = System.currentTimeMillis();
        }
    }

		注意
    enum Action {
        Register, Cancel, Renew
    };
			
```

### 下线

```java

InstanceResource # cancelLease
->
boolean isSuccess = registry.cancel(app.getName(), id, "true".equals(isReplication));
->
InstanceRegistry # cancel
->
    1. handleCancelation(appName, serverId, isReplication);
    // 发布下线事件
    publishEvent(new EurekaInstanceCanceledEvent(this, appName, id, isReplication));

    2. return super.cancel(appName, serverId, isReplication);
		->
    if (super.cancel(appName, id, isReplication)) {
      // 下线, 向集群同步
    	replicateToPeers(Action.Cancel, appName, id, null, null, isReplication);
			return true;
    }
		return false;
		->
    return internalCancel(appName, id, isReplication);
		->
    // 下线, 也是修改 evictionTimestamp 时间
		leaseToCancel.cancel();

    // 加入下线的队列
    recentlyChangedQueue.add(new RecentlyChangedItem(leaseToCancel));

```

### 拉取注册表

```java


对外提供拉取的两个方法, 区别 全量拉取/增量拉取:
	1. ApplicationsResource # getContainers // localhost:7900/eureka/apps 全量拉取
     ->
     // 属性 entityName = ALL_APPS
     Key cacheKey = new Key(Key.EntityType.Application, ResponseCacheImpl.ALL_APPS, 
                            keyType, CurrentRequestVersion.get(), EurekaAccept.fromString(eurekaAccept), regions);
     response = Response.ok(responseCache.getGZIP(cacheKey))
       .header(HEADER_CONTENT_ENCODING, HEADER_GZIP_VALUE)
       .header(HEADER_CONTENT_TYPE, returnMediaType)
       .build();
		 ->
     进去 getGZIP() 方法
     Value payload = getValue(key, shouldUseReadOnlyResponseCache);
		 ->
   	 ResponseCacheImpl # getValue(key = "ALL_APPS", boolean useReadOnlyCache)
     // 如果有的话 all-apps(包含两个requestType不同的 xml/json), apps-delta, 服务名
     // private final ConcurrentMap<Key, Value> readOnlyCacheMap = new ConcurrentHashMap<Key, Value>();
     final Value currentPayload = readOnlyCacheMap.get(key);
       
      
  2. @Path("delta") 增量拉取 
     ApplicationsResource # getContainerDifferential() // // localhost:7900/eureka/apps/delta 增量拉取
     ->
  	 // 属性 entityName = ALL_APPS_DELTA
     Key cacheKey = new Key(Key.EntityType.Application, ResponseCacheImpl.ALL_APPS, 
                            keyType, CurrentRequestVersion.get(), EurekaAccept.fromString(eurekaAccept), regions);
     
     response = Response.ok(responseCache.getGZIP(cacheKey))
       .header(HEADER_CONTENT_ENCODING, HEADER_GZIP_VALUE)
       .header(HEADER_CONTENT_TYPE, returnMediaType)
       .build();
     ->
    
    什么时候从 delta 读到 all 里面去?
    recentlyChangedQueue, delta 都从这里面取
  
    
  
```





### 集群同步

```java
ApplicationResource # addInstance()
->
registry.register(info, "true".equals(isReplication));
->
PeerAwareInstanceRegistryImpl # register # 
// 集群同步. 
// 参数 isReplication 为 false: 传过来是 "true".equals(isReplication) 
replicateToPeers(Action.Register, info.getAppName(), info.getId(), info, null, isReplication);
->
// If it is a replication already, do not replicate again as this will create a poison replication
// 如果 isReplication 为 true, 也不继续同步
if (peerEurekaNodes == Collections.EMPTY_LIST || isReplication) {
  return;
}

// 第一次进 isReplication 为 false, 所以会走 replicateInstanceActionsToPeers, 向其他节点同步
for (final PeerEurekaNode node : peerEurekaNodes.getPeerEurekaNodes()) {
  // If the url represents this host, do not replicate to yourself.
  if (peerEurekaNodes.isThisMyUrl(node.getServiceUrl())) {
    continue;
  }
  replicateInstanceActionsToPeers(action, appName, id, info, newStatus, node);
}
->
  switch (action) {
    case Cancel:
      node.cancel(appName, id);
      break;
    case Heartbeat:
      InstanceStatus overriddenStatus = overriddenInstanceStatusMap.get(id);
      infoFromRegistry = getInstanceByAppAndId(appName, id, false);
      node.heartbeat(appName, id, infoFromRegistry, overriddenStatus, false);
      break;
    case Register:
      node.register(info);
      break;
    case StatusUpdate:
      infoFromRegistry = getInstanceByAppAndId(appName, id, false);
      node.statusUpdate(appName, id, newStatus, infoFromRegistry);
      break;
    case DeleteStatusOverride:
      infoFromRegistry = getInstanceByAppAndId(appName, id, false);
      node.deleteStatusOverride(appName, id, infoFromRegistry);
      break;
      
   // 第一个节点注册进来: null, "true".equals(isReplication) 后面那个值为false, 所以会走replicateInstanceActionsToPeers
   // 注意在 register 方法里面 new InstanceReplicationTask() 时候最后一个参数 写死为 true
   public void register(final InstanceInfo info) throws Exception {
     long expiryTime = System.currentTimeMillis() + getLeaseRenewalOf(info);
     batchingDispatcher.process(
       taskId("register", info),
       new InstanceReplicationTask(targetHost, Action.Register, info, null, true) {
         public EurekaHttpResponse<Void> execute() {
           return replicationClient.register(info);
         }
       },
       expiryTime
     );
   }
   // 第二次进来 isReplication 为 true, "true".equals(isReplication) 也为true, 在 if(isReplication) return 了, 就不会再向其他节点同步了

      
  ---------------------------------------------
    
    
    同理, 续约走 switch - case 的 Heartbeat
      
    取消 new InstanceReplicationTask() 没传, 构造函数中 this.replicateInstanceInfo = false;
      
    集群同步:
      1. 注册register: 不会传递. // new InstanceReplicationTask 时 replicateInstanceInfo 为 true
      2. 续约heartbeat: 一直同步. 所有集群. // new InstanceReplicationTask 时 replicateInstanceInfo 为 false
      3. 下线: 和2一样 // new InstanceReplicationTask 时 replicateInstanceInfo 默认为 false
      4. 剔除: 不同步, 因为所有的 eureka server 都有自己的剔除. 没有同步这项业务
      		EurekaServerInitializerConfiguration # start()
          ->
          eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
      		->
          initEurekaServerContext();
      		->
          this.registry.openForTraffic(this.applicationInfoManager, registryCount);
      		->
          InstanceRegistry # openForTraffic()
          super.openForTraffic(applicationInfoManager, count == 0 ? this.defaultOpenForTrafficCount : count);
      		->
          PeerAwareInstanceRegistryImpl # openForTraffic()
          ->
          super.postInit();
      		->
          evictionTaskRef.set(new EvictionTask());
      		->
          evict(compensationTimeMs); // 剔除
      		->
          internalCancel(appName, id, false); // 不知道是不是此处 参数 isReplication 为 false


      
      
            
          
            

           
        
```





## EurekaServer 相关问题

```java

      	eureka 遇到的问题:
        生产环境: 服务重启时, 先停服, 再手动触发下线
        注意: 虽然停服了, 但是还在注册中心挂着, 别人一调用就错了
        如果在生产环境中, 先下线, 再停服, 很有可能下线白下. 举例: 不停服, 下线了过30s 又有了
        
        debug 源码: 发现又自动续约了
        client 每隔 30s 就会向 server 自动续约一次
          
          
      --------------------------------------
        search: unavailable-replicas
        问题, 注册服务可用, 但是出现在 unavailable-replicas 当中  
        参见 EurekaController 逻辑
          
        // 1.开启互相注册
        // 如果为false, 不向eureka注册, 出现在 unavailable-replicas 中. isReplicaAvailable() 方法中 app == null
        eureka.client.register-with-eureka = true
        eureka.client.fetch-registry = true
        
        // 2. defaultZone
        // 写 ip 也可以, 但是 我写的同伴的地址 和 收到的注册表中同伴的地址 是一样的, 证明服务是可用的<更优>
        defaultZone: 中 http://localhost:7900/eureka 改为 http://eureka-7900:7900/eureka
      	
      	// 3. appName 要一致
      	spring.application.name = cloud-eureka	
      
      	// 4.
      	eureka.instance.hostname = eureka-7901
        eureka.instance.prefer-ip-address = true
        eureka.instance.ip-address: 127.0.0.1
          
        host 文件配置:
      	127.0.0.1 eureka-7900
        127.0.0.1 eureka-7901
        127.0.0.1 eureka-7902
          
       --------------------------------------
          
          区域 / 可用区的问题: 减少网络延迟
      
          region: bj // 北京
          北京里面包含很多个可用区, 每个区里面有多个服务
             (包括 eureka client), 
      			(优先调用本区域的eureka的client), 
      			(当本区服务不可用再调用别的区域的 eureka client)
          
          // eureka server config:
      		eureka.client.region: bj
          eureka.client.availability-zones.bj: z1, z2
          eureka.client.service-url.z1: http://localhost:7911/eureka/, http://localhost:7912/eureka/
      	  eureka.client.service-url.z2: http://localhost:7921/eureka/, http://localhost:7922/eureka/
      		
      		四个配置文件, server port分别是 7911, 7912, 7921, 7922. 其中关于2的服务: 
      		eureka.client.availability-zones.bj: z2, z1 // 把z2写在前面
       
          // eureka client config:
      		eureka.client.region: bj
          eureka.client.availability-zones.bj: z1 // 第一个只给区域一注册
          eureka.client.service-url.z1: http://localhost:7911/eureka/, http://localhost:7912/eureka/
      	  eureka.client.service-url.z2: http://localhost:7921/eureka/, http://localhost:7922/eureka/
     
          eureka.client.prefer-same-zone-eureka: true // 先取相同区域的服务
          eureka.instance.metadata-map.zone: z1 // 优先从z1取服务
            
          一共两个配置文件, 第二个是把上述z1更换为z2
```



## 总结

```java
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
三. P: 网络不好的情况下, 还是可以拉取到注册表调用的. 服务还可以调用
    A: 高可用
  	
--------------------------------------------------------------------------
  	
自我保护剔除:优化参数
	1. 开关
	2. 阈值
    
--------------------------------------------------------------------------

服务测算:
  参数:
  eureka:
  	instance:
  		lease-renewal-interval-in-seconds: 10 // 默认值30s
  eureka:
  	client:
			// 表示eureka client间隔多久去拉取服务注册信息，默认为30秒，对于api-gateway，如果要迅速获取服务注册状态，可以缩小该值，比如5秒
    	registry-fetch-interval-seconds: 30 // 默认30
      
  可以估算 eureka 每天能承受多大的访问量:
  20个服务, 每个服务部署5个. eureka client 100 个, 30s 一次心跳
  1分钟 1个server 200次, 200 * 60 * 24 = 288000 访问量
  
  心跳: 向server发送我们还活着 
  288000 * 2 client 去拉服务
    
  TestConcurrentHashMap.java, 测算时间, 大概 113ms 可以处理 10000 次请求 (一个eureka)
  整个 网约车项目 大概10几万次请求 访问量
  多个 eureka, 同步,
	A 高可用
  P 网络分区, 续约, 
	自我保护 / 定时剔除, 由于网络问题, 续约不成功, 另一个服务还是能从 eureka server 节点拉到注册表信息的
  所以网络短了仍然可以被访问, 保证了 P

	  @Test
    public void concurrent() {
        ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry = new ConcurrentHashMap();
        long start = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            InstanceInfo ii = new InstanceInfo("instanceId", "appname", "groupname", "111.11.11", "sid",
                    new InstanceInfo.PortWrapper(true, 9988),
                    new InstanceInfo.PortWrapper(false, 9877),
                    "ssss", "ssss", "ssssss", "12121",
                    "safasdas", "adss", 1, () -> null, "localhost",
                    InstanceInfo.InstanceStatus.DOWN, null, null, null,
                    false, null, System.currentTimeMillis(), System.currentTimeMillis(),
                    InstanceInfo.ActionType.ADDED, "asgName");
            ii.setLastDirtyTimestamp(System.currentTimeMillis());
            Lease<InstanceInfo> lease = new Lease<>(ii, 1);
            Map<String, Lease<InstanceInfo>> map = new HashMap<>();
            registry.putIfAbsent("applicationName", map);
            registry.get("applicationName");
        }

        long end = System.currentTimeMillis();

        System.out.println(end - start);
    }

--------------------------------------------------------------------------
  
server源码:
	注册. 
	剔除(本质也是下线). 长时间没有心跳的服务, eureka server 将它从注册表剔除.
	续约
  下线
  集群间同步
  拉取注册表
    
    
    
                eureka server: 
      				操作 : 单体 / 高可用 / 多区域
            	 源码 : 注册 / 下线 / 心跳 / 剔除 / 拉取注册表 / 集群同步
```





# EurekaClient



```java
eureka client 客户端: api-passenger

// eureka.client: 与 server 交互的配置
eureka.client.registry-fetch-interval-seconds: 30 // 拉取间隔时间
// eureka.instance: 自身实例的信息
eureka.instance.lease-renewal-interval-in-seconds: 30 // 心跳间隔时间
  
以下两张图 spring.factories 注入客户端需要用的bean
```

![image-20220830105333371](https://tva1.sinaimg.cn/large/e6c9d24egy1h5ols9zh62j20rm0b075x.jpg)

![image-20220830105356826](https://tva1.sinaimg.cn/large/e6c9d24egy1h5olsn500pj21h00gm0xz.jpg)



## EurekaClientAutoConfiguration

```java


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
// 与server 交互的配置 EurekaClientConfig(接口). 默认配置 DefaultEurekaClientConfig
@ConditionalOnClass(EurekaClientConfig.class)
@Import(DiscoveryClientOptionalArgsConfiguration.class)
// 默认为true, 与 server 的 Maker 开关不同, client 的开关为 eureka.client.enabled
@ConditionalOnProperty(value = "eureka.client.enabled", matchIfMissing = true)
@ConditionalOnDiscoveryEnabled
@AutoConfigureBefore({ NoopDiscoveryClientAutoConfiguration.class,
		CommonsClientAutoConfiguration.class, ServiceRegistryAutoConfiguration.class })
@AutoConfigureAfter(name = {
		"org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
		"org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration",
		"org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration" })
public class EurekaClientAutoConfiguration {}


EurekaDiscoveryClientConfigServiceAutoConfiguration
  
  
```







## EurekaClient 启动流程

![05-eureka-client-1](https://tva1.sinaimg.cn/large/e6c9d24egy1h5p08x9lccj21270u0gpp.jpg)

```java

// spring 定义的一套标准  
org.springframework.cloud.client.discovery.DiscoveryClient
// Netflix eureka 实现了它
org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient
public class EurekaDiscoveryClient implements DiscoveryClient {}
// consul 也实现了
  
  
// com.netflix.discovery.EurekaClient
@ImplementedBy(DiscoveryClient.class)
public interface EurekaClient extends LookupService {
  // 注册健康检查
  public void registerHealthCheck(HealthCheckHandler healthCheckHandler);
  // 注册事件监听
  public void registerEventListener(EurekaEventListener eventListener);
}
->
// com.netflix.discovery.DiscoveryClient 实现了 EurekaClient 上面接口, 包含启动流程, 关键!!!!!
@Singleton
public class DiscoveryClient implements EurekaClient {}

// 断点打到 DiscoveryClient 最下面一个构造函数
// 如果 eureka.client.enabled = false, 就不会走到该构造函数中

@Inject
DiscoveryClient(ApplicationInfoManager applicationInfoManager, // 应用的信息管理器
                EurekaClientConfig config, // 和 server 交互的配置
                AbstractDiscoveryClientOptionalArgs args, 
                Provider<BackupRegistry> backupRegistryProvider, 
                EndpointRandomizer endpointRandomizer) {}

// 启动过程:
1. 封装和server交互的配置
2. 初始化三个任务, 发送心跳 / 缓存刷新 / 状态改变监听(状态改变了-按需要注册)
3. 发起注册, 等待40s后
// 运行:
就是运行这三个任务
// 消亡: discoveryClient # shutdown()
  // 销毁之前, 下线
  public synchronized void shutdown() {}
  ->
  // If APPINFO was registered
  if (applicationInfoManager != null
      && clientConfig.shouldRegisterWithEureka()
      && clientConfig.shouldUnregisterOnShutdown()) {
    applicationInfoManager.setInstanceStatus(InstanceStatus.DOWN);
    unregister(); // 走这个
  }
	->
  // 也是走http 请求
  EurekaHttpResponse<Void> httpResponse = eurekaTransport.registrationClient
    													.cancel(instanceInfo.getAppName(), instanceInfo.getId());
	->
  AbstractJerseyEurekaHttpClient # cancel()
  ->
  response = resourceBuilder.delete(ClientResponse.class);

------------------------------------------------------------------------------
  
  	DiscoveryClient 启动流程源码:
  
  			// 是不是去 server 拉取注册表
        if (config.shouldFetchRegistry()) {
        } else {
        }

				// 禁用 eureka client 功能, 不向 eureka 注册 && 不拉取, 直接return. 不作为 eureka 的客户端
    		// 同配置 eureka.client.enabled: false
				if (!config.shouldRegisterWithEureka() && !config.shouldFetchRegistry()) {
          return;  // no need to setup up an network tasks and we are done 不需要设置网络任务，我们就完成了
        }

        // 心跳定时任务
        heartbeatExecutor = new ThreadPoolExecutor(
          1, 
          clientConfig.getHeartbeatExecutorThreadPoolSize(), 
          0, 
          TimeUnit.SECONDS, 
          new SynchronousQueue<Runnable>(), 
          new ThreadFactoryBuilder().setNameFormat("DiscoveryClient-HeartbeatExecutor-%d").setDaemon(true).build()
        );  // use direct handoff

        // 缓存刷新, eureka client 优化, 拉取注册表更及时一些
        cacheRefreshExecutor = new ThreadPoolExecutor(
          1, 
          clientConfig.getCacheRefreshExecutorThreadPoolSize(), 
          0, 
          TimeUnit.SECONDS, 
          new SynchronousQueue<Runnable>(),
          new ThreadFactoryBuilder().setNameFormat("DiscoveryClient-CacheRefreshExecutor-%d").setDaemon(true).build()
        );  // use direct handoff
				
				// 同 eureka 交互的一个东西
				eurekaTransport = new EurekaTransport();

			  // fetchRegistry() 拉取注册表
        if (clientConfig.shouldFetchRegistry() && !fetchRegistry(false)) {
          fetchRegistryFromBackup();
        }
				>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				// 前面为 clientConfig.shouldFetchRegistry() = true, 进去 fetchRegistry() 方法
				if (clientConfig.shouldDisableDelta()
                    || (!Strings.isNullOrEmpty(clientConfig.getRegistryRefreshSingleVipAddress()))
                    || forceFullRegistryFetch
            				// 如果 注册信息是空的
                    || (applications == null)
                    || (applications.getRegisteredApplications().size() == 0)
                    || (applications.getVersion() == -1)) {
        		// 全量拉取
            getAndStoreFullRegistry();
        } else {
            // 增量拉取
            getAndUpdateDelta(applications);
        }
  			->
        // getAndStoreFullRegistry() 进去
          EurekaHttpResponse<Applications> httpResponse = clientConfig.getRegistryRefreshSingleVipAddress() == null
          ? eurekaTransport.queryClient.getApplications(remoteRegionsRef.get())
          : eurekaTransport.queryClient.getVip(clientConfig.getRegistryRefreshSingleVipAddress(), remoteRegionsRef.get());
				->
        getApplications()
        ->
        AbstractJerseyEurekaHttpClient # getApplications()
        // 请求 localhost:7900/eureka/apps
        @Override
        public EurekaHttpResponse<Applications> getApplications(String... regions) {
          return getApplicationsInternal("apps/", regions);
        }

				<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
          
          if (clientConfig.shouldRegisterWithEureka() && clientConfig.shouldEnforceRegistrationAtInit()) {
            try {
              // 注册
              if (!register() ) {
                throw new IllegalStateException("Registration error at startup. Invalid server response.");
              }
            } catch (Throwable th) {
              logger.error("Registration error at startup: {}", th.getMessage());
              throw new IllegalStateException(th);
            }
          }

				// finally, init the schedule tasks (e.g. cluster resolvers, heartbeat, instanceInfo replicator, fetch
        // 初始化, 包含三个任务, 1. 心跳定时任务 2. 缓存刷新, 拉取注册表更及时一些
        // 3. 状态改变监听 statusChangeListener 如果自身服务有变化, 重新注册
        // 第三点 和 EurekaClient 中的 registerHealthCheck() 与 registerEventListener() 有关
        initScheduledTasks();
				1. cacheRefreshTask 缓存刷新
          cacheRefreshTask = new TimedSupervisorTask(
                    "cacheRefresh",
                    scheduler,
                    cacheRefreshExecutor,
                    registryFetchIntervalSeconds,
                    TimeUnit.SECONDS,
                    expBackOffBound,
                    new CacheRefreshThread()
          ->
          class CacheRefreshThread implements Runnable {
            public void run() {
              refreshRegistry();
            }
          }
          ->
          // 这里又调用了 fetchRegistry()
         	boolean success = fetchRegistry(remoteRegionsModified);
        2. 定时心跳
          heartbeatTask = new TimedSupervisorTask(
            "heartbeat",
            scheduler,
            heartbeatExecutor,
            renewalIntervalInSecs, // 隔多少秒定时心跳
            TimeUnit.SECONDS,
            expBackOffBound,
            new HeartbeatThread()
        3. instanceInfoReplicator // 服务实例复制的
            
            // InstanceInfo replicator
            instanceInfoReplicator = new InstanceInfoReplicator(
              this,
              instanceInfo,
              clientConfig.getInstanceInfoReplicationIntervalSeconds(),
              2); // burstSize
            
            // 服务实例状态改变的一个监听器
            statusChangeListener = new ApplicationInfoManager.StatusChangeListener() {
              @Override
              public String getId() {
                return "statusChangeListener";
              }

              @Override
              public void notify(StatusChangeEvent statusChangeEvent) {
                if (InstanceStatus.DOWN == statusChangeEvent.getStatus() ||
                    InstanceStatus.DOWN == statusChangeEvent.getPreviousStatus()) {
                  // log at warn level if DOWN was involved
                  logger.warn("Saw local status change event {}", statusChangeEvent);
                } else {
                  logger.info("Saw local status change event {}", statusChangeEvent);
                }
                // OnDemand 按需
                instanceInfoReplicator.onDemandUpdate();
              }
            };
            
            // OnDemand 按需
            if (clientConfig.shouldOnDemandUpdateStatusChange()) {
              applicationInfoManager.registerStatusChangeListener(statusChangeListener);
            }
            
            // clientConfig.getInitialInstanceInfoReplicationIntervalSeconds() 默认 40s
            instanceInfoReplicator.start(clientConfig.getInitialInstanceInfoReplicationIntervalSeconds());
            ->
            // 注册前设置为dirty
            instanceInfo.setIsDirty();  // for initial register
            
            InstanceInfoReplicator # run() 方法
            public void run() {
              try {
                discoveryClient.refreshInstanceInfo();
                // -> applicationInfoManager.refreshDataCenterInfoIfRequired(); 刷新数据中心info
                // 如果已经存在的地址和新的地址不相等 updateInstanceInfo(newAddress, newIp);
                // instanceInfo.setIsDirty(); 又设置为dirty. 如果地址 newAddress, newIp 有变化, 就设置为脏
                // 脏: 自己的实例和server端的 不一致
                // 如果 ip address 发生变化, 发起一次重新注册 --> 按需注册
                // 为什么 ip address 会发生变化? 动态刷新
                
                // -> applicationInfoManager.refreshLeaseInfoIfRequired(); 刷新租约info

                Long dirtyTimestamp = instanceInfo.isDirtyWithTime();
                if (dirtyTimestamp != null) {
                  // 注册
                  // httpResponse = eurekaTransport.registrationClient.register(instanceInfo);
                  // ->
                  // AbstractJerseyEurekaHttpClient # register()
                  // 发送http请求
                  // String urlPath = "apps/" + info.getAppName();
                  discoveryClient.register();
                  // 注册完设置为 unDirty
                  instanceInfo.unsetIsDirty(dirtyTimestamp);
                }
              } catch (Throwable t) {
                logger.warn("There was a problem with the instance info replicator", t);
              } finally {
                Future next = scheduler.schedule(this, replicationIntervalSeconds, TimeUnit.SECONDS);
                scheduledPeriodicRef.set(next);
              }
            }
            
```



## defaultZone 配置个数问题

```java
question:

写三个. 如果写四台, 第四个没用
eureka.client.service-url.defaultZone: 
				http://localhost:7900/eureka/,  http://localhost:7901/eureka/,  http://localhost:7902/eureka/
client最开始只会给第一个去注册, 如果注册失败才会去找第二个.
如果第一个正常, 也不会拉取后面的
  
RetryableEurekaHttpClient 类
protected <R> EurekaHttpResponse<R> execute(RequestExecutor<R> requestExecutor) {
  // numberOfRetries 默认为3. 所以如果 defaultZone 中注册中心配了4台, 第四台是没用的
  for (int retry = 0; retry < numberOfRetries; retry++) {
  }
}
  

实际工作中, 要把 defaultZone 后面的url随机打乱, 不让某一个server 的压力过大 (从同一个server拉取注册表)
  
任何一个server, 都得把对方所有的同伴都配上. eureka 优化: 我 / 我的同伴
```



## 总结



![06-注册中心总结](https://tva1.sinaimg.cn/large/e6c9d24egy1h5q3o63ccrj20s71hs0wn.jpg)



# 验证码

![07-乘客发送验证码-qps提升](https://tva1.sinaimg.cn/large/e6c9d24ely1h5sg5km3opj20u010s417.jpg)



![08-提升qps技巧 减少响应时间](https://tva1.sinaimg.cn/large/e6c9d24ely1h5y56nx0nbj20v60u0t9q.jpg)

```java
小tips: 生产中不能用快照 SNAPSHOT 版本 (如果SNAP有变化 每次都会拉最新的 没经过测试就直接使用)
  
写impl 一个好处: 
	1. 如果遇到必须使用动态代理的实现方式, 就最好写接口+实现impl ---> 美团点评cat 必须用 接口+实现类 的方式
  2. 遇到多实例的抽象出来
    
随机验证码:
	1. (Math.random()+"").substring(2,8); // 都是6位. Math.random() 生成 0.266128937128937, 取2~8位. 问题在于效率, 时间耗费多
	2. String.valueOf(new Random().nextInt(1000000)); // 会有不是6位的情况
	数字的运算比字符串操作节省时间:
		String code = String.valueOf((int)((Math.random()*9+1)*Math.pow(10,5)));
	
常用的, 不变的, 用缓存: 短信模板
IO瓶颈: 网络IO / 磁盘IO
  
估算线程数:
	16核, 应该开几个线程?
  公式: 线程数 = cpu 可用核数 / 1-阻塞系数
  阻塞系数: io密集型接近1, 计算密集型接近0
  
提高qps: 
	1. 提高并发数 
    1.1 多线程
    1.2 增加各种连接数 mysql redis tomcat 线程池
    1.3 服务无状态, 便于横向拓展. 扩机器
    1.4 让服务能力对等. (serverUrl: 打乱顺序)
  2. 减少响应时间
    2.1 异步 (最终一致性, 不需要及时). 流量削峰
    2.2 缓存. (减少db读取, 减少磁盘io, 读多, 写少)
    2.3 数据库优化
    2.4 多的数据, 分批次返回
    2.5 减少调用链
    2.6 长连接. 不要让客户端去轮询, 减少网络延时
    
  business operation support system
  websocket / sse / netty
    
短信模板, 基本不修改, 只新增
查找短信模板, 为什么不用redis?
    1. 减少网络io
    2. 数据量少
如何估算数据量?
    1. 将模板写入txt文件, 看大小 81B
    2. 81B * 10, 10条短信模板≈1kb
    3. 1w个短信模板 ≈ 1mb
50m ~ 100m, 就可以放在redis了, 主要看数据量
也可以用 @Cache 注解
    
是先存redis 还是先发短信? 
    如果用户拿着验证码来校验, 结果没有.
    所以是先存再发

登陆完成之后, JWT生成的token, 要存在服务端里吗 ?
  	如果客户端违约操作, server要主动剔除一个账号下线, 需要存token (注意是server主动剔除) (接入的<极光>长链接)
    实时剔除必须使用长链接
    不实时, 可以请求一次再踢掉
    
插入 ServicePassengerUserInfo 的时候, 可以用 分布式锁 / 唯一索引
    
PassengerUserServiceImpl:
// 生成 token 的时候，如果要服务端控制，要把它存到 redis 中，再设置过期时间
String token = JwtUtil.createToken(passengerId+"", new Date());
// 存入redis，设置过期时间。
BoundValueOperations<String, String> stringStringBoundValueOperations = redisTemplate.boundValueOps(
				RedisKeyPrefixConstant.PASSENGER_LOGIN_TOKEN_APP_KEY_PRE + passengerId);
stringStringBoundValueOperations.set(token,30,TimeUnit.DAYS);

// 支持微信和app端同时登录, 通过 redis key 来区分是 微信端 还是 小程序端登录.
// 一个app + 一个手机号 只能用一个token
// token 过期可以用 redis, 也可以用自己的过期时间. JwtUtil.createToken # setExpiration(issueDate + xxx)
public static final String PASSENGER_LOGIN_TOKEN_APP_KEY_PRE = "passenger_login_token_app_";
public static final String PASSENGER_LOGIN_TOKEN_WEIXIN_KEY_PRE = "passenger_login_token_weixin_";

jwt不能强制让 token 失效
放 header
  
网关 com.mashibing.cloudzuul
spring boot 三步骤: pom yaml 启动类
鉴权 filter public class AuthFilter extends ZuulFilter
  
  1h


```



