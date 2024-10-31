package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@SpringBootTest(classes = { SpringBootWebApplication.class })
class GroupingAttributeServiceTest {

    @Autowired
    private GroupingAttributeService groupingAttributeService;

    @Test
    public void construction() {
        assertNotNull(groupingAttributeService);
    }
}
