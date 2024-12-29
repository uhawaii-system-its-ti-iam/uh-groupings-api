package edu.hawaii.its.api.groupings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;

/**
 * GroupingMembers is used to hydrate the all members sections of a grouping, all members is (basis + include) - exclude.
 */
public class GroupingMembers {
    private List<GroupingMember> members;

    public GroupingMembers() {
        this.members = new ArrayList<>();
    }

    @JsonCreator
    public GroupingMembers(@JsonProperty("members") List<GroupingMember> members) {
        this.members = members;
    }

    public GroupingMembers(HasMembersResults hasMembersResults) {
        setMembers(hasMembersResults);
    }

    public GroupingMembers(HasMembersResults hasMembersResults1, HasMembersResults hasMembersResults2) {
        setMembers(hasMembersResults1, hasMembersResults2);
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

    private void setMembers(HasMembersResults hasMembersResults) {
        this.members = new ArrayList<>();
        for (HasMemberResult hasMemberResult : hasMembersResults.getResults()) {
            this.members.add(new GroupingMember(hasMemberResult, hasMembersResults.getGroup().getExtension()));
        }
    }

    private void setMembers(HasMembersResults hasMembersResults1, HasMembersResults hasMembersResults2) {
        this.members = new ArrayList<>();
        for (int i = 0; i < hasMembersResults1.getResults().size(); i++) {
            this.members.add(new GroupingMember(
                    hasMembersResults1.getResults().get(i), hasMembersResults1.getGroup().getExtension(),
                    hasMembersResults2.getResults().get(i), hasMembersResults2.getGroup().getExtension()));
        }
    }
}
