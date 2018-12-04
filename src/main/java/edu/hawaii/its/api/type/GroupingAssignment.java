package edu.hawaii.its.api.type;

import java.util.List;

public class GroupingAssignment extends MembershipAssignment{
    private List<Grouping> groupingsOwned;
    private List<Grouping> groupingsOptedOutOf;
    private List<Grouping> groupingsOptedInTo;
    private List<Grouping> groupingsToOptOutOf;

    public GroupingAssignment(){

    }

    public List<Grouping> getGroupingsOwned() {
        return groupingsOwned;
    }

    public void setGroupingsOwned(List<Grouping> groupingsOwned) {
        this.groupingsOwned = groupingsOwned;
    }

    public List<Grouping> getGroupingsToOptOutOf() {
        return groupingsToOptOutOf;
    }

    public void setGroupingsToOptOutOf(List<Grouping> groupingsToOptOutOf) {
        this.groupingsToOptOutOf = groupingsToOptOutOf;
    }

    public List<Grouping> getGroupingsOptedOutOf() {
        return groupingsOptedOutOf;
    }

    public void setGroupingsOptedOutOf(List<Grouping> groupingsOptedOutOf) {
        this.groupingsOptedOutOf = groupingsOptedOutOf;
    }

    public List<Grouping> getGroupingsOptedInTo() {
        return groupingsOptedInTo;
    }

    public void setGroupingsOptedInTo(List<Grouping> groupingsOptedInTo) {
        this.groupingsOptedInTo = groupingsOptedInTo;
    }


}
