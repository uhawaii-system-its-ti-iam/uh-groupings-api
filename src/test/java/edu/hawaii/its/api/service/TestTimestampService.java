package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.UpdateTimestampResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestTimestampService {

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;
    @Autowired
    private TimestampService timestampService;

    @Test
    public void constructor() {
        assertNotNull(timestampService);
    }

    @Test
    public void updateTimestampTest() {
        LocalDateTime epoch = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT);
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> randomLocalDateTimes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            randomLocalDateTimes.add(getRandomLocalDateTimeBetween(epoch, now));
        }
        timestampService.updateTimestamp(GROUPING_INCLUDE, randomLocalDateTimes.get(0));
        UpdateTimestampResults updateTimestampResults = timestampService.updateTimestamp(GROUPING_INCLUDE, now);
        assertNotNull(updateTimestampResults);
        assertEquals(formatDateString(now.plusMinutes(1)), updateTimestampResults.getUpdatedTimestamp());
        updateTimestampResults = timestampService.updateTimestamp(GROUPING_INCLUDE, randomLocalDateTimes.get(1));
        assertNotNull(updateTimestampResults);
        JsonUtil.printJson(updateTimestampResults);
        assertEquals(formatDateString(now.plusMinutes(1)), updateTimestampResults.getUpdatedTimestamp());
        assertEquals(formatDateString(randomLocalDateTimes.get(1).plusMinutes(1)), updateTimestampResults.getReplacedTimestamp());

    }

    private static String formatDateString(LocalDateTime localDateTime) {
        String formatStr = "yyyyMMdd'T'HHmm";
        return Dates.formatDate(localDateTime, formatStr);
    }

    /**
     * Helper - updateLastModifiedTimestampTest
     * Get a random LocalDateTime between start and end.
     */
    private static LocalDateTime getRandomLocalDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return LocalDateTime.of(
                getRandomNumberBetween(start.getYear(), end.getYear()),
                getRandomNumberBetween(start.getMonthValue(), end.getMonthValue()),
                getRandomNumberBetween(start.getDayOfMonth(), end.getDayOfMonth()),
                getRandomNumberBetween(start.getHour(), end.getHour()),
                getRandomNumberBetween(start.getMinute(), end.getMinute()));
    }

    /**
     * Helper - getRandomLocalDateTimeBetween
     * Get a random number between start and end.
     */
    private static int getRandomNumberBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }
}
