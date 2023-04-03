package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGetMembersCommand {

    @Test
    public void constructor() {
        assertNotNull(new GetMembersCommand());
    }

    @Test
    public void execute() {
        assertThrows(RuntimeException.class, new GetMembersCommand()::execute);
    }
}
