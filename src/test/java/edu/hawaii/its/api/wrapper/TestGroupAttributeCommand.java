package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.service.GroupingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupAttributeCommand {
    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Autowired
    private GroupingsService groupingsService;

    private static final String TRIO = "uh-settings:attributes:for-groups:uh-grouping:is-trio";

    private static final List<String> PATHS = new ArrayList<>();
    private static final List<String> ATTRIBUTES = new ArrayList<>();

    @BeforeEach
    public void init() {
        PATHS.add(GROUPING);
        ATTRIBUTES.add(TRIO);
    }

    @Test
    public void constructor() {
        GroupAttributeCommand groupAttributeCommand = new GroupAttributeCommand();
        assertNotNull(groupAttributeCommand);

        groupAttributeCommand = new GroupAttributeCommand(TRIO);
        assertNotNull(groupAttributeCommand);

        groupAttributeCommand = new GroupAttributeCommand(TRIO, GROUPING);
        assertNotNull(groupAttributeCommand);

        groupAttributeCommand = new GroupAttributeCommand(TRIO, PATHS);
        assertNotNull(groupAttributeCommand);

        groupAttributeCommand = new GroupAttributeCommand(ATTRIBUTES, GROUPING);
        assertNotNull(groupAttributeCommand);

        groupAttributeCommand = new GroupAttributeCommand(ATTRIBUTES, PATHS);
        assertNotNull(groupAttributeCommand);
        groupAttributeCommand.execute();

    }
}