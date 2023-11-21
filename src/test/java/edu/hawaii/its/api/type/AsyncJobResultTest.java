package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
