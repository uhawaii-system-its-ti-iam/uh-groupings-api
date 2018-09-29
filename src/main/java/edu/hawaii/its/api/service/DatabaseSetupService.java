package edu.hawaii.its.api.service;

import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import java.util.List;

public interface DatabaseSetupService {

    public void initialize(
            PersonRepository personRepository,
            GroupRepository groupRepository,
            GroupingRepository groupingRepository,
            MembershipRepository membershipRepository,
            List<Person> users,
            List<WsSubjectLookup> lookups,
            List<Person> admins,
            Group adminGroup,
            Group appGroup);

}
