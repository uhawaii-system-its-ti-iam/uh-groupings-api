package edu.hawaii.its.api.service;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.JavaVersionException;

@Profile(value = { "localhost" })
@PropertySource("classpath:application-localhost.properties")
@Service
public class JavaVersionChecker {

    @Value("${java.specification.version.api}")
    private String javaSpecificationVersion;

    @PostConstruct
    public void init() throws RuntimeException {
        if (!System.getProperty("java.version").startsWith(javaSpecificationVersion)) {
            throw new JavaVersionException(javaSpecificationVersion);
        }
    }

}
