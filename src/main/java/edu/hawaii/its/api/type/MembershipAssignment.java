package edu.hawaii.its.api.type;

import java.util.List;

public class MembershipAssignment {
    private List<Grouping> groupingsIn;
    private List<Grouping> groupingsToOptInTo;

    public List<Grouping> getGroupingsIn() {
        return groupingsIn;
    }

    public void setGroupingsIn(List<Grouping> groupingsIn) {
        this.groupingsIn = groupingsIn;
    }

    public List<Grouping> getGroupingsToOptInTo() {
        return groupingsToOptInTo;
    }

    public void setGroupingsToOptInTo(List<Grouping> groupingsToOptInTo) {
        this.groupingsToOptInTo = groupingsToOptInTo;
    }

}
