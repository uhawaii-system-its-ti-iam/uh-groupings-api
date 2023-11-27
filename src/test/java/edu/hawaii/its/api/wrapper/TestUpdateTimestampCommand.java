package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> UH_USERNAMES;
    
    @Test
    public void constructor() {
        UpdateTimestampCommand updateTimestampCommand = new UpdateTimestampCommand(GROUPING_INCLUDE);
        assertNotNull(updateTimestampCommand);

        updateTimestampCommand = new UpdateTimestampCommand();
        assertNotNull(updateTimestampCommand);

        assertEquals("groupPath cannot be null",
                assertThrows(NullPointerException.class, () -> new UpdateTimestampCommand((String) null)).getMessage());

        List<String> groupPaths = new ArrayList<>();
        groupPaths.add(GROUPING_INCLUDE);
        groupPaths.add(GROUPING_OWNER);

        UpdateTimestampCommand updateTimestampCommandList = new UpdateTimestampCommand(groupPaths);
        assertNotNull(updateTimestampCommandList);

        assertEquals("groupPaths cannot be empty", assertThrows(IllegalStateException.class, () -> new UpdateTimestampCommand(new ArrayList<>())).getMessage());

    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new UpdateTimestampCommand()::execute);
    }

}
