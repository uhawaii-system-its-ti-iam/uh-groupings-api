package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hawaii.its.api.type.GroupType;

public class PathFilter {

    public static final Log logger = LogFactory.getLog(PathFilter.class);

    public static Predicate<String> pathHasInclude() {
        return path -> path.endsWith(GroupType.INCLUDE.value());
    }

    public static Predicate<String> pathHasExclude() {
        return path -> path.endsWith(GroupType.EXCLUDE.value());
    }

    public static Predicate<String> pathHasOwner() {
        return path -> path.endsWith(GroupType.OWNERS.value());
    }

    public static Predicate<String> pathHasBasis() {
        return path -> path.endsWith(GroupType.BASIS.value());
    }

    public static Predicate<String> onlyGroupingPaths() {
        return pathHasInclude().negate()
                .and(pathHasBasis().negate().and(pathHasExclude().negate().and(pathHasOwner().negate())));
    }

    public static String parentGroupingPath(String group) {
        if (group != null) {
            if (group.endsWith(GroupType.EXCLUDE.value())) {
                return group.substring(0, group.length() - GroupType.EXCLUDE.value().length());
            } else if (group.endsWith(GroupType.INCLUDE.value())) {
                return group.substring(0, group.length() - GroupType.INCLUDE.value().length());
            } else if (group.endsWith(GroupType.OWNERS.value())) {
                return group.substring(0, group.length() - GroupType.OWNERS.value().length());
            } else if (group.endsWith(GroupType.BASIS.value())) {
                return group.substring(0, group.length() - GroupType.BASIS.value().length());
            }
            return group;
        }
        return "";
    }

    public static List<String> parentGroupingPaths(List<String> groupPaths) {
        List<String> groupingPaths = new ArrayList<>();
        for (String groupPath : groupPaths) {
            groupingPaths.add(parentGroupingPath(groupPath));
        }
        return groupingPaths;
    }

    public static List<String> removeDuplicates(List<String> list) {
        return new ArrayList<>(new HashSet<>(list));
    }

    /**
     * Get list1 - (list1 ∩ list2).
     */
    public static List<String> disjoint(List<String> list1, List<String> list2) {
        List<String> l1 = removeDuplicates(list1);
        List<String> l2 = removeDuplicates(list2);
        for (String str : l2) {
            if (l1.stream().anyMatch(path -> path.equals(str))) {
                l1.remove(str);
            }
        }
        return l1;
    }

    /**
     * Get the name of a grouping from groupPath.
     */
    public static String nameGroupingPath(String groupPath) {
        String parentPath = parentGroupingPath(groupPath);
        if ("".equals(parentPath)) {
            return "";
        }
        return parentPath.substring(parentPath.lastIndexOf(":") + 1, parentPath.length());
    }

    public static String extractExtension(String groupPath) {
        int colonIndex = groupPath.indexOf(":");
        if (colonIndex != -1) {
            return groupPath.substring(colonIndex + 1);
        }
        return "";
    }
}
