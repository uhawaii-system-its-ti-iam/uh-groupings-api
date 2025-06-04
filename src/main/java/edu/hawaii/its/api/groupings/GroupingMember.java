package edu.hawaii.its.api.groupings;

import org.apache.commons.lang3.StringUtils;

import edu.hawaii.its.api.wrapper.HasMemberResult;

/**
 * GroupingMember is used to hydrate the allMembers section of a grouping.
 */
public class GroupingMember {
    private final String uid;
    private final String uhUuid;
    private final String name;
    private final String firstName;
    private final String lastName;
    private String whereListed;
    private static final String MEMBER = "IS_MEMBER";

    public GroupingMember(GroupingGroupMember groupingGroupMember, String whereListed) {
        this.name = groupingGroupMember.getName();
        this.firstName = groupingGroupMember.getFirstName();
        this.lastName = groupingGroupMember.getLastName();
        this.uhUuid = groupingGroupMember.getUhUuid();
        this.uid = groupingGroupMember.getUid();
        this.whereListed = whereListed;
    }

    public GroupingMember(HasMemberResult hasMemberResult, String groupingExtension) {
        this.name = hasMemberResult.getName();
        this.firstName = hasMemberResult.getFirstName();
        this.lastName = hasMemberResult.getLastName();
        this.uhUuid = hasMemberResult.getUhUuid();
        this.uid = hasMemberResult.getUid();
        setWhereListed(hasMemberResult, groupingExtension);
    }

    public GroupingMember(HasMemberResult hasMemberResult1, String groupingExtension1,
            HasMemberResult hasMemberResult2, String groupingExtension2) {
        this.name = hasMemberResult1.getName();
        this.firstName = hasMemberResult1.getFirstName();
        this.lastName = hasMemberResult1.getLastName();
        this.uhUuid = hasMemberResult1.getUhUuid();
        this.uid = hasMemberResult1.getUid();
        setWhereListed(hasMemberResult1, groupingExtension1, hasMemberResult2, groupingExtension2);
    }

    public GroupingMember() {
        this.name = "";
        this.firstName = "";
        this.lastName = "";
        this.uhUuid = "";
        this.uid = "";
        this.whereListed = "";
    }
    
    public GroupingMember(HasMemberResult result) {
        this.name = result.getName();
        this.firstName = result.getFirstName();
        this.lastName = result.getLastName();
        this.uhUuid = result.getUhUuid();
        this.uid = result.getUid();
        this.whereListed = "";
    }
    public String getName() {
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    private void setWhereListed(HasMemberResult hasMemberResult, String groupingExtension) {
        this.whereListed = hasMemberResult.getResultCode().equals(MEMBER)
                ? StringUtils.capitalize(groupingExtension)
                : "";
    }

    private void setWhereListed(HasMemberResult hasMemberResult1, String groupingExtension1,
            HasMemberResult hasMemberResult2, String groupingExtension2) {
        boolean isGroupingExtension1 = hasMemberResult1.getResultCode().equals(MEMBER);
        boolean isGroupingExtension2 = hasMemberResult2.getResultCode().equals(MEMBER);

        if (!isGroupingExtension1 && !isGroupingExtension2) {
            this.whereListed = "";
            return;
        }

        if (isGroupingExtension1 && isGroupingExtension2) {
            this.whereListed = StringUtils.capitalize(groupingExtension1) + " & " + StringUtils.capitalize(groupingExtension2);
        } else {
            this.whereListed = StringUtils.capitalize(isGroupingExtension1 ? groupingExtension1 : groupingExtension2);
        }
    }
}
