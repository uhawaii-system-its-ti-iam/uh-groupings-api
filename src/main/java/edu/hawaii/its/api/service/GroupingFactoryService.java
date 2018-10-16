package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import java.util.List;

public interface GroupingFactoryService {

    public List<GroupingsServiceResult> addGrouping(
            String username,
            String groupingPath);


    public List<GroupingsServiceResult> deleteGrouping(String adminUsername, String groupingPath);

    public boolean isPathEmpty(String adminUsername, String groupingPath);

}
