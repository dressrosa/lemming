/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemming.transport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.entity.LemmingTaskClient;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author xiaoyu
 * @date 2019-11
 * @description
 */
public class HttpTransporter extends AbstractTransporter {

    private static final Logger logger = LoggerFactory.getLogger(HttpTransporter.class);

    public static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient okClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(3600, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build();

    @Override
    public void callback(ExecuteResult result) throws Exception {
        String json = JSON.toJSONString(result);
        RequestBody body = RequestBody.create(json.getBytes(),
                JSON_MEDIA);
        Request request = new Request.Builder()
                .post(body)
                // TODO
                .url("http://" + result.getDispatchHost() + ":" + HttpPortConstant.Server_Port)
                .build();
        Response response = null;
        try {
            response = okClient.newCall(request).execute();
        } catch (IOException e) {
            logger.error("", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public void export() throws Exception {
        Thread t = new Thread(() -> {
            try {
                Context context = SpiManager.defaultSpiExtender(Context.class);
                int port = 0;
                // TODO 正常server和client不会是一台机器 这里为了方便一台机器测试,就分俩个端口
                if (CommonConstant.Client.equals(context.side())) {
                    port = HttpPortConstant.Client_Port;
                } else {
                    port = HttpPortConstant.Server_Port;
                }
                new NettyHttpListener().run(port);
                logger.info("lemming http listener start in port:{}", port);
            } catch (Exception e) {
                logger.error("", e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public ExecuteResult doCall(LemmingTask task, LemmingTaskClient client) throws Exception {
        String json = JSON.toJSONString(task);
        RequestBody body = RequestBody.create(json.getBytes(),
                JSON_MEDIA);
        Request request = new Request.Builder()
                // TODO
                // .url("http://localhost:6666")
                .url("http://" + client.getExecutionHost() + ":" + HttpPortConstant.Client_Port)
                .post(body)
                .build();
        Response response = null;
        try {
            response = okClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return JSON.parseObject(response.body().string(), ExecuteResult.class);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return new ExecuteResult().setTaskId(task.getTaskId())
                .setApp(task.getApp())
                .setGroup(task.getTaskGroup())
                .setTraceId(task.getTraceId())
                .setExecutionHost(client.getExecutionHost())
                .setDispatchHost(task.getDispatchHost())
                .setSuccess(false)
                .setMessage(response == null ? "call failed.unknown reason." : response.toString());
    }

}
