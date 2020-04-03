package edu.hawaii.its.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;


@Configuration
@EnableConfigurationProperties(APIConfig.class)
@EnableSwagger2
public class SwaggerConfig {


    @Value("${groupings.api.documentation.title}")
    private String TITLE;

    @Value("${groupings.api.documentation.description}")
    private String DESCRIPTION;

    @Value("${groupings.api.documentation.version}")
    private String VERSION;

    @Value("${groupings.api.documentation.tos.url}")
    private String TOS_URL;

    @Value("${groupings.api.documentation.contact.name}")
    private String CONTACT_NAME;

    @Value("${groupings.api.documentation.contact.url}")
    private String CONTACT_URL;

    @Value("${groupings.api.documentation.contact.email}")
    private String CONTACT_EMAIL;

    @Value("${groupings.api.documentation.license.name}")
    private String LICENSE_NAME;

    @Value("${groupings.api.documentation.license.url}")
    private String LICENSE_URL;


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()

                // do this for all endpoints with .any() on both
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())

                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                TITLE,
                DESCRIPTION,
                VERSION,
                TOS_URL,
                new Contact(CONTACT_NAME, CONTACT_URL, CONTACT_EMAIL),
                LICENSE_NAME, LICENSE_URL, Collections.emptyList());
    }

}
