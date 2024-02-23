package edu.hawaii.its.api.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToOptTypeConverter());
        registry.addConverter(new StringToPreferenceStatusConverter());
        registry.addConverter(new StringToFeedbackConverter());
    }
}
