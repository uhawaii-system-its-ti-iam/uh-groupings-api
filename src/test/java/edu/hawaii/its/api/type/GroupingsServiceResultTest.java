package edu.hawaii.its.api.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupingsServiceResultTest {
    private GroupingsServiceResult groupingsServiceResult;
    private final String action = "action";
    private final String resultCode = "resultCode";
    private Person person = null;

    @Before
    public void setUp() {
        groupingsServiceResult = new GroupingsServiceResult();
        person = new Person("name", "uhUuid", "uid");
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultCode);
        groupingsServiceResult.setPerson(person);
    }

    @Test
    public void constructor() {
        GroupingsServiceResult gsr = new GroupingsServiceResult();
        assertNotNull(gsr);
        gsr = new GroupingsServiceResult(resultCode, action);
        assertNotNull(gsr);
        gsr = new GroupingsServiceResult(resultCode, action, person);
        assertNotNull(gsr);
    }

    @Test
    public void accessors() {

        assertEquals(action, groupingsServiceResult.getAction());
        assertEquals(resultCode, groupingsServiceResult.getResultCode());
        assertEquals(person, groupingsServiceResult.getPerson());
    }

    @Test
    public void toStringTest() {
        String expected = "GroupingsServiceResult{" + "action='" + action + '\'' + ", resultCode='" + resultCode + '\''
                + ", person=" + person + '}';
        assertEquals(expected, groupingsServiceResult.toString());
    }
}
