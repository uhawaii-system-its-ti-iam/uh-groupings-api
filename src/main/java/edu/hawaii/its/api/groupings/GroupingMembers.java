package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GroupingMembers is used to hydrate the all members sections of a grouping, all members is (basis + include) - exclude.
 */
public class GroupingMembers {
    private final List<GroupingMember> members;

    public GroupingMembers() {
        this.members = new ArrayList<>();
    }

    public GroupingMembers(List<GroupingMember> members) {
        this.members = members;
    }

    public List<GroupingMember> getMembers() {
        return members;
    }

    public List<String> getUids() {
        return members.stream().map(GroupingMember::getUid).collect(Collectors.toList());
    }

    public List<String> getUhUuids() {
        return members.stream().map(GroupingMember::getUhUuid).collect(Collectors.toList());
    }
}
