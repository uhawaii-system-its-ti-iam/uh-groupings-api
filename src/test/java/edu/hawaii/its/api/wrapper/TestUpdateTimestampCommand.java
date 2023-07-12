package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateTimestampCommand {
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Test
    public void constructor() {
        UpdateTimestampCommand updateTimestampCommand = new UpdateTimestampCommand(GROUPING_INCLUDE);
        assertNotNull(updateTimestampCommand);

        updateTimestampCommand = new UpdateTimestampCommand();
        assertNotNull(updateTimestampCommand);

        assertEquals("groupPath cannot be null",
                assertThrows(NullPointerException.class, () -> new UpdateTimestampCommand(null)).getMessage());

    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new UpdateTimestampCommand()::execute);
    }

}
