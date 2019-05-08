/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.lemming.registry.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.AbstractRegistry;
import com.xiaoyu.lemming.storage.Storage;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class ZooRegistry extends AbstractRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ZooRegistry.class);

    private static final String ROOT = "/lemming";

    private static final String SERVERS = "/server";

    private static final String CLIENTS = "/client";

    private ZooUtil zoo;

    /**
     * consumer or provider path -> listener
     */
    private static final ConcurrentMap<String, IZkChildListener> CHILD_LISTENER_MAP = new ConcurrentHashMap<>(32);

    /**
     * 用来监听provider异常丢失
     */
    private static ScheduledExecutorService providerMonitor;

    @Override
    public void address(String addr) {
        try {
            zoo = ZooUtil.zoo(addr);
            IZkStateListener listener = new IZkStateListener() {
                @Override
                public void handleStateChanged(KeeperState state) throws Exception {
                    LOG.info("Get zookeeper state->" + state.name());
                }

                @Override
                public void handleSessionEstablishmentError(Throwable error) throws Exception {
                    LOG.error("HandleSessionEstablishmentError->", error);
                }

                @Override
                public void handleNewSession() throws Exception {

                }
            };
            zoo.subscribeStateChanges(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        zoo.createPersistent(ROOT);
    }

    @Override
    public boolean isInit() {
        ZooUtil tzoo = zoo;
        return tzoo == null ? false : true;
    }

    // 仅用于server端监听
    @Override
    public void initServerListener() {
        // server启动时,监听新的task
        // server启动时 监听已经存在的task
        IZkChildListener rootListener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                LOG.info(" Client Task changed:parentPath->{},currentChilds->{}", parentPath, currentChilds);
                if (currentChilds == null) {
                    return;
                }
                doServerResolveInfo(parentPath, currentChilds);
            }

        };
        zoo.subscribeChildChanges(ROOT, rootListener);
        // 保存listener
        CHILD_LISTENER_MAP.putIfAbsent(ROOT, rootListener);

        List<String> rootChildren = zoo.children(ROOT);
        if (!rootChildren.isEmpty()) {
            for (String t : rootChildren) {
                IZkChildListener listener = new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        LOG.info(" Client Task changed:parentPath->{},currentChilds->{}", parentPath, currentChilds);
                        if (currentChilds == null) {
                            return;
                        }
                        doServerResolveInfo(parentPath, currentChilds);
                    }
                };
                zoo.subscribeChildChanges(ROOT + "/" + t + CLIENTS, listener);
                // 保存listener
                CHILD_LISTENER_MAP.putIfAbsent(ROOT + "/" + t + CLIENTS, listener);
            }
        }
        LOG.info("Init server listener task in zookeeper->{}", ROOT);
    }

    private void doServerResolveInfo(String parentPath, List<String> currentChilds) {
        if (ROOT.equals(parentPath)) {
            // 新task上线, 已经存在的是永久节点 不会消失
            List<LemmingTask> tasks = new ArrayList<>();
            for (String t : currentChilds) {
                List<String> tcs = zoo.children(ROOT + "/" + t + CLIENTS);
                if (!tcs.isEmpty()) {
                    for (String p : tcs)
                        tasks.add(LemmingTask.toEntity(p));
                }
            }
            if (!tasks.isEmpty()) {
                try {
                    Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                    storage.batchSave(tasks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // 已存在的task发生改变,掉线或下线,或新增节点,或重新上线
            if (currentChilds.isEmpty()) {
                return;
            }
            List<LemmingTask> tasks = new ArrayList<>();
            for (String p : currentChilds) {
                tasks.add(LemmingTask.toEntity(p));
            }
            try {
                // Storage storage = SpiManager.defaultSpiExtender(Storage.class);
                // storage.batchSave(tasks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void registerTask(LemmingTask task) {
        // client端由配置文件或代码写入, server端由操作中心操作写入
        // 找到需要暴漏的service,然后写入providers信息,或者客户端启动写入consumers信息
        String detailInfo = task.toPath();
        String service = task.getTaskImpl();
        zoo.createPersistent(ROOT + "/" + service);
        String path = null;
        // server端监听client,client监听server端的
        IZkChildListener listener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                LOG.info(" Task changed:parentPath->{},currentChilds->{}", parentPath, currentChilds);
                if (currentChilds == null) {
                    return;
                }
                // doResolveInfo(parentPath, currentChilds);
            }
        };
        // 初始化service父节点
        if ("client".equals(task.getSide())) {
            path = this.clientPath(service);
            zoo.createPersistent(path);
            LOG.info("Register client task to zookeeper->{}", (path + "/" + detailInfo));
            zoo.createEphemeral(this.fullPath(path, detailInfo));
            // 监听操作在执行的时候,如果path不存在,会生成临时节点,但是临时节点是不能创建子节点的,这里首先判断下
            String serverPath = this.serverPath(service);
            zoo.createPersistent(serverPath);
            zoo.subscribeChildChanges(serverPath, listener);
            LOG.info("Subscribe server task in zookeeper->{}", serverPath);
        } else {
            path = this.serverPath(service);
            zoo.createPersistent(path);
            LOG.info("Register server task to zookeeper->{}", (path + "/" + detailInfo));
            zoo.createEphemeral(this.fullPath(path, detailInfo));
            // 进行监听某一个具体service,这里会多次调用 ,但是只监听第一个就够了
            // this.monitorProvider(service, detailInfo);

            String clientPath = this.clientPath(service);
            zoo.createPersistent(clientPath);
            zoo.subscribeChildChanges(clientPath, listener);
            LOG.info("Subscribe client task in zookeeper->{}", clientPath);
        }
        // 保存listener
        CHILD_LISTENER_MAP.putIfAbsent(path, listener);
        super.storeLocalTask(task);
    }

    @Override
    public List<String> getAllTask() {

        return null;
    }

    @Override
    public void close() {
        // 还没执行到监视器可能就结束了
        if (providerMonitor != null) {
            // 关闭检查器
            providerMonitor.shutdown();
        }
        ZooUtil tzoo = zoo;
        // 关闭所有
        tzoo.unsubscribeAll();
        tzoo.close();
    }

    private final String serverPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROOT).append("/").append(service).append(SERVERS);
        return builder.toString();
    }

    private final String fullPath(String path, String detailInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append(path).append("/").append(detailInfo);
        return builder.toString();
    }

    private final String clientPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROOT).append("/").append(service).append(CLIENTS);
        return builder.toString();
    }

    @Override
    public boolean discoverService(String service) {
        // TODO Auto-generated method stub
        return false;
    }

}
