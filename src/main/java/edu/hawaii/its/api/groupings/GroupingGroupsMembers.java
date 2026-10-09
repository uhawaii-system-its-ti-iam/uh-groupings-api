package edu.hawaii.its.api.groupings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<String, GroupingGroupMembers> groupsMembersByExtension;
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
        indexGroupsMembersByExtension(getMembersResults);
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
        this.groupsMembersByExtension = new HashMap<>();
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

    private void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    private void indexGroupsMembersByExtension(GetMembersResults getMembersResults) {
        this.groupsMembersByExtension = new HashMap<>();
        for (GetMembersResult getMembersResult : getMembersResults.getMembersResults()) {
            GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(getMembersResult);
            for (GroupType groupType : GroupType.values()) {
                if (groupingGroupMembers.getGroupPath().endsWith(groupType.value())) {
                    groupsMembersByExtension.putIfAbsent(groupType.value(), groupingGroupMembers);
                    break;
                }
            }
        }
    }

    private void setAllMembers() {
        this.allMembers = new GroupingMembers();
        List<GroupingGroupMember> basis = getGroupingBasis().getMembers();
        List<GroupingGroupMember> include = getGroupingInclude().getMembers();
        List<GroupingGroupMember> exclude = getGroupingExclude().getMembers();

        Set<String> includeUuids = include.stream()
                .map(GroupingGroupMember::getUhUuid).collect(Collectors.toSet());
        Set<String> excludeUuids = exclude.stream()
                .map(GroupingGroupMember::getUhUuid).collect(Collectors.toSet());
        Set<String> addedUuids = new HashSet<>();

        // Basis plus Include.
        for (GroupingGroupMember groupingGroupMember : basis) {
            String uhUuid = groupingGroupMember.getUhUuid();
            if (excludeUuids.contains(uhUuid) || !addedUuids.add(uhUuid)) {
                continue;
            }
            String whereListed = includeUuids.contains(uhUuid) ? "Basis & Include" : "Basis";
            this.allMembers.getMembers().add(new GroupingMember(groupingGroupMember, whereListed));
        }
        for (GroupingGroupMember groupingGroupMember : include) {
            String uhUuid = groupingGroupMember.getUhUuid();
            if (excludeUuids.contains(uhUuid) || !addedUuids.add(uhUuid)) {
                continue;
            }
            this.allMembers.getMembers().add(new GroupingMember(groupingGroupMember, "Include"));
        }
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

    private void setPaginationComplete() {
        paginationComplete = !isBasis && !isInclude && !isExclude && !isOwners;
    }

    private void setBasis(boolean basis) {
        isBasis = basis;
    }

    private void setInclude(boolean include) {
        isInclude = include;
    }

    private void setExclude(boolean exclude) {
        isExclude = exclude;
    }

    private void setOwners(boolean owners) {
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
        return !getMembersOf(groupExtension).getMembers().isEmpty();
    }

    private GroupingGroupMembers getMembersOf(String groupExtension) {
        return groupsMembersByExtension.getOrDefault(groupExtension, new GroupingGroupMembers());
    }
}
