package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@Service
public class OotbGroupingPropertiesService {

    public static final Log logger = LogFactory.getLog(OotbGroupingPropertiesService.class);

    @Value("${groupings.api.ootb.groupings_users}")
    private String OOTB_USERS;

    @Autowired
    @Qualifier("HasMembersResultsOOTBBean")
    private HasMembersResults hasMembersResults;

    @Autowired
    @Qualifier("FindGroupsResultsOOTBBean")
    private FindGroupsResults findGroupsResults;

    @Autowired
    @Qualifier("GetSubjectsResultsOOTBBean")
    private SubjectsResults subjectsResults;

    @Autowired
    @Qualifier("GroupSaveResultsOOTBBean")
    private GroupSaveResults groupSaveResults;

    @Autowired
    @Qualifier("AssignAttributesOOTBBean")
    private AssignAttributesResults assignAttributesResults;

    @Autowired
    @Qualifier("GetMembersResultsOOTBBean")
    private GetMembersResults getMembersResults;

    @Autowired
    @Qualifier("AddMemberResultsOOTBBean")
    private AddMembersResults addMembersResults;

    @Autowired
    @Qualifier("RemoveMembersResultsOOTBBean")
    private RemoveMembersResults removeMembersResults;

    @Autowired
    @Qualifier("AttributeAssignmentResultsOOTBBean")
    private GroupAttributeResults groupAttributeResults;

    @Autowired
    @Qualifier("GetGroupsResultsOOTBBean")
    private GetGroupsResults getGroupsResults;

    public HasMembersResults getHasMembersResultsBean() {
        return this.hasMembersResults;
    }

    public FindGroupsResults getFindGroupsResults() {
        return this.findGroupsResults;
    }

    public SubjectsResults getSubjectsResults() {
        return this.subjectsResults;
    }

    public GroupSaveResults getGroupSaveResults() {
        return this.groupSaveResults;
    }

    public AssignAttributesResults getAssignAttributesResults() {
        return this.assignAttributesResults;
    }

    public GetMembersResults getMembersResults() {
        return this.getMembersResults;
    }

    public AddMembersResults getAddMembersResults() {
        return this.addMembersResults;
    }

    public RemoveMembersResults getRemoveMembersResults() {
        return removeMembersResults;
    }

    public GroupAttributeResults getGroupAttributeResults() {
        return groupAttributeResults;
    }

    public GetGroupsResults getGroupsResults() {
        return getGroupsResults;
    }

    /* Updating Member(s) */

    public void removeMember(String groupPath, String uhIdentifier) {
        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] filteredSubjects = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .filter(wsSubject -> !uhIdentifier.equals(wsSubject.getId()))
                                .toArray(WsSubject[]::new);

