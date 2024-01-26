package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUpdateTimestampCommand {
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNER;
    
    @Test
    public void constructor() {
        UpdateTimestampCommand updateTimestampCommand = new UpdateTimestampCommand().addGroupPath(GROUPING_INCLUDE);
        assertNotNull(updateTimestampCommand);

        updateTimestampCommand = new UpdateTimestampCommand();
        assertNotNull(updateTimestampCommand);

        List<String> groupPaths = new ArrayList<>();
        groupPaths.add(GROUPING_INCLUDE);
        groupPaths.add(GROUPING_OWNER);

        UpdateTimestampCommand updateTimestampCommandList = new UpdateTimestampCommand().addGroupPaths(groupPaths);
        assertNotNull(updateTimestampCommandList);
    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new UpdateTimestampCommand()::execute);
    }

}
