package com.xiaoyu.lemming.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.Exchanger;
import com.xiaoyu.lemming.core.api.LemmingTask;
import com.xiaoyu.lemming.registry.Registry;

/**
 * @author hongyu
 * @date 2019-04
 * @description
 */
public class ServerContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(ServerContext.class);

    // public static void main(String[] args) {
    // CronDefinition def =
    // CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
    // CronParser parser = new CronParser(def);
    // long start = System.currentTimeMillis();
    // Cron cron = parser.parse("0 0 7,13,19 * * ?");
    // ZonedDateTime now = ZonedDateTime.now();
    // ExecutionTime executionTime = ExecutionTime.forCron(cron);
    // ZonedDateTime nextExecution = executionTime.nextExecution(now).get();
    // long end = System.currentTimeMillis();
    // DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // System.out.println(nextExecution.format(f));
    // System.out.println("cost:" + (end - start));
    // System.out.println(Timestamp.valueOf(LocalDateTime.now()).getTime() / 1000);
    // System.out.println(Timestamp.valueOf(LocalDateTime.now().plusSeconds(1)).getTime()
    // / 1000);
    // System.out.println(Timestamp.valueOf(nextExecution.toLocalDateTime()).getTime()
    // / 1000);
    // System.out.println(now.isEqual(ZonedDateTime.now()));
    // long a = Timestamp.valueOf(LocalDateTime.now()).getTime() -
    // Timestamp.valueOf(LocalDateTime.now().plusSeconds(1)).getTime();
    // System.out.println(LocalDateTime.now().compareTo(LocalDateTime.now().plusSeconds(1)));
    // }

    private Registry registry;

    private Exchanger exchanger;

    public ServerContext() {
    }

    @Override
    public String side() {
        return "server";
    }

    @Override
    public void start() {
        try {
            exchanger = SpiManager.defaultSpiExtender(Exchanger.class);
            exchanger.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public LemmingTask getLocalTask(String app, String taskId) {
        try {
            registry = SpiManager.defaultSpiExtender(Registry.class);
            final LemmingTask task = registry.getLocalTask(app, taskId);
            return task;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        logger.info(" Begin ServerContext close");
        if (exchanger != null) {
            exchanger.close();
        }
        if (registry != null) {
            registry.close();
        }
        logger.info(" Complete ServerContext close");
    }

    @Override
    public void initTransporter(String transporter) {
        // do nothing
    }
}
