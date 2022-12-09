package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static edu.hawaii.its.api.service.PathFilter.onlyMembershipPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasExclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PathFilterTest {

    private String includePath = "bogus:include";
    private String excludePath = "bogus:exclude";
    private String ownerPath = "bogus:owners";
    private String basisPath = "bogus:basis";

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
    public void testOnlyMemberships() {
        testPathList = new ArrayList<>();
        assertThat(filterPaths(testPathList, onlyMembershipPaths()).size(), equalTo(0));
        testPathList.add(includePath);
        testPathList.add(includePath);
        testPathList.add(excludePath);
        testPathList.add(excludePath);
        testPathList.add(ownerPath);
        testPathList.add(ownerPath);
        testPathList.add(basisPath);
        testPathList.add(basisPath);
        assertThat(filterPaths(testPathList, onlyMembershipPaths()).size(), equalTo(4));
    }

    /**
     * General filter to filter paths with the predicates in the PathFilter class.
     */
    private static List<String> filterPaths(List<String> groupPaths, Predicate<String> predicate) {
        return groupPaths.stream().filter(predicate).collect(Collectors.toList());
    }
}
