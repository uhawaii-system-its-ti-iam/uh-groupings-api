package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGetGroupsCommand {
    @Value("${groupings.api.test.uh-usernames}")
    private List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    private List<String> UH_NUMBERS;

    String UID;
    String UH_UUID;

    @BeforeEach
    public void init() {
        UID = UH_USERNAMES.get(0);
        UH_UUID = UH_NUMBERS.get(0);
    }

    @Test
    public void constructor() {
        assertNotNull(new GetGroupsCommand());
    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new GetGroupsCommand()::execute);
    }
}
