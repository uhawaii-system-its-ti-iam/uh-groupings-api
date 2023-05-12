package edu.hawaii.its.api.groupings;

/**
 * GroupingMember is used to hydrate the allMembers section of a grouping.
 */
public class GroupingMember {
    private final String uid;
    private final String uhUuid;
    private final String name;
    private final String whereListed;

    public GroupingMember(GroupingGroupMember groupingGroupMember, String whereListed) {
        this.name = groupingGroupMember.getName();
        this.uhUuid = groupingGroupMember.getUhUuid();
        this.uid = groupingGroupMember.getUid();
        this.whereListed = whereListed;
    }

    public GroupingMember() {
        this.name = "";
        this.uhUuid = "";
        this.uid = "";
        this.whereListed = "";
    }

    public String getName() {
        return name;
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public String getUid() {
        return uid;
    }

    public String getWhereListed() {
        return whereListed;
    }
}
