package edu.hawaii.its.api.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.Subject;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingTest {

    static final String groupingPath = "tmp:grouping";
    static final String groupingName = "grouping";

    @Test
    public void constructor() {
        Grouping grouping = new Grouping();
        assertNotNull(grouping);
        grouping = new Grouping(groupingPath);
        assertNotNull(grouping);
        assertEquals(groupingPath, grouping.getPath());
        assertEquals("", grouping.getDescription());
        assertEquals(groupingName, grouping.getName());
        assertNotNull(grouping.getBasis());
        assertNotNull(grouping.getComposite());
        assertNotNull(grouping.getInclude());
        assertNotNull(grouping.getExclude());
        assertNotNull(grouping.getOwners());

        grouping = new Grouping(null);
        assertEquals("", grouping.getPath());
    }

    @Test
    public void setIsEmpty() {
        Grouping grouping = new Grouping(groupingPath);
        assertFalse(grouping.isEmpty());
        grouping.setIsEmpty();
        assertTrue(grouping.isEmpty());
        Group group = new Group();
        Subject subject = new Subject("uid","name", "uhUuid");
        group.addMember(subject);
        grouping.setBasis(group);
        grouping.setIsEmpty();
        assertFalse(grouping.isEmpty());

        grouping = new Grouping(groupingPath);
        grouping.setInclude(group);
        grouping.setIsEmpty();
        assertFalse(grouping.isEmpty());

        grouping = new Grouping(groupingPath);
        grouping.setExclude(group);
        grouping.setIsEmpty();
        assertFalse(grouping.isEmpty());

        grouping = new Grouping(groupingPath);
        grouping.setOwners(group);
        grouping.setIsEmpty();
        assertFalse(grouping.isEmpty());

        grouping = new Grouping(groupingPath);
        grouping.setComposite(group);
        grouping.setIsEmpty();
        assertFalse(grouping.isEmpty());
    }

    @Test
    public void setName() {
        Grouping grouping = new Grouping();
        assertEquals("", grouping.getName());
        grouping.setName("name");
        assertEquals("name", grouping.getName());
    }

    @Test
    public void setPath() {
        Grouping grouping = new Grouping();
        assertEquals("", grouping.getPath());
        String groupPath = groupingPath + ":include";
        grouping = new Grouping(groupPath);
        assertEquals(groupPath, grouping.getPath());

    }

    @Test
    public void setOpt() {
        Grouping grouping = new Grouping();
        assertFalse(grouping.isOptInOn());
        assertFalse(grouping.isOptOutOn());
        grouping.setOptInOn(true);
        grouping.setOptOutOn(true);
        assertTrue(grouping.isOptInOn());
        assertTrue(grouping.isOptOutOn());
    }

    @Test
    public void setBasis() {
        Grouping grouping = new Grouping(groupingPath);
        assertTrue(grouping.getBasis().isEmpty());
        Group group = new Group();
        Subject subject = new Subject("uid", "name", "uhUuid");
        group.addMember(subject);
        grouping.setBasis(group);
        assertFalse(grouping.getBasis().isEmpty());
        assertTrue(grouping.getBasis().getMembers().contains(subject));
        grouping.setBasis(null);
        assertNotNull(grouping.getBasis());
        assertTrue(grouping.getBasis().isEmpty());
    }

    @Test
    public void setInclude() {
        Grouping grouping = new Grouping(groupingPath);
        assertTrue(grouping.getInclude().isEmpty());
        Group group = new Group();
        Subject subject = new Subject("uid", "name", "uhUuid");
        group.addMember(subject);
        grouping.setInclude(group);
        assertFalse(grouping.getInclude().isEmpty());
        assertTrue(grouping.getInclude().getMembers().contains(subject));
        grouping.setInclude(null);
        assertNotNull(grouping.getInclude());
        assertTrue(grouping.getInclude().isEmpty());
    }

    @Test
    public void setExclude() {
        Grouping grouping = new Grouping(groupingPath);
        assertTrue(grouping.getExclude().isEmpty());
        Group group = new Group();
        Subject subject = new Subject("uid", "name", "uhUuid");
        group.addMember(subject);
        grouping.setExclude(group);
        assertFalse(grouping.getExclude().isEmpty());
        assertTrue(grouping.getExclude().getMembers().contains(subject));
        grouping.setExclude(null);
        assertNotNull(grouping.getExclude());
        assertTrue(grouping.getExclude().isEmpty());
    }

    @Test
    public void setComposite() {
        Grouping grouping = new Grouping(groupingPath);
        assertTrue(grouping.getComposite().isEmpty());
        Group group = new Group();
        Subject subject = new Subject("uid", "name", "uhUuid");
        group.addMember(subject);
        grouping.setComposite(group);
        assertFalse(grouping.getComposite().isEmpty());
        assertTrue(grouping.getComposite().getMembers().contains(subject));
        grouping.setComposite(null);
        assertNotNull(grouping.getComposite());
        assertTrue(grouping.getComposite().isEmpty());
    }

    @Test
    public void setOwners() {
        Grouping grouping = new Grouping(groupingPath);
        assertTrue(grouping.getOwners().isEmpty());
        Group group = new Group();
        Subject subject = new Subject("uid", "name", "uhUuid");
        group.addMember(subject);
        grouping.setOwners(group);
        assertFalse(grouping.getOwners().isEmpty());
        assertTrue(grouping.getOwners().getMembers().contains(subject));
        grouping.setOwners(null);
        assertNotNull(grouping.getOwners());
        assertTrue(grouping.getOwners().isEmpty());
    }
}
