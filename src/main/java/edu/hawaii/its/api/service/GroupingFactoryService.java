package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupingsServiceResult;

import java.util.List;

public interface GroupingFactoryService {

    List<GroupingsServiceResult> addGrouping(
            String username,
            String groupingPath);


    List<GroupingsServiceResult> deleteGrouping(String adminUsername, String groupingPath);

    List<GroupingsServiceResult> markGroupForPurge(String adminUsername, String groupingPath);

    boolean isPathEmpty(String adminUsername, String groupingPath);

}
