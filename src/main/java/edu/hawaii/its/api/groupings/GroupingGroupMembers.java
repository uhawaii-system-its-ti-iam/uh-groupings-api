package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * When getMembers is called, GroupingGroupMembers holds the information about UH affiliates that are listed in a
 * group such as include, exclude, owners.
 */
public class GroupingGroupMembers implements GroupingResult {
    private static final Map<String, Comparator<GroupingGroupMember>> SORT_COMPARATORS = Map.of(
            "name", Comparator.comparing(GroupingGroupMember::getName),
            "search_string0", Comparator.comparing(GroupingGroupMember::getUid),
            "subjectId", Comparator.comparing(GroupingGroupMember::getUhUuid)
    );

    private String resultCode;
    private String groupPath;
    private int size;

    private List<GroupingGroupMember> members;

    public GroupingGroupMembers(GetMembersResult getMembersResult) {
        setResultCode(getMembersResult.getResultCode());
        setGroupPath(getMembersResult.getGroup().getGroupPath());
        setMembers(getMembersResult.getSubjects());
        setSize(members.size());
    }

    public GroupingGroupMembers(SubjectsResults subjectsResults) {
        setResultCode(subjectsResults.getResultCode());
        setGroupPath(subjectsResults.getGroup().getGroupPath());
        setMembers(subjectsResults.getSubjects());
        setSize(members.size());
    }

    public GroupingGroupMembers() {
        setResultCode("");
        setGroupPath("");
        setMembers(new ArrayList<>());
        setSize(0);
    }

    private GroupingGroupMembers(GroupingGroupMembers other) {
        this(other, new ArrayList<>(other.members));
    }

    private GroupingGroupMembers(GroupingGroupMembers other, List<GroupingGroupMember> members) {
        setResultCode(other.getResultCode());
        setGroupPath(other.getGroupPath());
        setGroupingMembers(members);
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

    private void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public int getSize() {
        return size;
    }

    private void setSize(int size) {
        this.size = size;
    }

    public List<GroupingGroupMember> getMembers() {
        return this.members;
    }

    private void setMembers(List<Subject> subjects) {
        this.members = new ArrayList<>();
        for (Subject subject : subjects) {
            this.members.add(new GroupingGroupMember(subject));
        }
    }

    public GroupingGroupMembers sort(String sortString, boolean isAscending) {
        Comparator<GroupingGroupMember> comparator = SORT_COMPARATORS.get(sortString);

        // do not sort in-place to prevent any side effects in pagination
        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(this);
        groupingGroupMembers.members.sort(isAscending ? comparator : comparator.reversed());

        return groupingGroupMembers;
    }

    public GroupingGroupMembers paginate(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, members.size());

        return fromIndex < toIndex
                ? new GroupingGroupMembers(this, new ArrayList<>(members.subList(fromIndex, toIndex)))
                : new GroupingGroupMembers(this, new ArrayList<>());
    }
}
