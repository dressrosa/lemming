package com.xiaoyu.lemming.storage.mysql.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

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
import com.xiaoyu.lemming.common.entity.LemmingTaskLog;
import com.xiaoyu.lemming.common.util.TimeUtils;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.storage.Storage;
import com.xiaoyu.lemming.storage.mysql.mapper.LemmingTaskMapper;
import com.xiaoyu.lemming.storage.mysql.query.LemmingTaskQuery;

/**
 * @author hongyu
 * @date 2019-03
 * @description
 */
public class MysqlStorage implements Storage {

    private static final Logger logger = LoggerFactory.getLogger(MysqlStorage.class);

    private SqlSessionFactory sqlSessionFactory = null;

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
                e.printStackTrace();
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

        SqlSession session = this.sqlSessionFactory.openSession();
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            List<LemmingTask> list = mapper.getTasks(q);
            Map<String, LemmingTask> map = list.stream().collect(Collectors.toMap(a -> {
                return a.getApp() + "_" + a.getTaskId();
            }, a -> a));
            List<LemmingTask> insertList = new ArrayList<>();
            for (LemmingTask t : tasks) {
                if (!map.containsKey(t.getApp() + "_" + t.getTaskId())) {
                    insertList.add(t);
                }
            }
            if (!insertList.isEmpty()) {
                try {
                    mapper.batchInsert(insertList);
                    session.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    session.rollback();
                }
            }
        } finally {
            session.close();
        }
        return 0;
    }

    public MysqlStorage() {
        logger.info(" Connect to storage mysql");
        api();
    }

    private void api() {
        DataSource dataSource = new PooledDataSource("com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://127.0.0.1:3306/lemming", "root", "1234");
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
        List<LemmingTask> tasks = null;
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            tasks = mapper.getTasks(new LemmingTaskQuery());
        } finally {
            session.close();
        }
        return tasks;

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
        taskLog.setGroup(task.getGroup())
                .setApp(task.getApp())
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

}
