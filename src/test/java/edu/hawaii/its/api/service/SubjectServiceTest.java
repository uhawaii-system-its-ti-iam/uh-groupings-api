package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.GrouperException;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class SubjectServiceTest {

    private static final String TEST_USER = "testiwta";

    @Autowired
    private SubjectService subjectService;

    @MockitoBean
    private GrouperService grouperService;

    @Test
    public void getValidUhUuidPropagatesGrouperException() {
        given(grouperService.getSubjects(TEST_USER))
                .willThrow(new GrouperException("Grouper unavailable"));

        assertThrows(GrouperException.class, () -> subjectService.getValidUhUuid(TEST_USER, TEST_USER));
    }

    @Test
    public void getValidUhUuidReturnsEmptyForSubjectNotFound() {
        given(grouperService.getSubjects(TEST_USER))
                .willReturn(subjectsResults("SUCCESS", "SUBJECT_NOT_FOUND"));

        assertEquals("", subjectService.getValidUhUuid(TEST_USER, TEST_USER));
    }

    @Test
    public void getValidUhUuidThrowsGrouperExceptionWhenRawSubjectLookupFails() {
        given(grouperService.getSubjects(TEST_USER))
                .willReturn(subjectsResults("FAILURE", "SUBJECT_NOT_FOUND"));

        assertThrows(GrouperException.class, () -> subjectService.getValidUhUuid(TEST_USER, TEST_USER));
    }

    private SubjectsResults subjectsResults(String rawResultCode, String subjectResultCode) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(rawResultCode);

        WsSubject subject = new WsSubject();
        subject.setResultCode(subjectResultCode);
        subject.setIdentifierLookup(TEST_USER);

        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(new WsSubject[] { subject });

        return new SubjectsResults(wsGetSubjectsResults);
    }
}
