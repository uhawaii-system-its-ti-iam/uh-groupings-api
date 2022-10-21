package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestSubjectCommand  {
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
        assertNotNull(new SubjectCommand(UH_NUMBERS.get(0)));
        assertNotNull(new SubjectCommand(UH_USERNAMES.get(0)));
        assertNotNull(new SubjectCommand(null));
    }

    @Test
    public void executeTest() {
        assertNotNull(new SubjectCommand(UH_NUMBERS.get(0)).execute());
        assertNotNull(new SubjectCommand(UH_USERNAMES.get(0)).execute());
        assertNotNull(new SubjectCommand(null).execute());
    }

    @Test
    public void resultsTest() {
        String username = UH_USERNAMES.get(0);
        String number = UH_NUMBERS.get(0);
        SubjectResult usernameResult = new SubjectCommand(username).execute();
        SubjectResult numberResult = new SubjectCommand(number).execute();

        assertEquals(number, usernameResult.getUhUuid());
        assertEquals(number, numberResult.getUhUuid());

        new SubjectCommand("Badfasd-fasdfsad").execute();
    }
}
