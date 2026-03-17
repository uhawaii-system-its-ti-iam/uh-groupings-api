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

    /**
     * Set all members using Grouper's composite group directly.
     * The composite group already calculates (basis + include) - exclude correctly.
     * If the composite group is available, use it directly. Otherwise, fall back to manual calculation.
     */
    private void setAllMembers() {
        this.allMembers = new GroupingMembers();
        
        // Try to use the composite grouping directly (Grouper already calculates (basis + include) - exclude)
        GroupingGroupMembers compositeGrouping = getCompositeGrouping();
        if (!compositeGrouping.getMembers().isEmpty()) {
            // Use composite group members directly - this is the correct membership from Grouper
            List<GroupingGroupMember> basis = getGroupingBasis().getMembers();
            List<GroupingGroupMember> include = getGroupingInclude().getMembers();
            
            for (GroupingGroupMember member : compositeGrouping.getMembers()) {
                String whereListed = determineWhereListed(member, basis, include);
                this.allMembers.getMembers().add(new GroupingMember(member, whereListed));
            }
        } else {
            // Fall back to manual calculation if composite group is not available
            setAllMembersFallback();
        }
    }
    
    /**
     * Determine where a member is listed (Basis, Include, or Basis & Include).
     */
    private String determineWhereListed(GroupingGroupMember member, 
            List<GroupingGroupMember> basis, List<GroupingGroupMember> include) {
        boolean inBasis = basis.stream()
                .anyMatch(b -> b.getUhUuid().equals(member.getUhUuid()));
        boolean inInclude = include.stream()
                .anyMatch(i -> i.getUhUuid().equals(member.getUhUuid()));
        
        if (inBasis && inInclude) {
            return "Basis & Include";
        } else if (inBasis) {
            return "Basis";
        } else if (inInclude) {
            return "Include";
        }
        return "Basis"; // Default to Basis if not found (shouldn't happen)
    }
    
    /**
     * Fallback method for calculating all members manually.
     * Used when composite group is not available in the results.
     */
    private void setAllMembersFallback() {
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

    /**
     * Get the composite grouping members (the grouping path without :basis, :include, :exclude, :owners suffix).
     * This represents the actual membership as calculated by Grouper: (basis + include) - exclude.
     */
    public GroupingGroupMembers getCompositeGrouping() {
        for (GroupingGroupMembers groupingGroupMembers : this.groupsMembersList) {
            String path = groupingGroupMembers.getGroupPath();
            // Composite grouping path doesn't end with any of the subgroup suffixes
            if (!path.endsWith(GroupType.BASIS.value()) && 
                !path.endsWith(GroupType.INCLUDE.value()) && 
                !path.endsWith(GroupType.EXCLUDE.value()) && 
                !path.endsWith(GroupType.OWNERS.value()) &&
                !path.isEmpty()) {
                return groupingGroupMembers;
            }
        }
        return new GroupingGroupMembers();
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
