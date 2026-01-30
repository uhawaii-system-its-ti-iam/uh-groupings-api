package edu.hawaii.its.api.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import edu.hawaii.its.api.exception.GroupPathNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.wrapper.Group;

/**
 * GroupPathService provides a set functions for checking the validity of UH grouping/group paths.
 */
@Service
public class GroupPathService {

    private final GrouperService grouperService;

    private static final String BASIS = "basis";
    private static final String EXCLUDE = "exclude";
    private static final String INCLUDE = "include";
    private static final String OWNERS = "owners";
    private static final String RESULT_CODE_SUCCESS = "SUCCESS";

    @Value("${groupings.api.validation.path.maxlength}")
    private int MAX_PATH_LENGTH;

    @Value("${groupings.api.validation.path.regex}")
    private String PATH_REGEX;

    private static Pattern GROUP_PATH_PATTERN;

    public GroupPathService(GrouperService grouperService) {
            this.grouperService = grouperService;
    }

    /**
     * Throw an exception if path is invalid.
     */
    public void checkPath(String path) {
        if (!isWellFormedPath(path)) {
            throw new InvalidGroupPathException(path);
        }
        if (!isValidPath(path)) {
            throw new GroupPathNotFoundException(path);
        }
    }

    private boolean isWellFormedPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        if (path.length() > MAX_PATH_LENGTH) {
            return false;
        }
        if (GROUP_PATH_PATTERN == null) {
            GROUP_PATH_PATTERN = Pattern.compile(PATH_REGEX);
        }
        return GROUP_PATH_PATTERN.matcher(path).matches();
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
        return isGroupPath(group, BASIS);
    }

    public boolean isIncludeGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, INCLUDE);
    }

    public boolean isExcludeGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, EXCLUDE);
    }

    public boolean isOwnersGroupPath(String path) {
        Group group = getGroup(path);
        return isGroupPath(group, OWNERS);
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
        return replaceGroup(INCLUDE, group);
    }

    public String getExcludeGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup(EXCLUDE, group);
    }

    public String getBasisGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup(BASIS, group);
    }

    public String getOwnersGroup(String path) {
        Group group = getGroup(path);
        return replaceGroup(OWNERS, group);
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

        return (result.equals(RESULT_CODE_SUCCESS) && isGroupExtension(extension));
    }

    private boolean isGroupPath(Group group, String expectedExtension) {
        String extension = group.getExtension();
        String result = group.getResultCode();

        return (result.equals(RESULT_CODE_SUCCESS) && isGroupExtension(extension) && isGroupExtension(expectedExtension)
                && expectedExtension.equals(extension));
    }

    public boolean isGroupingPath(Group group) {
        String extension = group.getExtension();
        String result = group.getResultCode();

        return (result.equals(RESULT_CODE_SUCCESS) && !isGroupExtension(extension));
    }

    private boolean isGroupExtension(String extension) {
        return (isBasisExtension(extension) || isExcludeExtension(extension)
                || isIncludeExtension(extension) || isOwnerExtension(extension));
    }

    private boolean isOwnerExtension(String extension) {
        return extension.equals(OWNERS);
    }

    private boolean isIncludeExtension(String extension) {
        return extension.equals(INCLUDE);
    }

    private boolean isExcludeExtension(String extension) {
        return extension.equals(EXCLUDE);
    }

    private boolean isBasisExtension(String extension) {
        return extension.equals(BASIS);
    }
}
