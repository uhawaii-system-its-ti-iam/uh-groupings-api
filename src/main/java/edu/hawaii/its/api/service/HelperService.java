package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;

import java.time.Period;
import java.util.List;

public interface HelperService {

    String extractFirstMembershipID(WsGetMembershipsResults wsGetMembershipsResults);

    WsGetMembershipsResults membershipsResults(String username, String group);

    List<String> extractGroupings(List<String> groupPaths);

    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action);
    GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action, Person person);

    GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action);


    List<Grouping> makeGroupings(List<String> groupingPaths);

    String parentGroupingPath(String group);
}
