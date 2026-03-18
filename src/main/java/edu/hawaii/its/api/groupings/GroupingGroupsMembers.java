package edu.hawaii.its.api.groupings;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;


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
        this.allMembers = new GroupingMembers();
        setBasis(false);
        setInclude(false);
        setExclude(false);
        setOwners(false);
        setPageNumber(0);
        setPaginationComplete();
    }


    @Override
    public String getResultCode() {
        return resultCode;
    }


    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }


    @Override
    public String getGroupPath() {
        return groupPath;
    }


    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }


    private void setGroupsMembersList(GetMembersResults getMembersResults) {
        this.groupsMembersList = new ArrayList<>();
        for (GetMembersResult result : getMembersResults.getMembersResults()) {
            groupsMembersList.add(new GroupingGroupMembers(result));
        }
    }


    private void setAllMembers() {


        this.allMembers = new GroupingMembers();


        List<GroupingGroupMember> basis = safeList(getGroupingBasis().getMembers());
        List<GroupingGroupMember> include = safeList(getGroupingInclude().getMembers());
        List<GroupingGroupMember> exclude = safeList(getGroupingExclude().getMembers());


        Set<String> basisSet = toUuidSet(basis);
        Set<String> includeSet = toUuidSet(include);
        Set<String> excludeSet = toUuidSet(exclude);


        GroupingGroupMembers compositeGrouping = getCompositeGrouping();
        List<GroupingGroupMember> compositeMembers = safeList(compositeGrouping.getMembers());


        if (!compositeMembers.isEmpty()) {


            for (GroupingGroupMember member : compositeMembers) {
                String where = determineWhereListed(member, basisSet, includeSet);
                this.allMembers.getMembers().add(new GroupingMember(member, where));
            }


        } else {
            buildFallbackMembers(basis, include, excludeSet);
        }
    }


    private Set<String> toUuidSet(List<GroupingGroupMember> members) {
        return members.stream()
                .map(GroupingGroupMember::getUhUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    private List<GroupingGroupMember> safeList(List<GroupingGroupMember> list) {
        return list != null ? list : new ArrayList<>();
    }


    private String determineWhereListed(GroupingGroupMember member,
                                        Set<String> basisSet,
                                        Set<String> includeSet) {


        String uuid = member.getUhUuid();


        if (uuid == null) {
            return "Unknown";
        }


        boolean inBasis = basisSet.contains(uuid);
        boolean inInclude = includeSet.contains(uuid);


        if (inBasis && inInclude) return "Basis & Include";
        if (inBasis) return "Basis";
        if (inInclude) return "Include";


        return "Unknown";
    }


    private void buildFallbackMembers(List<GroupingGroupMember> basis,
                                      List<GroupingGroupMember> include,
                                      Set<String> excludeSet) {


        Map<String, GroupingMember> map = new LinkedHashMap<>();


        for (GroupingGroupMember member : basis) {
            String uuid = member.getUhUuid();
            if (uuid == null || excludeSet.contains(uuid)) continue;


            String where = include.stream()
                    .anyMatch(m -> uuid.equals(m.getUhUuid()))
                    ? "Basis & Include"
                    : "Basis";


            map.put(uuid, new GroupingMember(member, where));
        }


        for (GroupingGroupMember member : include) {
            String uuid = member.getUhUuid();
            if (uuid == null || excludeSet.contains(uuid)) continue;


            map.putIfAbsent(uuid, new GroupingMember(member, "Include"));
        }


        this.allMembers.getMembers().addAll(map.values());
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


    public GroupingGroupMembers getCompositeGrouping() {
        for (GroupingGroupMembers g : this.groupsMembersList) {
            String path = g.getGroupPath();
            if (!path.endsWith(GroupType.BASIS.value()) &&
                    !path.endsWith(GroupType.INCLUDE.value()) &&
                    !path.endsWith(GroupType.EXCLUDE.value()) &&
                    !path.endsWith(GroupType.OWNERS.value()) &&
                    !path.isEmpty()) {
                return g;
            }
        }
        return new GroupingGroupMembers();
    }

    private boolean hasMembers(String groupExtension) {
        for (GroupingGroupMembers g : this.groupsMembersList) {
            if (g.getGroupPath().endsWith(groupExtension)) {
                return !safeList(g.getMembers()).isEmpty();
            }
        }
        return false;
    }

    private GroupingGroupMembers getMembersOf(String groupExtension) {
        for (GroupingGroupMembers g : this.groupsMembersList) {
            if (g.getGroupPath().endsWith(groupExtension)) {
                return g;
            }
        }
        return new GroupingGroupMembers();
    }

    public void setPaginationCompleteTrue() {
        this.paginationComplete = true;
    }
}