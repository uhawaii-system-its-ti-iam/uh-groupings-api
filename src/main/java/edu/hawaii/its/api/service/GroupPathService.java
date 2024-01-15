package edu.hawaii.its.api.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.wrapper.Group;

/**
 * GroupPathService provides a set functions for checking the validity of UH grouping/group paths.
 */
@Service
public class GroupPathService {

    @Autowired
    private GrouperService grouperService;

    /**
     * Throw an exception if path is invalid.
     */
    public void checkPath(String path) {
        if (!isValidPath(path)) {
            throw new InvalidGroupPathException(path);
        }
    }

    public boolean isValidPath(String path) {
        Group group = grouperService.findGroupsResults(path).getGroup();
        return group.isValidPath();
    }

    public boolean isGroupingPath(String path) {
        Group group = getGroup(path);
        return isGroupingPath(group);
    }

    public boolean isGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group);
    }

    public boolean isBasisGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, "basis");
    }

    public boolean isIncludeGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, "include");
    }

    public boolean isExcludeGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, "exclude");
    }

    public boolean isOwnersGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, "owners");
    }

    public String getGroupingPath(String groupPath) {
        Group group = getGroup(groupPath);
        if (isGroupingPath(group)) {
            return group.getGroupPath();
        }
        return group.getGroupPath().replaceAll(":" + group.getExtension(), "");
    }

    public List<Group> getValidGroupings(List<String> groupingPaths) {
        return grouperService.findGroupsResults(groupingPaths).getGroups();
    }

    public String getIncludeGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup("include", group);
    }

    public String getExcludeGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup("exclude", group);
    }

    public String getBasisGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup("basis", group);
    }

    public String getOwnersGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup("owners", group);
    }

    public List<String> getGroupPaths(List<Group> groups) {
        return groups.stream().map(Group::getGroupPath).collect(Collectors.toList());
    }

    public List<String> getGroupPaths(String groupingPath) {
        return Arrays.asList(
                getBasisGroup(groupingPath),
                getIncludeGroup(groupingPath),
                getExcludeGroup(groupingPath),
                getOwnersGroup(groupingPath));
    }

    private Group getGroup(String groupPath) {
        return grouperService.findGroupsResults(groupPath).getGroup();
    }

    private String replaceGroup(String rep, Group group) {
        if (isGroupPath(group)) {
            return group.getGroupPath().replaceAll(group.getExtension(), rep);
        }
        if (isGroupingPath(group)) {
            return group.getGroupPath() + ":" + rep;
        }
        return "";
    }

    private boolean isGroupPath(Group group) {
        String extension = group.getExtension();
        String result = group.getResultCode();

        return (result.equals("SUCCESS") && isGroupExtension(extension));
    }

    private boolean isGroupPath(Group group, String expectedExtension) {
        String extension = group.getExtension();
        String result = group.getResultCode();

        return (result.equals("SUCCESS") && isGroupExtension(extension) && isGroupExtension(expectedExtension)
                && expectedExtension.equals(extension));
    }

    public boolean isGroupingPath(Group group) {
        String extension = group.getExtension();
        String result = group.getResultCode();

        return (result.equals("SUCCESS") && !isGroupExtension(extension));
    }

    private boolean isGroupExtension(String extension) {
        return (isBasisExtension(extension) || isExcludeExtension(extension)
                || isIncludeExtension(extension) || isOwnerExtension(extension));
    }

    private boolean isOwnerExtension(String extension) {
        return extension.equals("owners");
    }

    private boolean isIncludeExtension(String extension) {
        return extension.equals("include");
    }

    private boolean isExcludeExtension(String extension) {
        return extension.equals("exclude");
    }

    private boolean isBasisExtension(String extension) {
        return extension.equals("basis");
    }
}
