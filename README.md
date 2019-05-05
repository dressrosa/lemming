业务代码层需要处理另外一件事(处理那些不紧急的事),
然后主动去发布或者配置好一个任务,调度中心接受这个任务,然后主动调用任务.
1.可以在代码里写规则,或者配置管理台配置规则,然后按照所制定的规则去调用这个任务接口.称为schedule task
2.无规则,保证调用成功一次.称为temporary task (需要单独考虑)

mq的意思是生产者发布消息,让消费者去消费,一般二者属于俩个独立服务.
task的意思是我有个任务,希望别人来完成这个任务,一般任务和任务逻辑属于同一个服务.

client发布任务,经过Registry,传到server,server将任务存到storage层. schedule层会定期从storage层
拉取数据到exchanger里面进行调度,分配worker去执行任务.
client需要告诉server任务的是哪个服务,哪个接口,哪个方法,server端的worker根据这些信息去回调client任务的方法.

task包含taskId,taskType以及参数,client先设置个taskService(回调的接口)(只能包含一个业务方法),定义好task的业务方法,每次调用的时候,调度平台就会根据这些信息主动调用client端


调用流程:
client项目启动的时候会扫描加了@Task的注解(或配置文件),并将其通过注册中心Registry注册为task,并监听task是否丢失,如果丢失,则重新写入.
server端启动的时候会去监听注册中心Registry里面的task,并将其存入Storage进行持久化,如果监听到task丢失,则置Storage中的任务为下线不可用.
task一旦持久化,将以持久化的task为标准,注册中心Registry对server端来说只是负责task的是否下线判断.除非监控中心手动删除,否则均以taskId+group为唯一标识.
调度中心Schedule主要处理Storage中的任务,并通过transpoter层对client进行调用.client收到transporter的调用时,会解析传过来的task的信息,并找到本地中已经存储的task对象(springbean or new )进行本地调用.

调用分为同步和异步,同步则等待client的调用结果,异步则等待client的回调结果.(待考虑)

监控中心可以对Storage中的任务进行修改.可以观察每个任务的执行情况.
可以手动增加worker来减轻当前所有worker的压力.

zk的作用是快速感知client任务的是否丢失
db的作用是给监控中心对任务操作的灵活性.

三种调用状态:
1.发送调用,无需结果通知
2.发送调用,异步等待通知
3.发送调用,同步等待通知
