package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UpdateTimestampCommandTest {

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Test public void constructor() {
        UpdateTimestampCommand updateTimestampCommand =
                new UpdateTimestampCommand(ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, GROUPING_INCLUDE,
                        YYYYMMDDTHHMM, OPERATION_REPLACE_VALUES, LocalDateTime.now());
        assertNotNull(updateTimestampCommand);
        try {
            new UpdateTimestampCommand(null, OPERATION_ASSIGN_ATTRIBUTE, GROUPING_INCLUDE, YYYYMMDDTHHMM,
                    OPERATION_REPLACE_VALUES, LocalDateTime.now());
        } catch (NullPointerException e) {
            assertEquals("assignType cannot be null", e.getMessage());
        }
    }
}
