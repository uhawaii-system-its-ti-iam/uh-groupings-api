package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingsGroupMember;
import edu.hawaii.its.api.groupings.GroupingsGroupMembers;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.Subject;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

@Service("groupingAssignmentService")
public class GroupingAssignmentService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.subject_attribute_name_uhuuid}")
    private String SUBJECT_ATTRIBUTE_NAME_UID;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.stale_subject_id}")
    private String STALE_SUBJECT_ID;

    public static final Log logger = LogFactory.getLog(GroupingAssignmentService.class);

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsService groupingsService;

    /**
     * Fetch a grouping from Grouper or the database.
     */
    public Grouping getGrouping(String groupingPath, String ownerUsername) {
        logger.info("getGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + ";");

        Grouping compositeGrouping;

        if (!memberService.isOwner(groupingPath, ownerUsername) && !memberService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException();
        }
        compositeGrouping = new Grouping(groupingPath);

        String basis = groupingPath + GroupType.BASIS.value();
        String include = groupingPath + GroupType.INCLUDE.value();
        String exclude = groupingPath + GroupType.EXCLUDE.value();
        String owners = groupingPath + GroupType.OWNERS.value();

        String[] paths = { include,
                exclude,
                basis,
                groupingPath,
                owners };
        Map<String, Group> groups = getMembers(ownerUsername, Arrays.asList(paths));

        compositeGrouping = setGroupingAttributes(compositeGrouping);

        compositeGrouping.setDescription(groupingsService.getGroupingDescription(groupingPath));
        compositeGrouping.setBasis(groups.get(basis));
        compositeGrouping.setExclude(groups.get(exclude));
        compositeGrouping.setInclude(groups.get(include));
        compositeGrouping.setComposite(groups.get(groupingPath));
        compositeGrouping.setOwners(groups.get(owners));

        return compositeGrouping;
    }

    /**
     * Fetch a grouping from Grouper Database, but paginated based on given page + size sortString sorts the database
     * by whichever sortString category is given (e.g. "uid" will sort list by uid) before returning page
     * isAscending puts the database in ascending or descending order before returning page.
     */
    public Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size,
            String sortString, Boolean isAscending) {
        logger.info(
                "getPaginatedGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + "; page: " + page
                        + "; size: " + size + "; sortString: " + sortString + "; isAscending: " + isAscending + ";");
        if (!memberService.isOwner(groupingPath, ownerUsername) && !memberService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException();
        }
        Grouping compositeGrouping = new Grouping(groupingPath);
        String basis = groupingPath + GroupType.BASIS.value();
        String include = groupingPath + GroupType.INCLUDE.value();
        String exclude = groupingPath + GroupType.EXCLUDE.value();
        String owners = groupingPath + GroupType.OWNERS.value();

        List<String> paths = new ArrayList<>();
        paths.add(include);
        paths.add(exclude);
        paths.add(basis);
        paths.add(groupingPath);
        paths.add(owners);
        Map<String, Group> groups = getPaginatedMembers(ownerUsername, paths, page, size, sortString, isAscending);
        compositeGrouping = setGroupingAttributes(compositeGrouping);

        compositeGrouping.setDescription(groupingsService.getGroupingDescription(groupingPath));
        compositeGrouping.setBasis(groups.get(basis));
        compositeGrouping.setExclude(groups.get(exclude));
        compositeGrouping.setInclude(groups.get(include));
        compositeGrouping.setComposite(groups.get(groupingPath));
        compositeGrouping.setOwners(groups.get(owners));
        compositeGrouping.setIsEmpty();

        return compositeGrouping;
    }

    //returns an adminLists object containing the list of all admins and all groupings
    public AdminListsHolder adminLists(String adminUsername) {
        if (!memberService.isAdmin(adminUsername)) {
            throw new AccessDeniedException();
        }
        AdminListsHolder adminListsHolder = new AdminListsHolder();

        List<String> adminGrouping = Arrays.asList(GROUPING_ADMINS);
        Group admin = getMembers(adminUsername, adminGrouping).get(GROUPING_ADMINS);
        adminListsHolder.setAllGroupingPaths(groupingsService.allGroupingPaths());
        adminListsHolder.setAdminGroup(admin);
        return adminListsHolder;
    }

    //returns a group from grouper or the database
    public Map<String, Group> getMembers(String ownerUsername, List<String> groupPaths) {
        GetMembersResults getMembersResults =
                grouperApiService.getMembersResults(
                        ownerUsername,
                        groupPaths,
                        null,
                        null,
                        null,
                        false);
        return makeGroups(getMembersResults);
    }

    public Map<String, Group> getPaginatedMembers(String ownerUsername, List<String> groupPaths, Integer page,
            Integer size,
            String sortString, Boolean isAscending) {
        GetMembersResults getMembersResults = grouperApiService.getMembersResults(
                ownerUsername,
                groupPaths,
                page,
                size,
                sortString,
                isAscending);
        return makeGroups(getMembersResults);
    }

    // Sets the attributes of a grouping in grouper or the database to match the attributes of the supplied grouping.
    public Grouping setGroupingAttributes(Grouping grouping) {
        logger.info("setGroupingAttributes; grouping: " + grouping + ";");
        GroupAttributeResults groupAttributeResults = grouperApiService.groupAttributeResult(grouping.getPath());
        grouping.setOptInOn(groupAttributeResults.isOptInOn());
        grouping.setOptOutOn(groupAttributeResults.isOptOutOn());

        // Set the sync destinations.
        List<SyncDestination> syncDestinations = groupAttributeService.getSyncDestinations(grouping);
        grouping.setSyncDestinations(syncDestinations);

        return grouping;
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    public List<String> optOutGroupingsPaths(String owner, String optOutUid) {
        logger.info("optOutGroupingsPaths; owner: " + owner + "; optOutUid: " + optOutUid + ";");

        List<String> includes = groupingsService.groupPaths(optOutUid, pathHasInclude());
        includes = includes.stream().map(path -> parentGroupingPath(path)).collect(Collectors.toList());
        List<String> optOutPaths = groupingsService.optOutEnabledGroupingPaths();
        optOutPaths.retainAll(includes);
        return new ArrayList<>(new HashSet<>(optOutPaths));
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    public List<GroupingPath> optInGroupingPaths(String owner, String optInUid) {
        logger.info("optInGroupingsPaths; owner: " + owner + "; optInUid: " + optInUid + ";");

        List<String> includes = groupingsService.groupPaths(optInUid, pathHasInclude());
        includes = includes.stream().map(path -> parentGroupingPath(path)).collect(Collectors.toList());

        List<String> optInPaths = groupingsService.optInEnabledGroupingPaths();
        optInPaths.removeAll(includes);
        optInPaths = new ArrayList<>(new HashSet<>(optInPaths));

        return groupingsService.getGroupingPaths(optInPaths);
    }

    public GroupingsGroupMembers groupingOwners(String currentUser, String groupingPath) {
        return new GroupingsGroupMembers(
                grouperApiService.getMembersResult(groupingPath + GroupType.OWNERS.value()));
    }

    public Boolean isSoleOwner(String currentUser, String groupPath, String uidToCheck) {
        List<GroupingsGroupMember> owners = groupingOwners(currentUser, groupPath).getGroupMembers();
        if (owners.size() >= 2) {
            return false;
        }
        return owners.stream().anyMatch(owner -> owner.getUid().contains(uidToCheck));
    }

    public Map<String, Group> makeGroups(GetMembersResults getMembersResults) {
        Map<String, Group> groupMembers = new HashMap<>();
        List<GetMembersResult> membersResults = getMembersResults.getMembersResults();
        for (GetMembersResult membersResult : membersResults) {
            Group group = new Group(membersResult.getGroup().getGroupPath());
            List<Subject> subjects = membersResult.getSubjects();
            for (Subject subject : subjects) {
                if (!subject.hasUHAttributes()) {
                    continue;
                }
                Person person = new Person(subject);
                if (group.getPath().endsWith(GroupType.BASIS.value()) && subject.getSourceId() != null
                        && subject.getSourceId()
                        .equals(STALE_SUBJECT_ID)) {
                    person.setUsername("User Not Available.");
                }
                group.addMember(new Person(subject));
            }
            groupMembers.put(group.getPath(), group);
        }
        return groupMembers;
    }

    /**
     * Makes a group filled with members from membersResults.
     */
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
                    if (group.getPath().endsWith(GroupType.BASIS.value()) && subject.getSourceId() != null
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

    /**
     * Helper - makeGroups
     * Makes a person with all attributes in attributeNames.
     */
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
}
