package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.ResultMetadataHolder;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("helperService")
public class HelperServiceImpl implements HelperService {

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.basis_plus_include}")
    private String BASIS_PLUS_INCLUDE;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.stale_subject_id}")
    private String STALE_SUBJECT_ID;

    public static final Log logger = LogFactory.getLog(HelperServiceImpl.class);

    /**
     * Return true if username is a UH id number
     */
    @Override
    public boolean isUhUuid(String naming) {
        return naming != null && naming.matches("\\d+");
    }

    //returns the first membership id in the list of membership ids inside of the WsGerMembershipsResults object
    @Override
    public String extractFirstMembershipID(WsGetMembershipsResults wsGetMembershipsResults) {
        if (wsGetMembershipsResults != null
                && wsGetMembershipsResults.getWsMemberships() != null
                && wsGetMembershipsResults.getWsMemberships()[0] != null
                && wsGetMembershipsResults.getWsMemberships()[0].getMembershipId() != null) {

            return wsGetMembershipsResults
                    .getWsMemberships()[0]
                            .getMembershipId();
        }
        return "";
    }

    /**
     * Make a groupingsServiceResult with the result code from the metadataHolder and the action string.
     */
    @Override
    public GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action) {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultMetadataHolder.getResultMetadata().getResultCode());

        if (groupingsServiceResult.getResultCode().startsWith(FAILURE)) {
            throw new GroupingsServiceResultException(groupingsServiceResult);
        }

        return groupingsServiceResult;
    }

    @Override
    public GroupingsServiceResult makeGroupingsServiceResult(ResultMetadataHolder resultMetadataHolder, String action,
            Person person) {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultMetadataHolder.getResultMetadata().getResultCode());
        groupingsServiceResult.setPerson(person);

        if (groupingsServiceResult.getResultCode().startsWith(FAILURE)) {
            throw new GroupingsServiceResultException(groupingsServiceResult);
        }

        return groupingsServiceResult;
    }

    @Override
    public GroupingsServiceResult makeGroupingsServiceResult(String resultCode, String action) {
        GroupingsServiceResult groupingsServiceResult = new GroupingsServiceResult();
        groupingsServiceResult.setAction(action);
        groupingsServiceResult.setResultCode(resultCode);

        if (groupingsServiceResult.getResultCode().startsWith(FAILURE)) {
            throw new GroupingsServiceResultException(groupingsServiceResult);
        }
        return groupingsServiceResult;
    }

    /**
     * Take a list of grouping path strings and return a list of GroupingPath objects.
     */
    @Override
    public List<GroupingPath> makePaths(List<String> groupingPaths) {
        List<GroupingPath> paths = new ArrayList<>();
        if (groupingPaths.size() > 0) {
            paths = groupingPaths.stream().map(GroupingPath::new).collect(Collectors.toList());
        }
        return paths;
    }

    /**
     * Remove one of the words (:exclude, :include, :owners ...) from the end of the string.
     */
    @Override
    public String parentGroupingPath(String group) {
        if (group != null) {
            if (group.endsWith(EXCLUDE)) {
                return group.substring(0, group.length() - EXCLUDE.length());
            } else if (group.endsWith(INCLUDE)) {
                return group.substring(0, group.length() - INCLUDE.length());
            } else if (group.endsWith(OWNERS)) {
                return group.substring(0, group.length() - OWNERS.length());
            } else if (group.endsWith(BASIS)) {
                return group.substring(0, group.length() - BASIS.length());
            } else if (group.endsWith(BASIS_PLUS_INCLUDE)) {
                return group.substring(0, group.length() - BASIS_PLUS_INCLUDE.length());
            }
            return group;
        }
        return "";
    }

    /**
     * Get the name of a grouping from groupPath.
     */
    @Override
    public String nameGroupingPath(String groupPath) {
        String parentPath = parentGroupingPath(groupPath);
        if ("".equals(parentPath)) {
            return "";
        }
        return parentPath.substring(parentPath.lastIndexOf(":") + 1, parentPath.length());
    }

    //makes a group filled with members from membersResults
    @Override
    public Map<String, Group> makeGroups(WsGetMembersResults membersResults) {
        Map<String, Group> groups = new HashMap<>();
        if (membersResults.getResults().length > 0) {
            String[] attributeNames = membersResults.getSubjectAttributeNames();

            for (WsGetMembersResult result : membersResults.getResults()) {
                WsSubject[] subjects = result.getWsSubjects();
                Group group = new Group(result.getWsGroup().getName());

                if (subjects == null || subjects.length == 0) {
                    continue;
                }
                for (WsSubject subject : subjects) {
                    if (subject == null) {
                        continue;
                    }
                    Person personToAdd = makePerson(subject, attributeNames);
                    if (group.getPath().endsWith(BASIS) && subject.getSourceId() != null
                            && subject.getSourceId().equals(STALE_SUBJECT_ID)) {
                        personToAdd.setUsername("User Not Available.");
                    }
                    group.addMember(personToAdd);
                }
                groups.put(group.getPath(), group);
            }
        }
        // Return empty group if for any unforeseen results.
        return groups;
    }

    // Makes a person with all attributes in attributeNames.
    @Override
    public Person makePerson(WsSubject subject, String[] attributeNames) {
        if (subject == null || subject.getAttributeValues() == null) {
            return new Person();
        }

        Person person = new Person();
        for (int i = 0; i < subject.getAttributeValues().length; i++) {
            person.addAttribute(attributeNames[i], subject.getAttributeValue(i));
        }

        // uhUuid is the only attribute not actually 
        // in the WsSubject attribute array.
        person.addAttribute(UHUUID, subject.getId());

        return person;
    }

    @Override
    //take a list of WsGroups ans return a list of the paths for all of those groups
    public List<String> extractGroupPaths(List<WsGroup> groups) {
        Set<String> names = new LinkedHashSet<>();
        if (groups != null) {
            names = groups
                    .parallelStream()
                    .map(WsGroup::getName)
                    .collect(Collectors.toSet());

        }
        return new ArrayList<>(names);
    }
}
