package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void test() {
        Group group = groupingsTestConfiguration.groupSuccessTestData();
        assertNotNull(group);
        assertEquals("SUCCESS", group.getResultCode());
        assertEquals("extension", group.getExtension());
        assertEquals("description", group.getDescription());
        assertEquals("group-path", group.getGroupPath());
        assertEquals("grouper-uuid", group.getGrouperUuid());
        assertTrue(group.isValidPath());
    }

    @Test
    public void invalidGroup() {
        Group group = new Group(null);
        assertEquals("FAILURE", group.getResultCode());
        assertEquals("", group.getExtension());
        assertEquals("", group.getDescription());
        assertEquals("", group.getGroupPath());
        assertEquals("", group.getGrouperUuid());
    }

}
