package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.nameGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.onlyGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasExclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;
import static edu.hawaii.its.api.service.PathFilter.removeDuplicates;
import static edu.hawaii.its.api.util.OnlyUniqueItems.onlyUniqueItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class PathFilterTest {

    private String includePath = "bogus:include";
    private String excludePath = "bogus:exclude";
    private String ownerPath = "bogus:owners";
    private String basisPath = "bogus:basis";

    private String groupinPath = "bogus-path";

    private List<String> testPathList;

    @Test
    public void testPathHasInclude() {
        testPathList = new ArrayList<>();
        assertThat(filterPaths(testPathList, pathHasInclude()).size(), equalTo(0));
        testPathList.add(includePath);
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        assertThat(filterPaths(testPathList, pathHasInclude()).size(), equalTo(2));
    }

    @Test
    public void testPathHasExclude() {
        testPathList = new ArrayList<>();
        assertThat(filterPaths(testPathList, pathHasExclude()).size(), equalTo(0));
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        assertThat(filterPaths(testPathList, pathHasExclude()).size(), equalTo(2));
    }

    @Test
    public void testPathHasOwner() {
        testPathList = new ArrayList<>();
        assertThat(filterPaths(testPathList, pathHasOwner()).size(), equalTo(0));
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        assertThat(filterPaths(testPathList, pathHasOwner()).size(), equalTo(2));
    }

    @Test
    public void testPathHasBasis() {
        testPathList = new ArrayList<>();
        assertThat(filterPaths(testPathList, pathHasBasis()).size(), equalTo(0));
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        testPathList.add(basisPath);
        assertThat(filterPaths(testPathList, pathHasBasis()).size(), equalTo(2));
    }

    @Test
    public void testOnlyGroupingPaths() {
        testPathList = new ArrayList<>();
        assertThat(filterPaths(testPathList, onlyGroupingPaths()).size(), equalTo(0));
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        testPathList.add(basisPath);
        testPathList.add(groupinPath);
        assertThat(filterPaths(testPathList, onlyGroupingPaths()).size(), equalTo(1));
    }

    @Test
    public void testParentGroupingPath() {
        testPathList = new ArrayList<>();
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        testPathList.add(basisPath);

        for (String testPath : testPathList) {
            assertThat(parentGroupingPath(testPath), equalTo("bogus"));
        }
        assertThat(parentGroupingPath(groupinPath), equalTo(groupinPath));

        PathFilter pathFilter = new PathFilter();
        assertEquals("", pathFilter.parentGroupingPath(null));
        assertEquals("", pathFilter.parentGroupingPath(""));
    }

    @Test
    public void testParentGroupingPaths() {
        testPathList = new ArrayList<>();
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        testPathList.add(basisPath);

        List<String> parentPaths = parentGroupingPaths(testPathList);
        for (String parentPath : parentPaths) {
            assertThat(parentPath, equalTo("bogus"));
        }

    }

    @Test
    public void testRemoveDuplicates() {
        testPathList = new ArrayList<>();
        testPathList.add(includePath);
        testPathList.add(includePath);
        testPathList.add(includePath);
        testPathList.add(includePath);

        List<String> duplicatesRemoved = removeDuplicates(testPathList);
        assertThat(duplicatesRemoved.size(), equalTo(1));
        assertThat(duplicatesRemoved, onlyUniqueItems());
    }

    @Test
    public void testDisjoint() {
        List<String> list1 = new ArrayList<>();
        list1.add(includePath);
        list1.add(includePath);
        list1.add(excludePath);
        list1.add(ownerPath);
        list1.add(basisPath);
        List<String> list2 = new ArrayList<>();
        list2.add(excludePath);
        list2.add(excludePath);
        list2.add(basisPath);

        List<String> disjoint = PathFilter.disjoint(list1, list2);
        assertTrue(disjoint.contains(includePath));
        assertTrue(disjoint.contains(ownerPath));
        assertFalse(disjoint.contains(excludePath));
        assertFalse(disjoint.contains(basisPath));
    }

    @Test
    public void nameGroupingPathTest() {
        assertEquals("grouping-test-path", nameGroupingPath("test:grouping-test-path:include"));
        assertEquals("", nameGroupingPath(""));
    }

    @Test
    public void testExtractExtension() {
        assertEquals("include", PathFilter.extractExtension("bogus:include"));
        assertEquals("exclude", PathFilter.extractExtension("bogus:exclude"));
        assertEquals("owners", PathFilter.extractExtension("bogus:owners"));
        assertEquals("basis", PathFilter.extractExtension("bogus:basis"));
        assertEquals("grouping-path", PathFilter.extractExtension("bogus:grouping-path"));
        assertEquals("", PathFilter.extractExtension("bogus"));
        assertEquals("", PathFilter.extractExtension(""));
    }

    /**
     * General filter to filter paths with the predicates in the PathFilter class.
     */
    private static List<String> filterPaths(List<String> groupPaths, Predicate<String> predicate) {
        return groupPaths.stream().filter(predicate).collect(Collectors.toList());
    }


}
