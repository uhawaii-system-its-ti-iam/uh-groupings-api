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
public class TestSubjectCommand {
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;

    @Test
    public void constructorTest() {
        SubjectCommand subjectCommand = new SubjectCommand(TEST_UH_NUMBERS.get(0));
        assertNotNull(subjectCommand);
        subjectCommand = new SubjectCommand(null);
        assertNotNull(subjectCommand);
    }

    @Test
    public void executeTest() {
        SubjectCommand subjectCommand = new SubjectCommand(TEST_UH_NUMBERS.get(0));
        SubjectResult subjectResult = subjectCommand.execute();
        assertNotNull(subjectResult);
        assertEquals("SUCCESS", subjectResult.getResultCode());

        subjectCommand = new SubjectCommand("bogus-subject");
        subjectResult = subjectCommand.execute();
        assertEquals("SUBJECT_NOT_FOUND", subjectResult.getResultCode());
    }

}
