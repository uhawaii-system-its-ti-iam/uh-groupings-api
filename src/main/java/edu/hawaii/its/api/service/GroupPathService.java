package edu.hawaii.its.api.service;

import edu.hawaii.its.api.exception.InvalidGroupPathException;
import edu.hawaii.its.api.wrapper.FindGroupCommand;
import edu.hawaii.its.api.wrapper.FindGroupResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * GroupPathService provides a set functions for checking the validity of UH grouping/group paths.
 */
@Service
public class GroupPathService {
    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    /**
     * Throw an exception if path is invalid.
     */
    public void checkPath(String path) {
        if (!isValidPath(path)) {
            throw new InvalidGroupPathException(path);
        }
    }

    public boolean isValidPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return findGroupResult.isValidPath();
    }

    public boolean isGroupingPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return isGroupingPath(findGroupResult);
    }

    public boolean isGroupPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return isGroupPath(findGroupResult);
    }

    public boolean isBasisGroupPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return isGroupPath(findGroupResult, "basis");
    }

    public boolean isIncludeGroupPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return isGroupPath(findGroupResult, "include");
    }

    public boolean isExcludeGroupPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return isGroupPath(findGroupResult, "exclude");
    }

    public boolean isOwnersGroupPath(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return isGroupPath(findGroupResult, "owners");
    }

    public String getGroupingPath(String groupPath) {
        FindGroupResult findGroupResult = new FindGroupCommand(groupPath).execute();
        if (isGroupingPath(findGroupResult)) {
            return findGroupResult.getGroupPath();
        }
        return findGroupResult.getGroupPath().replaceAll(":" + findGroupResult.getExtension(), "");
    }

    public String getIncludeGroup(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return replaceGroup("include", findGroupResult);
    }

    public String getExcludeGroup(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return replaceGroup("exclude", findGroupResult);
    }

    public String getBasisGroup(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return replaceGroup("basis", findGroupResult);
    }

    public String getOwnersGroup(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        return replaceGroup("owners", findGroupResult);
    }

    private String replaceGroup(String rep, FindGroupResult findGroupResult) {
        if (isGroupPath(findGroupResult)) {
            return findGroupResult.getGroupPath().replaceAll(findGroupResult.getExtension(), rep);
        }
        if (isGroupingPath(findGroupResult)) {
            return findGroupResult.getGroupPath() + ":" + rep;
        }
        return "";
    }

    public String getGroupingDescription(String path) {
        FindGroupResult findGroupResult = new FindGroupCommand(path).execute();
        if (!isGroupingPath(findGroupResult)) {
            return "";
        }
        return findGroupResult.getDescription();
    }

    private boolean isGroupPath(FindGroupResult findGroupResult) {
        String extension = findGroupResult.getExtension();
        String result = findGroupResult.getResultCode();

        return (result.equals("SUCCESS") && isGroupExtension(extension));
    }

    private boolean isGroupPath(FindGroupResult findGroupResult, String expectedExtension) {
        String extension = findGroupResult.getExtension();
        String result = findGroupResult.getResultCode();

        return (result.equals("SUCCESS") && isGroupExtension(extension) && isGroupExtension(expectedExtension)
                && expectedExtension.equals(extension));
    }

    private boolean isGroupingPath(FindGroupResult findGroupResult) {
        String extension = findGroupResult.getExtension();
        String result = findGroupResult.getResultCode();

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
