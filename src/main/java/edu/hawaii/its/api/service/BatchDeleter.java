package edu.hawaii.its.api.service;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

import java.util.List;

public interface BatchDeleter {
    WsDeleteMemberResults makeWsDeleteMemberResults(String group, String memberToDelete);
}
