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
public class TestSubjectsCommand {
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;

    @Test
    public void constructorTest() {
        SubjectsCommand subjectsCommand = new SubjectsCommand(TEST_UH_NUMBERS);
        assertNotNull(subjectsCommand);
    }

    @Test
    public void executeTest() {
        SubjectsCommand subjectsCommand = new SubjectsCommand(TEST_UH_NUMBERS);
        SubjectsResults subjectsResults = subjectsCommand.execute();
        assertNotNull(subjectsResults);
        assertEquals("SUCCESS", subjectsResults.getResultCode());
    }
}
