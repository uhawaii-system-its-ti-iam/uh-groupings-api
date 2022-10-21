package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestUsersGroupsResult {
    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.uh-usernames}")
    protected List<String> UH_USERNAMES;

    @Value("${groupings.api.test.uh-numbers}")
    protected List<String> UH_NUMBERS;

    @Value("${groupings.api.success}")
    protected String SUCCESS;

    @Test
    public void constructorTest() {
         new UsersGroupsCommand(UH_USERNAMES.get(0));
         new UsersGroupsCommand(UH_NUMBERS.get(0));
    }

    @Test
    public void executeTest() {
        UsersGroupsCommand usersGroupsCommandUsername = new UsersGroupsCommand(UH_USERNAMES.get(0));
        UsersGroupsCommand usersGroupsCommandNumber = new UsersGroupsCommand(UH_NUMBERS.get(0));

        UsersGroupsResult usersGroupsResultUsername = usersGroupsCommandUsername.execute();
        UsersGroupsResult usersGroupsResultNumber = usersGroupsCommandNumber.execute();
    }

    @Test
    public void resultsTest() {
    }
}
