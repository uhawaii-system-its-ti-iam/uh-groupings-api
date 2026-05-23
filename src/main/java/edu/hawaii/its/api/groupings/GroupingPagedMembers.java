package edu.hawaii.its.api.groupings;

import java.util.ArrayList;
import java.util.List;

public class GroupingPagedMembers {

    private List<GroupingMember> members;
    private Integer pageNumber;
    private Integer totalCount;

    public GroupingPagedMembers() {
        this.members = new ArrayList<>();
        this.pageNumber = 1;
        this.totalCount = 0;
    }

    public List<GroupingMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupingMember> members) {
        this.members = members;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}