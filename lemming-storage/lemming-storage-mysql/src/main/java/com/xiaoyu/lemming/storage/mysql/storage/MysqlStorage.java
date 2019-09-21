package com.xiaoyu.lemming.storage.mysql.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.constant.ExecuteStateEnum;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.common.util.TimeUtils;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.storage.Storage;
import com.xiaoyu.lemming.storage.mysql.mapper.LemmingTaskMapper;
import com.xiaoyu.lemming.storage.mysql.query.LemmingTaskClientQuery;
import com.xiaoyu.lemming.storage.mysql.query.LemmingTaskQuery;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class MysqlStorage implements Storage {

    private static final Logger logger = LoggerFactory.getLogger(MysqlStorage.class);

    private SqlSessionFactory sqlSessionFactory = null;

    public MysqlStorage() {
    }

    private void api(String url, String user, String password) {
        DataSource dataSource = new PooledDataSource("com.mysql.cj.jdbc.Driver",
                url, user, password);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration config = new Configuration(environment);
        config.setEnvironment(environment);
        config.setLazyLoadingEnabled(true);
        config.setUseGeneratedKeys(true);
        config.setMultipleResultSetsEnabled(true);
        if (logger.isDebugEnabled()) {
            config.setLogPrefix("dao.");
            config.setLogImpl(StdOutImpl.class);
        }
        config.setCacheEnabled(true);
        config.setAggressiveLazyLoading(true);
        config.setMapUnderscoreToCamelCase(true);
        config.setJdbcTypeForNull(JdbcType.NULL);
        config.getTypeAliasRegistry().registerAlias(LemmingTask.class);
        config.addMapper(LemmingTaskMapper.class);
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Override
    public void init(String url, String user, String password) {
        logger.info(" Connect to storage mysql");
        api(url, user, password);
    }

    @Override
    public void insert(LemmingTask task) {
        SqlSession session = this.sqlSessionFactory.openSession();
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            LemmingTask t = mapper.getOneTask(task.getApp(), task.getTaskId());
            if (t != null) {
                return;
            }
            try {
                mapper.insert(task);
                session.commit();
            } catch (Exception e) {
                logger.error("" + e);
                session.rollback();
            }
        } finally {
            session.close();
        }
    }

    @Override
    public int batchSave(List<LemmingTask> tasks) {
        if (tasks.isEmpty()) {
            return 0;
        }
        List<String> apps = new ArrayList<>();
        List<String> taskIds = new ArrayList<>();
        tasks.forEach(a -> {
            apps.add(a.getApp());
            taskIds.add(a.getTaskId());
        });
        LemmingTaskQuery q = new LemmingTaskQuery();
        q.setApps(apps);
        q.setTaskIds(taskIds);
        LemmingTaskClientQuery cq = new LemmingTaskClientQuery();
        cq.setApps(apps);
        cq.setTaskIds(taskIds);

        SqlSession session = this.sqlSessionFactory.openSession();
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            List<LemmingTask> list = mapper.getTasks(q);
            List<LemmingTaskClient> clientList = mapper.getTaskClients(cq);

            Map<String, LemmingTask> map = list.stream().collect(Collectors.toMap(a -> {
                return a.getApp() + "_" + a.getTaskId();
            }, a -> a));

            List<LemmingTask> insertList = new ArrayList<>();
            List<LemmingTaskClient> insertClientList = new ArrayList<>();
            List<LemmingTaskClient> deleteClientList = new ArrayList<>();
            Map<String, LemmingTaskClient> clientMap = new HashMap<>();

            clientList.forEach(a -> {
                String key = a.getApp() + "_" + a.getTaskId() + "_" + a.getHost();
                if (clientMap.containsKey(key)) {
                    deleteClientList.add(a);
                } else {
                    clientMap.put(a.getApp() + "_" + a.getTaskId() + "_" + a.getHost(), a);
                }
            });

            for (LemmingTask t : tasks) {
                if (!map.containsKey(t.getApp() + "_" + t.getTaskId())) {
                    insertList.add(t);
                }
                if (!clientMap.containsKey(t.getApp() + "_" + t.getTaskId() + "_" + t.getHost())) {
                    LemmingTaskClient client = new LemmingTaskClient()
                            .setApp(t.getApp())
                            .setHost(t.getHost())
                            .setTaskId(t.getTaskId());
                    insertClientList.add(client);
                } else {
                    clientMap.remove(t.getApp() + "_" + t.getTaskId() + "_" + t.getHost());
                }
            }
            deleteClientList.addAll(clientMap.values());
            try {
                if (!insertList.isEmpty()) {
                    mapper.batchInsert(insertList);
                }
                if (!deleteClientList.isEmpty()) {
                    mapper.batchDeleteTaskClients(deleteClientList);
                }
                if (!insertClientList.isEmpty()) {
                    mapper.batchInsertTaskClients(insertClientList);
                }
                session.commit();
            } catch (Exception e) {
                logger.error("" + e);
                session.rollback(true);
            }

        } finally {
            session.close();
        }
        return 1;
    }

    // private void xml() {
    // String resource = "mybatis-config.xml";
    // InputStream inputStream = null;
    // try {
    // inputStream = Resources.getResourceAsStream(resource);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // }

    @Override
    public LemmingTask fetch() {
        return null;
    }

    @Override
    public List<LemmingTask> fetchTasks() {
        SqlSession session = sqlSessionFactory.openSession();
        List<LemmingTask> tasks = null;
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            tasks = mapper.getTasks(null);
        } finally {
            session.close();
        }
        return tasks;
    }

    @Override
    public int insertLog(String taskId, String msg, boolean isError) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<LemmingTask> fetchAllTasks() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            LemmingTaskQuery query = new LemmingTaskQuery();
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            Long count = mapper.count(query);
            long pageSize = 256;// 2^8
            long page = (count + pageSize - 1) / pageSize;
            long startNum = 0;
            query.setPageSize(pageSize);
            List<LemmingTask> total = new ArrayList<>(count.intValue());
            List<LemmingTask> tasks = new ArrayList<>(0);
            for (int i = 0; i < page; i++) {
                query.setStartNum(startNum);
                tasks = mapper.getTasks(query);
                List<String> apps = new ArrayList<>(tasks.size());
                List<String> taskIds = new ArrayList<>(tasks.size());
                Map<String, LemmingTask> tasksMap = new HashMap<>(tasks.size());
                if (tasks != null) {
                    tasks.forEach(a -> {
                        apps.add(a.getApp());
                        taskIds.add(a.getTaskId());
                        tasksMap.put(a.getApp() + "_" + a.getTaskId(), a);
                    });
                }
                if (!apps.isEmpty()) {
                    LemmingTaskClientQuery cQuery = new LemmingTaskClientQuery();
                    cQuery.setApps(apps);
                    cQuery.setTaskIds(taskIds);
                    List<LemmingTaskClient> clients = mapper.getTaskClients(cQuery);
                    clients.forEach(a -> {
                        LemmingTask t = tasksMap.get(a.getApp() + "_" + a.getTaskId());
                        if (t != null) {
                            t.getClients().add(a);
                        }
                    });
                }
                if (tasks != null && !tasks.isEmpty()) {
                    total.addAll(tasks);
                }
                startNum += pageSize;
            }
            return total;
        } finally {
            session.close();
        }
    }

    @Override
    public List<LemmingTask> fetchUpdatedTasks() {
        SqlSession session = sqlSessionFactory.openSession();
        List<LemmingTask> tasks = null;
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            tasks = mapper
                    .getUpdatedTasks(TimeUtils.format(
                            TimeUtils.addMinutes(new Date(), -1), "yyyy-MM-dd HH:mm:ss"));
        } finally {
            session.close();
        }
        return tasks;
    }

    @Override
    public int saveLog(LemmingTask task, ExecuteResult ret) {
        LemmingTaskLog taskLog = new LemmingTaskLog();
        taskLog.setApp(task.getApp())
                .setTaskId(task.getTaskId())
                .setHost(ret.getHost() == null ? "" : ret.getHost())
                .setMessage(ret.getMessage() == null ? "" : ret.getMessage())
                .setTraceId(ret.getTraceId());
        if (ret.isSuccess()) {
            taskLog.setState(ExecuteStateEnum.Success.ordinal());
        } else {
            taskLog.setState(ExecuteStateEnum.Failed.ordinal());
        }
        SqlSession session = this.sqlSessionFactory.openSession();
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            mapper.insertLog(taskLog);
            session.commit();
        } finally {
            session.close();
        }
        return 1;
    }

    @Override
    public int removeTaskClientsByImpl(String taskImpl) {
        if (StringUtils.isBlank(taskImpl)) {
            return 0;
        }
        LemmingTaskQuery q = new LemmingTaskQuery();
        q.setTaskImpl(taskImpl);

        List<String> apps = new ArrayList<>();
        List<String> taskIds = new ArrayList<>();

        LemmingTaskClientQuery cq = new LemmingTaskClientQuery();

        SqlSession session = this.sqlSessionFactory.openSession();
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            List<LemmingTask> list = mapper.getTasks(q);
            if (list.isEmpty()) {
                return 0;
            }
            list.forEach(a -> {
                apps.add(a.getApp());
                taskIds.add(a.getTaskId());
            });
            cq.setApps(apps);
            cq.setTaskIds(taskIds);
            List<LemmingTaskClient> clientList = mapper.getTaskClients(cq);
            try {
                if (!clientList.isEmpty()) {
                    mapper.batchDeleteTaskClients(clientList);
                }
                session.commit();
            } catch (Exception e) {
                logger.error("" + e);
                session.rollback(true);
            }

        } finally {
            session.close();
        }
        return 1;
    }

}
