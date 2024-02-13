package edu.hawaii.its.api.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.hawaii.its.api.service.HappyService;
import edu.hawaii.its.api.service.MoodService;
import edu.hawaii.its.api.service.SadService;

@Configuration
public class MoodConfig {

    @Bean
    @ConditionalOnProperty(value = "mood.service.impl", havingValue = "happy")
    public MoodService happyMoodService() {
        return new HappyService();
    }

    @Bean
    @ConditionalOnProperty(value = "mood.service.impl", havingValue = "sad")
    public MoodService sadMoodService() {
        return new SadService();
    }
}
