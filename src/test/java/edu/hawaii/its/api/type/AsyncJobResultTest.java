package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AsyncJobResultTest {

    private AsyncJobResult asyncJobResult;
    private final Integer id = 0;
    private final String status = "IN_PROGRESS";
    private final Object result = new Object();

    @BeforeEach
    public void setup() {
        asyncJobResult = new AsyncJobResult(id, status, result);
    }

    @Test
    public void construction() {
        AsyncJobResult jobResult = new AsyncJobResult();
        assertNotNull(jobResult);
        assertNull(jobResult.getId());
        assertNull(jobResult.getStatus());
        assertNull(jobResult.getResult());

        jobResult = new AsyncJobResult(id, status);
        assertNotNull(jobResult);
        assertEquals(id, jobResult.getId());
        assertEquals(status, jobResult.getStatus());
        assertEquals("", jobResult.getResult());

        jobResult = new AsyncJobResult(id, status, result);
        assertNotNull(jobResult);
        assertEquals(id, jobResult.getId());
        assertEquals(status, jobResult.getStatus());
        assertEquals(result, jobResult.getResult());
    }

    @Test
    public void getIdTest() {
        assertEquals(id, asyncJobResult.getId());
    }

    @Test
    public void setIdTest() {
        asyncJobResult.setId(1);
        assertEquals(1, asyncJobResult.getId());
    }

    @Test
    public void getStatusTest() {
        assertEquals(status, asyncJobResult.getStatus());
    }

    @Test
    public void setStatusTest() {
        asyncJobResult.setStatus("COMPLETE");
        assertEquals("COMPLETE", asyncJobResult.getStatus());
    }

    @Test
    public void getResultTest() {
        assertEquals(result, asyncJobResult.getResult());
    }

    @Test
    public void setResultTest() {
        asyncJobResult.setResult("result");
        assertEquals("result", asyncJobResult.getResult());
    }

    @Test
    public void progressSurvivesAJsonRoundTrip() throws Exception {
        AsyncJobProgress progress = new AsyncJobProgress();
        progress.start(AsyncJobProgress.Phase.ADDING, 12284);
        progress.addDone(250);
        AsyncJobResult inProgress = new AsyncJobResult(id, status);
        inProgress.setProgress(progress.snapshot());

        ObjectMapper mapper = new ObjectMapper();
        AsyncJobResult read = mapper.readValue(mapper.writeValueAsString(inProgress), AsyncJobResult.class);

        assertEquals(status, read.getStatus());
        assertEquals(AsyncJobProgress.Phase.ADDING, read.getProgress().getPhase());
        assertEquals(250, read.getProgress().getDone());
        assertEquals(12284, read.getProgress().getTotal());
    }

    @Test
    public void absentProgressSurvivesAJsonRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AsyncJobResult read = mapper.readValue(mapper.writeValueAsString(new AsyncJobResult(id, "COMPLETED")),
                AsyncJobResult.class);

        assertEquals("COMPLETED", read.getStatus());
        assertNull(read.getProgress());
    }
}
