package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.List;

public class AdminListsHolder {
    List<GroupingPath> allGroupingPaths = new ArrayList<>();
    Group adminGroup = new EmptyGroup();

    public AdminListsHolder() {
        //empty
    }

    public AdminListsHolder(List<GroupingPath> allGroupingPaths, Group adminGroup) {
        this.allGroupingPaths = allGroupingPaths;
        this.adminGroup = adminGroup;
    }

    public List<GroupingPath> getAllGroupingPaths() {
        return allGroupingPaths;
    }

    public void setAllGroupingPaths(List<GroupingPath> allGroupingPaths) {
        this.allGroupingPaths = allGroupingPaths;
    }

    public Group getAdminGroup() {
        return adminGroup;
    }

    public void setAdminGroup(Group adminGroup) {
        this.adminGroup = adminGroup;
    }
}
