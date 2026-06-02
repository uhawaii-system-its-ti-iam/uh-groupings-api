package edu.hawaii.its.api.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import edu.hawaii.its.api.configuration.SecurityTestConfig;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GrouperService;
import edu.hawaii.its.api.util.MutableClock;
import edu.hawaii.its.api.wrapper.FindAttributesResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
@Import({ SecurityTestConfig.class, TestAnnouncementMessageTiming.AnnouncementTimingTestConfig.class })
public class TestAnnouncementMessageTiming {

    private static final String API_ANNOUNCEMENTS = "/api/groupings/v2.1/announcements";
    private static final String BEFORE_MESSAGE = "UH Groupings will be updated BEFORE.";
    private static final String AFTER_MESSAGE = "UH Groupings has been updated as of AFTER.";
    private static final DateTimeFormatter ANNOUNCEMENT_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private MutableClock mutableClock;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
        given(grouperService.findAttributesResults(anyString(), anyString()))
                .willReturn(buildFindAttributesResults(buildAnnouncementsJson()));
    }

    @Test
    void announcementMessagesTransitionThroughTimelineStages() throws Exception {
        // Stage 1 — before any messages are active
        mutableClock.setTime(LocalDateTime.of(2024, 6, 1, 12, 0, 0));
        assertAnnouncementsStage(0);

        // Stage 2 — only BEFORE message is active
        mutableClock.setTime(LocalDateTime.of(2024, 6, 1, 12, 2, 0));
        assertAnnouncementsStage(1, BEFORE_MESSAGE);

        // Stage 3 — both BEFORE and AFTER messages are active
        mutableClock.setTime(LocalDateTime.of(2024, 6, 1, 12, 3, 30));
        assertAnnouncementsStage(2, BEFORE_MESSAGE, AFTER_MESSAGE);

        // Stage 4 — BEFORE expired, only AFTER message is active
        mutableClock.setTime(LocalDateTime.of(2024, 6, 1, 12, 5, 0));
        assertAnnouncementsStage(1, AFTER_MESSAGE);

        // Stage 5 — all messages expired
        mutableClock.setTime(LocalDateTime.of(2024, 6, 1, 12, 7, 0));
        assertAnnouncementsStage(0);
    }

    private void assertAnnouncementsStage(int expectedCount, String... expectedMessages) throws Exception {
        var resultActions = mockMvc.perform(get(API_ANNOUNCEMENTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.announcements", hasSize(expectedCount)));

        if (expectedCount > 0) {
            resultActions.andExpect(jsonPath("$.announcements[*].message", containsInAnyOrder(expectedMessages)))
                    .andExpect(jsonPath("$.announcements[0].start").doesNotExist())
                    .andExpect(jsonPath("$.announcements[0].end").doesNotExist())
                    .andExpect(jsonPath("$.announcements[0].state").doesNotExist());
        }
    }

    private static String buildAnnouncementsJson() {
        return """
                [
                  {
                    "message":"%s",
                    "start":"%s",
                    "end":"%s"
                  },
                  {
                    "message":"%s",
                    "start":"%s",
                    "end":"%s"
                  }
                ]
                """.formatted(
                BEFORE_MESSAGE,
                formatTime(LocalDateTime.of(2024, 6, 1, 12, 1, 0)),
                formatTime(LocalDateTime.of(2024, 6, 1, 12, 4, 0)),
                AFTER_MESSAGE,
                formatTime(LocalDateTime.of(2024, 6, 1, 12, 3, 0)),
                formatTime(LocalDateTime.of(2024, 6, 1, 12, 6, 0)));
    }

    private static String formatTime(LocalDateTime time) {
        return time.format(ANNOUNCEMENT_TIME);
    }

    private static FindAttributesResults buildFindAttributesResults(String descriptionJson) {
        WsResultMeta resultMeta = new WsResultMeta();
        resultMeta.setResultCode("SUCCESS");

        WsAttributeDefName attributeDefName = new WsAttributeDefName();
        attributeDefName.setDescription(descriptionJson);

        WsFindAttributeDefNamesResults wsResults = new WsFindAttributeDefNamesResults();
        wsResults.setResultMetadata(resultMeta);
        wsResults.setAttributeDefNameResults(new WsAttributeDefName[] { attributeDefName });

        return new FindAttributesResults(wsResults);
    }

    @TestConfiguration
    static class AnnouncementTimingTestConfig {

        @Bean
        @Primary
        MutableClock mutableClock() {
            return new MutableClock(LocalDateTime.of(2024, 6, 1, 12, 0, 0), ZoneId.systemDefault());
        }
    }
}
