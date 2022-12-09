package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hawaii.its.api.type.GroupType;

import java.util.function.Predicate;

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

    public static Predicate<String> onlyMembershipPaths() {
        return pathHasInclude().or(pathHasBasis().and(pathHasExclude().negate()));
    }

}