                        filteredResult.setWsSubjects(filteredSubjects);
                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembersResults);
    }

    public void removeMembers(String groupPath, List<String> uhIdentifiers) {
        WsGetMembersResult[] updatedWsGetMembersResults = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        WsGetMembersResult filteredResult = new WsGetMembersResult();
                        filteredResult.setWsGroup(wsGetMembersResult.getWsGroup());
                        filteredResult.setResultMetadata(wsGetMembersResult.getResultMetadata());
                        WsSubject[] filteredSubjects = Arrays.stream(wsGetMembersResult.getWsSubjects())
                                .filter(wsSubject -> !uhIdentifiers.contains(wsSubject.getId()))
                                .toArray(WsSubject[]::new);

                        filteredResult.setWsSubjects(filteredSubjects);
                        return filteredResult;
                    } else {
                        return wsGetMembersResult;
                    }
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembersResults);
    }

    public void addMember(String groupPath, String uhIdentifier) {

        /* Map< uhIdentifier, Pair< Name, Attributes >> for list of users can be added in ootb groupings page. */
        Map<String, Pair<String, String[]>> idToNameAndAttributesMap = Arrays.stream(wsGetMembersResultsList())
                .filter(wsGetMembersResult -> OOTB_USERS.equals(wsGetMembersResult.getWsGroup().getName()))
                .flatMap(wsGetMembersResult -> Arrays.stream(wsGetMembersResult.getWsSubjects()))
                .filter(wsSubject -> uhIdentifier.equals(wsSubject.getId()))
                .collect(Collectors.toMap(
                        WsSubject::getId,
                        wsSubject -> Pair.of(wsSubject.getName(), wsSubject.getAttributeValues())
                ));

        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> updatedSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));
                        WsSubject newMember = new WsSubject();
                        Pair<String, String[]> nameAndAttributes = idToNameAndAttributesMap.get(uhIdentifier);
                        newMember.setId(uhIdentifier);
                        newMember.setName(nameAndAttributes.getLeft());
                        newMember.setAttributeValues(nameAndAttributes.getRight());
                        newMember.setSuccess("T");
                        newMember.setResultCode("SUCCESS");
                        newMember.setSourceId("UH core LDAP");
                        updatedSubjectsList.add(newMember);

                        wsGetMembersResult.setWsSubjects(updatedSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);
        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);
    }

    public void addMembers(String groupPath, List<String> uhIdentifiers) {

        /* Map< uhIdentifier, Pair< Name, Attributes >> for list of users can be added in ootb groupings page. */
        Map<String, Pair<String, String[]>> idToNameAndAttributesMap = Arrays.stream(wsGetMembersResultsList())
                .filter(wsGetMembersResult -> OOTB_USERS.equals(wsGetMembersResult.getWsGroup().getName()))
                .flatMap(wsGetMembersResult -> Arrays.stream(wsGetMembersResult.getWsSubjects()))
                .filter(wsSubject -> uhIdentifiers.contains(wsSubject.getId()))
                .collect(Collectors.toMap(
                        WsSubject::getId,
                        wsSubject -> Pair.of(wsSubject.getName(), wsSubject.getAttributeValues())
                ));

        WsGetMembersResult[] updatedWsGetMembers = Arrays.stream(wsGetMembersResultsList())
                .map(wsGetMembersResult -> {
                    if (wsGetMembersResult.getWsGroup() != null && groupPath.equals(
                            wsGetMembersResult.getWsGroup().getName())) {
                        List<WsSubject> updatedSubjectsList =
                                new ArrayList<>(Arrays.asList(wsGetMembersResult.getWsSubjects()));

                        uhIdentifiers.stream()
                                .filter(idToNameAndAttributesMap::containsKey)
                                .forEach(id -> {
                                    WsSubject newMember = new WsSubject();
                                    Pair<String, String[]> nameAndAttributes = idToNameAndAttributesMap.get(id);
                                    newMember.setId(id);
                                    newMember.setName(nameAndAttributes.getLeft());
                                    newMember.setAttributeValues(nameAndAttributes.getRight());
                                    newMember.setSuccess("T");
                                    newMember.setResultCode("SUCCESS");
                                    newMember.setSourceId("UH core LDAP");
                                    updatedSubjectsList.add(newMember);
                                });

                        wsGetMembersResult.setWsSubjects(updatedSubjectsList.toArray(new WsSubject[0]));
                    }
                    return wsGetMembersResult;
                })
                .toArray(WsGetMembersResult[]::new);

        getMembersResults.getWsGetMembersResults().setResults(updatedWsGetMembers);
    }

    /* Updating Subject(s) */
    public void updateSubjectsByUhIdentifier(String uhIdentifier) {

        /* Valid Ootb Users List */
        WsSubject[] wsSubjects = getWsSubjectListOfOotbUsers();

        /* Skipping the validating without grouperClient */
        if (!isValidOotbUhIdentifier(uhIdentifier)) {
            WsSubject newSubject = new WsSubject();
            newSubject.setIdentifierLookup(uhIdentifier);
            newSubject.setId(uhIdentifier);
            newSubject.setResultCode("SUCCESS");
            newSubject.setName("OOTB CURRENT USER");
            WsSubject[] updatedSubjects = new WsSubject[] { newSubject };
            subjectsResults.getWsGetSubjectsResults().setWsSubjects(updatedSubjects);
            return;
        }

        /* Updating subjects for displaying the information of the user to be deleted, added
           when user clicked the add, remove button */
        WsSubject[] filteredWsSubjects = Arrays.stream(wsSubjects)
                .filter(wsSubject -> uhIdentifier.equals(wsSubject.getId()) || uhIdentifier.equals(
                        wsSubject.getIdentifierLookup()))
                .toArray(WsSubject[]::new);
        subjectsResults.getWsGetSubjectsResults().setWsSubjects(filteredWsSubjects);
    }

    public void updateSubjectsByUhIdentifiers(List<String> uhIdentifiers) {

        /* Valid Ootb Users List */
        WsSubject[] wsSubjects = getWsSubjectListOfOotbUsers();

        /* Skipping the validating without grouperClient */
        if (!isValidOotbUhIdentifier(uhIdentifiers)) {
            WsSubject[] updatedWsSubjects = uhIdentifiers.stream()
                    .map(uhIdentifier -> {
                        WsSubject wsSubject = new WsSubject();
                        wsSubject.setIdentifierLookup(uhIdentifier);
                        wsSubject.setId(uhIdentifier);
                        wsSubject.setResultCode("SUCCESS");
                        wsSubject.setName("OOTB CURRENT USER");
                        return wsSubject;
                    })
                    .toArray(WsSubject[]::new);
            subjectsResults.getWsGetSubjectsResults().setWsSubjects(updatedWsSubjects);
            return;
        }

        /* Updating subjects for displaying the information of the user to be deleted added,
           when user clicked the add, remove button */
        WsSubject[] filteredWsSubjects = Arrays.stream(wsSubjects)
                .filter(wsSubject -> uhIdentifiers.contains(wsSubject.getId()) || uhIdentifiers.contains(
                        wsSubject.getIdentifierLookup()))
                .toArray(WsSubject[]::new);
        subjectsResults.getWsGetSubjectsResults().setWsSubjects(filteredWsSubjects);
    }



    /* Util Function */

    public WsGetMembersResult[] wsGetMembersResultsList() {
        WsGetMembersResults wsGetMembersResults = getMembersResults().getWsGetMembersResults();
        WsGetMembersResult[] wsGetMembers = wsGetMembersResults.getResults();
        return wsGetMembers;
    }

    public GetMembersResult getMembersByGroupPath(String groupPath) {
        Optional<GetMembersResult> matchingResult = getMembersResults.getMembersResults().stream()
                .filter(result -> groupPath.equals(result.getGroup().getGroupPath()))
                .findFirst();
        return matchingResult.orElse(null);
    }

    public WsSubject[] getWsSubjectListOfOotbUsers() {
        GetMembersResult getMembersResult = getMembersByGroupPath(OOTB_USERS);
        WsSubject[] wsSubjects = getMembersResult.getWsGetMembersResult().getWsSubjects();
        return wsSubjects;
    }

    public Boolean isValidOotbUhIdentifier(String uhIdentifier) {
        GetMembersResult getMembersResult = getMembersByGroupPath(OOTB_USERS);
        return Arrays.stream(getMembersResult.getWsGetMembersResult().getWsSubjects())
                .anyMatch(subject -> uhIdentifier.equals(subject.getId()) || uhIdentifier.equals(
                        subject.getIdentifierLookup()));
    }

    public Boolean isValidOotbUhIdentifier(List<String> uhIdentifiers) {
        GetMembersResult getMembersResult = getMembersByGroupPath(OOTB_USERS);
        return Arrays.stream(getMembersResult.getWsGetMembersResult().getWsSubjects())
                .anyMatch(subject -> uhIdentifiers.contains(subject.getId()) || uhIdentifiers.contains(
                        subject.getIdentifierLookup()));
    }

}

