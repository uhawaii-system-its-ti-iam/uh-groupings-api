package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

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
    private boolean isComposite;
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
        setComposite(hasMembers(GroupType.COMPOSITE.value()));
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
        setComposite(false);
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
        
        // Get the composite group from Grouper - it already contains (basis + include) - exclude
        GroupingGroupMembers composite = getGroupingComposite();
        List<GroupingGroupMember> compositeMembers = composite.getMembers();
        
        // Get basis and include to determine "where listed" for each member
        List<GroupingGroupMember> basis = getGroupingBasis().getMembers();
        List<GroupingGroupMember> include = getGroupingInclude().getMembers();
        
        // Use the composite group members (which Grouper has already calculated correctly)
        for (GroupingGroupMember member : compositeMembers) {
            boolean inBasis = basis.stream()
                    .anyMatch(b -> b.getUhUuid().equals(member.getUhUuid()));
            boolean inInclude = include.stream()
                    .anyMatch(i -> i.getUhUuid().equals(member.getUhUuid()));
            
            String whereListed;
            if (inBasis && inInclude) {
                whereListed = "Basis & Include";
            } else if (inBasis) {
                whereListed = "Basis";
            } else {
                whereListed = "Include";
            }
            
            this.allMembers.getMembers().add(new GroupingMember(member, whereListed));
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

    public void setPaginationComplete() {
        paginationComplete = !isBasis && !isInclude && !isExclude && !isOwners && !isComposite;
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

    public boolean isComposite() {
        return isComposite;
    }

    public void setComposite(boolean composite) {
        isComposite = composite;
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

    public GroupingGroupMembers getGroupingComposite() {
        return getMembersOf(GroupType.COMPOSITE.value());
    }

    private boolean hasMembers(String groupExtension) {
        for (GroupingGroupMembers groupingGroupMembers : this.groupsMembersList) {
            String path = groupingGroupMembers.getGroupPath();
            boolean matches;
            if (groupExtension.isEmpty()) {
                // For composite group, find the path that doesn't end with any known suffix
                matches = !path.endsWith(GroupType.BASIS.value()) &&
                        !path.endsWith(GroupType.INCLUDE.value()) &&
                        !path.endsWith(GroupType.EXCLUDE.value()) &&
                        !path.endsWith(GroupType.OWNERS.value());
            } else {
                matches = path.endsWith(groupExtension);
            }
            if (matches) {
                return !groupingGroupMembers.getMembers().isEmpty();
            }
        }
        return false;
    }

    private GroupingGroupMembers getMembersOf(String groupExtension) {
        for (GroupingGroupMembers groupingGroupMembers : this.groupsMembersList) {
            String path = groupingGroupMembers.getGroupPath();
            if (groupExtension.isEmpty()) {
                // For composite group, find the path that doesn't end with any known suffix
                if (!path.endsWith(GroupType.BASIS.value()) &&
                        !path.endsWith(GroupType.INCLUDE.value()) &&
                        !path.endsWith(GroupType.EXCLUDE.value()) &&
                        !path.endsWith(GroupType.OWNERS.value())) {
                    return groupingGroupMembers;
                }
            } else if (path.endsWith(groupExtension)) {
                return groupingGroupMembers;
            }
        }
        return new GroupingGroupMembers();
    }
}
