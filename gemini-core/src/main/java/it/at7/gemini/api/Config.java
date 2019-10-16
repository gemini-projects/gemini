package it.at7.gemini.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class Config implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new EntityRecordMessageConverter());
        converters.add(1, new EntityRecordListMessageConverter());
        converters.add(2, new EntityRecordApiTypeMessageConverter());
        converters.add(3, new EntityRecordListApiMessageConverter());
        converters.add(4, new CountRequestMessageConverter());
        converters.add(5, new CountRequestApiMessageConverter());
    }
}
