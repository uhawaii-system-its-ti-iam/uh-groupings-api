package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GenericServiceResult;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HelperService {

    String extractFirstMembershipID(WsGetMembershipsResults wsGetMembershipsResults);

    WsGetMembershipsResults membershipsResults(String username, String group);

    List<String> extractGroupings(List<String> groupPaths);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action,
            Person person);

    GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action);

    List<Grouping> makeGroupings(List<String> groupingPaths);

    List<GroupingPath> makePaths(List<String> groupingPaths);

    String parentGroupingPath(String group);

    String nameGroupingPath(String group);

    Map<String, String> memberAttributeMapSetKeys();

    GenericServiceResult swaggerToString(String currentUser) throws IOException;
}
