/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemming.transport;

import com.alibaba.fastjson.JSON;
import com.xiaoyu.lemming.common.constant.CommonConstant;
import com.xiaoyu.lemming.common.entity.ExecuteResult;
import com.xiaoyu.lemming.common.extension.SpiManager;
import com.xiaoyu.lemming.core.api.Context;
import com.xiaoyu.lemming.core.api.LemmingTask;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * @author:xiaoyu
 * @date:2019年12月
 * 
 */
public class NettyHttpListenerHandler extends ChannelInboundHandlerAdapter {

    /**
     * client时代表com.xiaoyu.lemming.client.LemmingServiceClientImpl
     * server时代表com.xiaoyu.lemming.schedule.LemmingServiceServerImpl
     */
    private static Object Handler_INSTANCE = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = null;
        if (msg instanceof HttpContent) {
            request = (FullHttpRequest) msg;
        }
        Context context = SpiManager.defaultSpiExtender(Context.class);
        FullHttpResponse response = null;
        if (CommonConstant.Client.equals(context.side())) {
            LemmingTask task = JSON.parseObject(request.content().toString(CharsetUtil.UTF_8), LemmingTask.class);
            ExecuteResult result = doHandleCallPost(task);
            ByteBuf content = Unpooled.wrappedBuffer(JSON.toJSONString(result).getBytes());
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    content);
        } else {
            ExecuteResult ret = JSON.parseObject(request.content().toString(CharsetUtil.UTF_8), ExecuteResult.class);
            this.doHandleCallbackPost(ret);
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        }
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                "application/json; charset=utf-8");
        ctx.channel().writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }

    private ExecuteResult doHandleCallPost(LemmingTask task) throws Exception {
        Object result = null;
        if (Handler_INSTANCE == null) {
            Class<?> lemmingClient = Class.forName(CommonConstant.Lemming_Service_Client_Impl);
            Handler_INSTANCE = lemmingClient.newInstance();
            result = lemmingClient
                    .getMethod(CommonConstant.Task_Call_Method, LemmingTask.class)
                    .invoke(Handler_INSTANCE, task);
        } else {
            result = Handler_INSTANCE.getClass()
                    .getMethod(CommonConstant.Task_Call_Method, LemmingTask.class)
                    .invoke(Handler_INSTANCE, task);
        }
        return (ExecuteResult) result;
    }

    private void doHandleCallbackPost(ExecuteResult ret) throws Exception {
        if (Handler_INSTANCE == null) {
            Class<?> lemmingServer = Class.forName(CommonConstant.Lemming_Service_Server_Impl);
            Handler_INSTANCE = lemmingServer.newInstance();
            lemmingServer
                    .getMethod(CommonConstant.Task_Callback_Method, ExecuteResult.class)
                    .invoke(Handler_INSTANCE, ret);
        } else {
            Handler_INSTANCE.getClass()
                    .getMethod(CommonConstant.Task_Callback_Method, ExecuteResult.class)
                    .invoke(Handler_INSTANCE, ret);
        }
    }

}