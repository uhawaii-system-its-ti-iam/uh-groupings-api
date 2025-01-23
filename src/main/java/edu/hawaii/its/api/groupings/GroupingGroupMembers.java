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
        this.resultCode = other.resultCode;
        this.groupPath = other.groupPath;
        this.size = other.size;
        this.members = other.members;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<GroupingGroupMember> getMembers() {
        return this.members;
    }

    public void setMembers(List<Subject> subjects) {
        this.members = new ArrayList<>();
        for (Subject subject : subjects) {
            this.members.add(new GroupingGroupMember(subject));
        }
    }

    public GroupingGroupMembers sort(String sortString, boolean isAscending) {
        Map<String, Comparator<GroupingGroupMember>> comparatorMap = Map.of(
                "name", Comparator.comparing(GroupingGroupMember::getName),
                "search_string0", Comparator.comparing(GroupingGroupMember::getUid),
                "subjectId", Comparator.comparing(GroupingGroupMember::getUhUuid)
        );
        Comparator<GroupingGroupMember> comparator = comparatorMap.get(sortString);

        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(this);
        groupingGroupMembers.members.sort(isAscending ? comparator : comparator.reversed());

        return groupingGroupMembers;
    }

    public GroupingGroupMembers paginate(int pageNumber, int pageSize) {
        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, members.size());

        GroupingGroupMembers groupingGroupMembers = new GroupingGroupMembers(this);
        groupingGroupMembers.members = fromIndex < toIndex
                ? members.subList(fromIndex, toIndex)
                : new ArrayList<>();

        return groupingGroupMembers;
    }
}
