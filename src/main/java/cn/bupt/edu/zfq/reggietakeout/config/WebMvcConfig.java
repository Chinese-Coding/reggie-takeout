package cn.bupt.edu.zfq.reggietakeout.config;


import cn.bupt.edu.zfq.reggietakeout.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @author zfq
 */
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 扩展 mvc 框架的消息转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器, 实现自定义对象转换器");
        var messageConverter = new MappingJackson2HttpMessageConverter(); // 创建消息转换器对象
        messageConverter.setObjectMapper(new JacksonObjectMapper()); // 设置对象转换器, 底层使用我们定义的对象转换器通过 Jackson 将 java 对象转换为 json
        converters.add(0, messageConverter); // 将上面的消息转换器对象追加到 mvc 框架的转换器集合中 // 0表示最先执行我们的追加的转换器
    }
}
