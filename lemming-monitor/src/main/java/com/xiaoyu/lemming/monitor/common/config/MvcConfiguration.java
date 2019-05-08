/**
 * 不要因为走了很远就忘记当初出发的目的:whatever happened,be yourself
 */
package com.xiaoyu.lemming.monitor.common.config;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * @author hongyu
 * @date 2019-05
 * @description
 */
@Configuration
@EnableScheduling
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    /**
     * 解决@{@link responseBody}中文乱码问题
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter1 = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        // alibaba json转化
        FastJsonHttpMessageConverter converter2 = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.UseISO8601DateFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty);
        converter2.setFastJsonConfig(fastJsonConfig);
        converters.add(converter1);
        converters.add(converter2);
    }
}
