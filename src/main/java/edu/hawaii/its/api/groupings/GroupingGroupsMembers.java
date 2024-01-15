package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;

/**
 * When getMembers is called, GroupingGroupsMembers holds the information about UH affiliates that are listed in a grouping.
 */
public class GroupingGroupsMembers implements GroupingResult {
    private String resultCode;
    private String groupPath;
    private List<GroupingGroupMembers> groupsMembersList;
    private boolean isBasis;
    private boolean isInclude;
    private boolean isExclude;
    private boolean isOwners;
    private boolean paginationComplete;
    private GroupingMembers allMembers;
    private Integer pageNumber;

    public GroupingGroupsMembers(GetMembersResults getMembersResults) {
        setGroupPath("");
        setResultCode(getMembersResults.getResultCode());
        setGroupsMembersList(getMembersResults);
        setAllMembers();
        setBasis(hasMembers(GroupType.BASIS.value()));
        setInclude(hasMembers(GroupType.INCLUDE.value()));
        setExclude(hasMembers(GroupType.EXCLUDE.value()));
        setOwners(hasMembers(GroupType.OWNERS.value()));
        setPageNumber(0);
        setPaginationComplete();
    }

    public GroupingGroupsMembers() {
        setGroupPath("");
        setResultCode("");
        this.groupsMembersList = new ArrayList<>();
        setAllMembers();
        setBasis(false);
        setInclude(false);
        setExclude(false);
        setOwners(false);
        setPageNumber(0);
        setPaginationComplete();
    }

    @Override public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    private void setGroupsMembersList(GetMembersResults getMembersResults) {
        this.groupsMembersList = new ArrayList<>();
        for (GetMembersResult getMembersResult : getMembersResults.getMembersResults()) {
            groupsMembersList.add(new GroupingGroupMembers(getMembersResult));
        }
    }

    private void setAllMembers() {
        this.allMembers = new GroupingMembers();
        List<GroupingGroupMember> basis = getGroupingBasis().getMembers();
        List<GroupingGroupMember> include = getGroupingInclude().getMembers();
        List<GroupingGroupMember> exclude = getGroupingExclude().getMembers();

        List<GroupingGroupMember> intersectionBasisInclude = basis.stream()
                .distinct().filter(groupingsGroupMember -> include.stream()
                        .anyMatch(includeMember -> includeMember.getUhUuid().equals(groupingsGroupMember.getUhUuid())))
                .collect(Collectors.toList());

        // Basis plus Include.
        for (GroupingGroupMember groupingGroupMember : intersectionBasisInclude) {
            this.allMembers.getMembers().add(new GroupingMember(groupingGroupMember, "Basis & Include"));
        }
        for (GroupingGroupMember groupingGroupMember : basis) {
            if (this.allMembers.getMembers().stream()
                    .noneMatch(groupingMember -> groupingMember.getUhUuid().equals(groupingGroupMember.getUhUuid()))) {
                this.allMembers.getMembers().add(new GroupingMember(groupingGroupMember, "Basis"));
            }
        }
        for (GroupingGroupMember groupingGroupMember : include) {
            if (this.allMembers.getMembers().stream()
                    .noneMatch(groupingMember -> groupingMember.getUhUuid().equals(groupingGroupMember.getUhUuid()))) {
                this.allMembers.getMembers().add(new GroupingMember(groupingGroupMember, "Include"));
            }
        }

        // Minus Exclude
        this.allMembers.getMembers().removeIf(groupingMember -> exclude.stream()
                .anyMatch(excludeMember -> excludeMember.getUhUuid().equals(groupingMember.getUhUuid())));
    }

    public GroupingMembers getAllMembers() {
        return allMembers;
    }

    public boolean isBasis() {
        return isBasis;
    }

    public boolean isInclude() {
        return isInclude;
    }

    public boolean isExclude() {
        return isExclude;
    }

    public boolean isOwners() {
        return isOwners;
    }

    public boolean isPaginationComplete() {
        return paginationComplete;
    }

    public void setPaginationComplete() {
        paginationComplete = !isBasis && !isInclude && !isExclude && !isOwners;
    }

    public void setBasis(boolean basis) {
        isBasis = basis;
    }

    public void setInclude(boolean include) {
        isInclude = include;
    }

    public void setExclude(boolean exclude) {
        isExclude = exclude;
    }

    public void setOwners(boolean owners) {
        isOwners = owners;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public GroupingGroupMembers getGroupingBasis() {
        return getMembersOf(GroupType.BASIS.value());
    }

    public GroupingGroupMembers getGroupingInclude() {
        return getMembersOf(GroupType.INCLUDE.value());
    }

    public GroupingGroupMembers getGroupingExclude() {
        return getMembersOf(GroupType.EXCLUDE.value());
    }

    public GroupingGroupMembers getGroupingOwners() {
        return getMembersOf(GroupType.OWNERS.value());
    }

    private boolean hasMembers(String groupExtension) {
        for (GroupingGroupMembers groupingGroupMembers : this.groupsMembersList) {
            if (groupingGroupMembers.getGroupPath().endsWith(groupExtension)) {
                return !groupingGroupMembers.getMembers().isEmpty();
            }
        }
        return false;
    }

    private GroupingGroupMembers getMembersOf(String groupExtension) {
        for (GroupingGroupMembers groupingGroupMembers : this.groupsMembersList) {
            if (groupingGroupMembers.getGroupPath().endsWith(groupExtension)) {
                return groupingGroupMembers;
            }
        }
        return new GroupingGroupMembers();
    }

    public void setPaginationCompleteTrue() {
        this.paginationComplete = true;
    }
}
