package com.xiaoyu.lemming.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
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

import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.mapper.LemmingTaskMapper;

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
        System.out.println("add a new task to storage");
    }

    @Override
    public int batchSave(List<LemmingTask> tasks) {
        SqlSession session = this.sqlSessionFactory.openSession();

        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            List<LemmingTask> list = mapper.getTasks(tasks.get(0).getApp());
            Map<String, LemmingTask> map = list.stream().collect(Collectors.toMap(LemmingTask::getTaskId, a -> a));
            List<LemmingTask> insertList = new ArrayList<>();
            for (LemmingTask t : tasks) {
                if (!map.containsKey(t.getTaskId())) {
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
        String resource = "mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        DataSource dataSource = new PooledDataSource("com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://127.0.0.1:3306/lemming", "root", "1234");
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration config = sqlSessionFactory.getConfiguration();
        config.setEnvironment(environment);
        config.setLazyLoadingEnabled(true);
        config.setUseGeneratedKeys(true);
        config.setMultipleResultSetsEnabled(true);
        config.setLogPrefix(".dao");
        config.setCacheEnabled(true);
        config.setAggressiveLazyLoading(true);
        config.setMapUnderscoreToCamelCase(true);
        config.setJdbcTypeForNull(JdbcType.NULL);
    }

    @Override
    public LemmingTask fetch() {
        SqlSession session = this.sqlSessionFactory.openSession();
        LemmingTask task = null;
        try {
            LemmingTaskMapper mapper = session.getMapper(LemmingTaskMapper.class);
            task = mapper.getOneTask("helloTask");
        } finally {
            session.close();
        }
        return task;
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
}
