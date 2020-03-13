/**
 * 不要因为走了很远就忘记当初出发的目的:whatever happened,be yourself
 */
package com.xiaoyu.lemming.monitor.common.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

/**
 * @author xiaoyu
 * @date 2019-05
 * @description
 */
@Configuration
@EnableScheduling
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    /**
     * springboot 默认静态资源访问路径
     */
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/",
            "classpath:/resources/", "classpath:/static/", "classpath:/public/" };

    // 前缀
    @Value("${spring.mvc.view.prefix}")
    private String prefix;
    // 后缀
    @Value("${spring.mvc.view.suffix}")
    private String suffix;

    /**
     * 设置起始欢迎页
     */
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//       // registry.addViewController("/").setViewName("task/taskList");
//        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
//        super.addViewControllers(registry);
//    }

    /**
     * 解决@{@link responseBody}中文乱码问题
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter1 = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        // alibaba json转化
        FastJsonHttpMessageConverter converter2 = new FastJsonHttpMessageConverter();
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.ALL);
        converter2.setSupportedMediaTypes(supportedMediaTypes);
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.UseISO8601DateFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty);
        converter2.setFastJsonConfig(fastJsonConfig);
        converters.add(converter1);
        converters.add(converter2);
    }

    /**
     * 设置注解{@link EnableWebMvc} 会破坏默认的静态资源访问路径 设置静态资源访问路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // TODO Auto-generated method stub
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
        // // 将访问图片的地址映射到具体文件夹
        // if (!registry.hasMappingForPattern("/images/**")) {
        // registry.addResourceHandler("/images/**").addResourceLocations("file:" + disk
        // + imagesDir);
        // }
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
        }
    }

    /**
     * 下载设置
     * 
     * @author xiaoyu
     * @return
     * @time 2016年3月29日上午9:09:15
     */
//    @Bean
//    public MultipartResolver multipartResolver() {
//        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
//        multipartResolver.setMaxUploadSize(5000000);
//        return multipartResolver;
//    }

    /**
     * 视图解析器
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // TODO Auto-generated method stub
        super.configureViewResolvers(registry);
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix(prefix);
        resolver.setSuffix(suffix);
        resolver.setOrder(0);
        registry.viewResolver(resolver);
        // 设置多个视图解析器是没用的
        // InternalResourceViewResolver resolver1= new
        // InternalResourceViewResolver();
        // resolver1.setPrefix("/WEB-INF/");
        // resolver1.setSuffix(".html");
        // resolver1.setViewNames("html*");
        // resolver1.setOrder(1);
        // registry.viewResolver(resolver1);
    }

}
