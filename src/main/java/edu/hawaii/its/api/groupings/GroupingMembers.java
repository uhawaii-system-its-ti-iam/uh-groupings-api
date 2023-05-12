package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

/**
 * GroupingMembers is used to hydrate the all members sections of a grouping, all members is (basis + include) - exclude.
 */
public class GroupingMembers {
    private final List<GroupingMember> members;

    public GroupingMembers() {
        this.members = new ArrayList<>();
    }

    public List<GroupingMember> getMembers() {
        return members;
    }
}
