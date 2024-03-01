package edu.hawaii.its.api.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.EmailService;
import edu.hawaii.its.api.type.EmailResult;
import edu.hawaii.its.api.type.Feedback;
import edu.hawaii.its.api.util.JsonUtil;

@SpringBootTest(classes = { SpringBootWebApplication.class })
class EmailRestControllerTest {

    private static final String BASE_URL = "/email";

    @Value("${groupings.api.current_user}")
    private String CURRENT_USER;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private EmailService emailService;

    private MockMvc mockMvc;

    private static final String ADMIN = "admin";

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(context).build();
    }

    @Test
    public void sendFeedbackTest() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setExceptionMessage("exceptionMessage");
        given(emailService.sendFeedback(eq(ADMIN), refEq(feedback))).willReturn(new EmailResult());

        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "/send/feedback")
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.asJson(feedback)))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);

        verify(emailService, times(1)).sendFeedback(eq(ADMIN), refEq(feedback));
    }

    @Test
    public void sendStackTraceTest() throws Exception {
        String stackTrace = "stackTrace";
        given(emailService.sendStackTrace(ADMIN, stackTrace)).willReturn(new EmailResult());

        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "/send/stack-trace")
                        .header(CURRENT_USER, ADMIN)
                        .contentType(MediaType.TEXT_PLAIN_VALUE)
                        .content(stackTrace))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(mvcResult);

        verify(emailService, times(1)).sendStackTrace(ADMIN, stackTrace);
    }
}
