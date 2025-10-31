package edu.hawaii.its.api.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
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

    private String SECURITY_SCHEME_NAME = "Bearer Auth";

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(
                        new Components()
                                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                        )
                )
                .info(new Info().title(TITLE)
                .description(DESCRIPTION)
                .version(VERSION)
                .termsOfService(TOS_URL)
                .contact(new Contact().name(CONTACT_NAME).url(CONTACT_URL).email(CONTACT_EMAIL))
                .license(new License().name(LICENSE_NAME).url(LICENSE_URL)));
    }
}
