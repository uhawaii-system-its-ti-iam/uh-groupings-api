package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberAttributeServiceTest {

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Test
    public void construction() {
        assertNotNull(memberAttributeService);
    }
}
