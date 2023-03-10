package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupSaveCommand {
    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    @Autowired
    private GroupingsService groupingsService;

    @Test
    public void constructor() {
        GroupSaveCommand groupSaveCommand = new GroupSaveCommand();
        assertNotNull(groupSaveCommand);
    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new GroupSaveCommand()::execute);
    }
}
