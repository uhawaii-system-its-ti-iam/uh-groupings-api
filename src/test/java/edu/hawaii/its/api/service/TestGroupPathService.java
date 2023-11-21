package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.util.JsonUtil;

@ActiveProfiles("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestGroupPathService {

    @Value("${groupings.api.test.grouping_many_include}")
    protected String GROUPING_INCLUDE;

    @Value("${groupings.api.test.grouping_many_exclude}")
    protected String GROUPING_EXCLUDE;

    @Value("${groupings.api.test.grouping_many_basis}")
    protected String GROUPING_BASIS;

    @Value("${groupings.api.test.grouping_many_owners}")
    protected String GROUPING_OWNERS;

    @Value("${groupings.api.test.grouping_many}")
    protected String GROUPING;

    private static final String INVALID_GROUPING_PATH = "invalid-path:include";

    @Autowired
    private GroupPathService groupPathService;

    @Test
    public void constructor() {
        assertNotNull(groupPathService);
    }

    @Test
    public void checkPath() {
        try {
            groupPathService.checkPath(GROUPING);
        } catch (InvalidGroupPathException e) {
            fail("Should not throw an exception if path is valid");
        }
        try {
            groupPathService.checkPath("bad-path");
            fail("Should throw an exception if path is valid");
        } catch (InvalidGroupPathException e) {
            JsonUtil.printJson(e);
        }
    }

    @Test
    public void isValidPath() {
        assertTrue(groupPathService.isValidPath(GROUPING));
        assertTrue(groupPathService.isValidPath(GROUPING_BASIS));
        assertTrue(groupPathService.isValidPath(GROUPING_EXCLUDE));
        assertTrue(groupPathService.isValidPath(GROUPING_INCLUDE));
        assertFalse(groupPathService.isValidPath("bad-path"));
    }

    @Test
    public void isGroupingPath() {
        assertTrue(groupPathService.isGroupingPath(GROUPING));
        assertFalse(groupPathService.isGroupingPath(GROUPING_BASIS));
        assertFalse(groupPathService.isGroupingPath(GROUPING_OWNERS));
        assertFalse(groupPathService.isGroupingPath(GROUPING_INCLUDE));
        assertFalse(groupPathService.isGroupingPath(GROUPING_EXCLUDE));
        assertFalse(groupPathService.isGroupingPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void isGroupPath() {
        assertFalse(groupPathService.isGroupPath(GROUPING));
        assertTrue(groupPathService.isGroupPath(GROUPING_BASIS));
        assertTrue(groupPathService.isGroupPath(GROUPING_OWNERS));
        assertTrue(groupPathService.isGroupPath(GROUPING_INCLUDE));
        assertTrue(groupPathService.isGroupPath(GROUPING_EXCLUDE));
        assertFalse(groupPathService.isGroupPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void isBasisGroupPath() {
        assertFalse(groupPathService.isBasisGroupPath(GROUPING));
        assertTrue(groupPathService.isBasisGroupPath(GROUPING_BASIS));
        assertFalse(groupPathService.isBasisGroupPath(GROUPING_OWNERS));
        assertFalse(groupPathService.isBasisGroupPath(GROUPING_INCLUDE));
        assertFalse(groupPathService.isBasisGroupPath(GROUPING_EXCLUDE));
        assertFalse(groupPathService.isBasisGroupPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void isIncludeGroupPath() {
        assertFalse(groupPathService.isIncludeGroupPath(GROUPING));
        assertFalse(groupPathService.isIncludeGroupPath(GROUPING_BASIS));
        assertFalse(groupPathService.isIncludeGroupPath(GROUPING_OWNERS));
        assertTrue(groupPathService.isIncludeGroupPath(GROUPING_INCLUDE));
        assertFalse(groupPathService.isIncludeGroupPath(GROUPING_EXCLUDE));
        assertFalse(groupPathService.isIncludeGroupPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void isExcludeGroupPath() {
        assertFalse(groupPathService.isExcludeGroupPath(GROUPING));
        assertFalse(groupPathService.isExcludeGroupPath(GROUPING_BASIS));
        assertFalse(groupPathService.isExcludeGroupPath(GROUPING_OWNERS));
        assertFalse(groupPathService.isExcludeGroupPath(GROUPING_INCLUDE));
        assertTrue(groupPathService.isExcludeGroupPath(GROUPING_EXCLUDE));
        assertFalse(groupPathService.isExcludeGroupPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void isOwnersGroupPath() {
        assertFalse(groupPathService.isOwnersGroupPath(GROUPING));
        assertFalse(groupPathService.isOwnersGroupPath(GROUPING_BASIS));
        assertTrue(groupPathService.isOwnersGroupPath(GROUPING_OWNERS));
        assertFalse(groupPathService.isOwnersGroupPath(GROUPING_INCLUDE));
        assertFalse(groupPathService.isOwnersGroupPath(GROUPING_EXCLUDE));
        assertFalse(groupPathService.isOwnersGroupPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void getGroupingPath() {
        assertEquals(GROUPING, groupPathService.getGroupingPath(GROUPING));
        assertEquals(GROUPING, groupPathService.getGroupingPath(GROUPING_INCLUDE));
        assertEquals(GROUPING, groupPathService.getGroupingPath(GROUPING_EXCLUDE));
        assertEquals(GROUPING, groupPathService.getGroupingPath(GROUPING_BASIS));
        assertEquals(GROUPING, groupPathService.getGroupingPath(GROUPING_OWNERS));
        assertEquals("", groupPathService.getGroupingPath(INVALID_GROUPING_PATH));
    }

    @Test
    public void getValidGroupings() {
        List<String> groupingPaths = Arrays.asList(GROUPING);
        assertEquals(GROUPING, groupPathService.getValidGroupings(groupingPaths).get(0).getGroupPath());
    }

    @Test
    public void getIncludeGroup() {
        assertEquals(GROUPING_INCLUDE, groupPathService.getIncludeGroup(GROUPING));
        assertEquals(GROUPING_INCLUDE, groupPathService.getIncludeGroup(GROUPING_INCLUDE));
        assertEquals(GROUPING_INCLUDE, groupPathService.getIncludeGroup(GROUPING_EXCLUDE));
        assertEquals(GROUPING_INCLUDE, groupPathService.getIncludeGroup(GROUPING_OWNERS));
        assertEquals(GROUPING_INCLUDE, groupPathService.getIncludeGroup(GROUPING_BASIS));
        assertEquals("", groupPathService.getIncludeGroup(INVALID_GROUPING_PATH));
    }

    @Test
    public void getExcludeGroup() {
        assertEquals(GROUPING_EXCLUDE, groupPathService.getExcludeGroup(GROUPING));
        assertEquals(GROUPING_EXCLUDE, groupPathService.getExcludeGroup(GROUPING_INCLUDE));
        assertEquals(GROUPING_EXCLUDE, groupPathService.getExcludeGroup(GROUPING_EXCLUDE));
        assertEquals(GROUPING_EXCLUDE, groupPathService.getExcludeGroup(GROUPING_OWNERS));
        assertEquals(GROUPING_EXCLUDE, groupPathService.getExcludeGroup(GROUPING_BASIS));
        assertEquals("", groupPathService.getExcludeGroup(INVALID_GROUPING_PATH));
    }

    @Test
    public void getBasisGroup() {
        assertEquals(GROUPING_BASIS, groupPathService.getBasisGroup(GROUPING));
        assertEquals(GROUPING_BASIS, groupPathService.getBasisGroup(GROUPING_INCLUDE));
        assertEquals(GROUPING_BASIS, groupPathService.getBasisGroup(GROUPING_EXCLUDE));
        assertEquals(GROUPING_BASIS, groupPathService.getBasisGroup(GROUPING_OWNERS));
        assertEquals(GROUPING_BASIS, groupPathService.getBasisGroup(GROUPING_BASIS));
        assertEquals("", groupPathService.getBasisGroup(INVALID_GROUPING_PATH));
    }

    @Test
    public void getOwnersGroup() {
        assertEquals(GROUPING_OWNERS, groupPathService.getOwnersGroup(GROUPING));
        assertEquals(GROUPING_OWNERS, groupPathService.getOwnersGroup(GROUPING_INCLUDE));
        assertEquals(GROUPING_OWNERS, groupPathService.getOwnersGroup(GROUPING_EXCLUDE));
        assertEquals(GROUPING_OWNERS, groupPathService.getOwnersGroup(GROUPING_OWNERS));
        assertEquals(GROUPING_OWNERS, groupPathService.getOwnersGroup(GROUPING_BASIS));
        assertEquals("", groupPathService.getOwnersGroup(INVALID_GROUPING_PATH));
    }

    @Test
    public void getGroupPaths() {
        List<String> groupPaths = groupPathService.getGroupPaths(GROUPING);
        assertTrue(groupPaths.contains(GROUPING_BASIS));
        assertTrue(groupPaths.contains(GROUPING_INCLUDE));
        assertTrue(groupPaths.contains(GROUPING_EXCLUDE));
        assertTrue(groupPaths.contains(GROUPING_OWNERS));
    }
}
