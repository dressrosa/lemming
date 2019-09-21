task的意思是我有个任务,希望别人来完成这个任务,一般任务和任务逻辑属于同一个服务.
### 总体设计:
![基本原理](https://dressrosa.github.io/resources/lemming/lemming_struct.png)
### 调用流程:
1.client发布任务,经过Registry,传到server,server将任务存到storage层.  
2. server端schedule层会定期从storage层拉取数据到exchanger里面进行调度,分配worker去执行任务.  
3. worker检测需要执行的任务,并通过transpoter层对client进行调用.  
4. client收到transporter的调用时,会解析传过来的task的信息,并找到本地中已经存储的task对象(springbean or new )进行本地调用.(调用分为同步和异步,同步则等待client的调用结果,异步则等待client的回调结果.)  
5. 调用结束后回调结果给server.

### Client:
1.client初始化的所有task,并指定了任务的实现用于Server去调用(调度不保证唯一性,如果需要严格的唯一性,任务的业务逻辑需要处理.)初始化的任务都没有group,必须在monitor进行指定group并启用.
### Server:
1.server启动时会初始化所有的zk的数据到db,并监听所有新上的client信息.
### Registry:
注册中心暂时只支持zookeeper,由client进行任务注册,Server进行监听获取.
### Monitor:
monitor属于web项目,可单独使用.可以由其对所有任务进行查看和管理,但是同时monitor本身也是一个Server.  
每个任务都必须先指定组别并启用.监控中心可以对Storage中的任务进行修改.可以观察每个任务的执行情况.(可以手动增加worker来减轻当前所有worker的压力[待考虑]).  
目前只是实现简单的管理功能.
![monitor](https://dressrosa.github.io/resources/lemming/lemming_monitor_01.png)

