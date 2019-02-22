package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public interface DatabaseSetupService {

    public void initialize(
            List<Person> users,
            List<WsSubjectLookup> lookups,
            List<Person> admins,
            Group adminGroup,
            Group appGroup);

}
