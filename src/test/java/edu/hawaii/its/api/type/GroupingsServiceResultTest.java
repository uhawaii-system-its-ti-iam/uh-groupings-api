package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.wrapper.Subject;

public class GroupingsServiceResultTest {
    private GroupingsServiceResult groupingsServiceResult;
    private final String action = "action";
    private final String resultCode = "resultCode";
    private Subject subject = null;

    @BeforeEach
    public void setUp() {
        groupingsServiceResult = new GroupingsServiceResult();
        subject = new Subject("uid", "name", "uhUuid");
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultCode);
        groupingsServiceResult.setSubject(subject);
    }

    @Test
    public void constructor() {
        GroupingsServiceResult gsr = new GroupingsServiceResult();
        assertNotNull(gsr);
        gsr = new GroupingsServiceResult(resultCode, action);
        assertNotNull(gsr);
        gsr = new GroupingsServiceResult(resultCode, action, subject);
        assertNotNull(gsr);
    }

    @Test
    public void accessors() {

        assertEquals(action, groupingsServiceResult.getAction());
        assertEquals(resultCode, groupingsServiceResult.getResultCode());
        assertEquals(subject, groupingsServiceResult.getSubject());
    }

    @Test
    public void toStringTest() {
        String expected = "GroupingsServiceResult{" + "action='" + action + '\'' + ", resultCode='" + resultCode + '\''
                + ", subject=" + subject + '}';
        assertEquals(expected, groupingsServiceResult.toString());
    }
}
