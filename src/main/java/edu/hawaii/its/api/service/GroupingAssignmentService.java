package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.wrapper.AttributeAssignmentsResults;
import edu.hawaii.its.api.wrapper.GroupsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private MemberAttributeService memberAttributeService;

    @Autowired
    private GroupAttributeService groupAttributeService;

    /**
     * Fetch a grouping from Grouper or the database.
     */
    public Grouping getGrouping(String groupingPath, String ownerUsername) {
        logger.info("getGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + ";");

        Grouping compositeGrouping;

        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) &&
                !memberAttributeService.isAdmin(ownerUsername)) {
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

        compositeGrouping.setDescription(grouperApiService.descriptionOf(groupingPath));
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
        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
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

        compositeGrouping.setDescription(grouperApiService.descriptionOf(groupingPath));
        compositeGrouping.setBasis(groups.get(basis));
        compositeGrouping.setExclude(groups.get(exclude));
        compositeGrouping.setInclude(groups.get(include));
        compositeGrouping.setComposite(groups.get(groupingPath));
        compositeGrouping.setOwners(groups.get(owners));

        return compositeGrouping;
    }

    //returns an adminLists object containing the list of all admins and all groupings
    public AdminListsHolder adminLists(String adminUsername) {
        if (!memberAttributeService.isAdmin(adminUsername)) {
            throw new AccessDeniedException();
        }
        AdminListsHolder adminListsHolder = new AdminListsHolder();
        List<String> groupingPathStrings = allGroupingsPaths();

        List<String> adminGrouping = Arrays.asList(GROUPING_ADMINS);
        Group admin = getMembers(adminUsername, adminGrouping).get(GROUPING_ADMINS);
        adminListsHolder.setAllGroupingPaths(makePaths(groupingPathStrings));
        adminListsHolder.setAdminGroup(admin);
        return adminListsHolder;
    }

    //returns a group from grouper or the database
    public Map<String, Group> getMembers(String ownerUsername, List<String> groupPaths) {
        logger.info("getMembers; user: " + ownerUsername + "; groups: " + groupPaths + ";");

        WsSubjectLookup lookup = grouperApiService.subjectLookup(ownerUsername);
        WsGetMembersResults members = grouperApiService.membersResults(
                SUBJECT_ATTRIBUTE_NAME_UID,
                lookup,
                groupPaths,
                null,
                null,
                null,
                null);

        Map<String, Group> groupMembers = new HashMap<>();
        if (members.getResults() != null) {
            groupMembers = makeGroups(members);
        }

        return groupMembers;
    }

    public Map<String, Group> getPaginatedMembers(String ownerUsername, List<String> groupPaths, Integer page,
            Integer size,
            String sortString, Boolean isAscending) {
        logger.info("getPaginatedMembers; ownerUsername: " + ownerUsername + "; groups: " + groupPaths +
                "; page: " + page + "; size: " + size + "; sortString: " + sortString + "; isAscending: " + isAscending
                + ";");

        WsSubjectLookup lookup = grouperApiService.subjectLookup(ownerUsername);
        WsGetMembersResults members = grouperApiService.membersResults(
                SUBJECT_ATTRIBUTE_NAME_UID,
                lookup,
                groupPaths,
                page,
                size,
                sortString,
                isAscending);

        Map<String, Group> groupMembers = new HashMap<>();
        if (members.getResults() != null) {
            groupMembers = makeGroups(members);
        }

        return groupMembers;
    }

    // Sets the attributes of a grouping in grouper or the database to match the attributes of the supplied grouping.
    public Grouping setGroupingAttributes(Grouping grouping) {
        logger.info("setGroupingAttributes; grouping: " + grouping + ";");

        AttributeAssignmentsResults attributeAssignmentsResults = new AttributeAssignmentsResults(
                grouperApiService.groupAttributeDefNames(ASSIGN_TYPE_GROUP, grouping.getPath()));
        grouping.setOptInOn(attributeAssignmentsResults.isOptInOn());
        grouping.setOptOutOn(attributeAssignmentsResults.isOptOutOn());

        // Set the sync destinations.
        List<SyncDestination> syncDestinations = groupAttributeService.getSyncDestinations(grouping);
        grouping.setSyncDestinations(syncDestinations);

        return grouping;
    }

    /**
     * Return the list of groups that the user is in, searching by username or uhUuid.
     */
    public List<String> getGroupPaths(String ownerUsername, String uhIdentifier) {
        logger.info("getGroupPaths; uhIdentifier: " + uhIdentifier + ";");

        if (!ownerUsername.equals(uhIdentifier) && !memberAttributeService.isAdmin(ownerUsername)) {
            return new ArrayList<>();
        }
        GroupsResults groupsResults = new GroupsResults(grouperApiService.groupsResults(uhIdentifier));
        return groupsResults.groupPaths();
    }

    /**
     * Return the list of groups that the user is in, searching by username or uhUuid
     * and filtered by a given predicate (can be found in PathFilter)
     */
    public List<String> getGroupPaths(String ownerUsername, String uhIdentifier, Predicate<String> predicate) {
        logger.info("getGroupPaths; uhIdentifier: " + uhIdentifier + ";" + "predicate: " + predicate + ";");

        if (!ownerUsername.equals(uhIdentifier) && !memberAttributeService.isAdmin(ownerUsername)) {
            return new ArrayList<>();
        }
        GroupsResults groupsResults = new GroupsResults(grouperApiService.groupsResults(uhIdentifier));
        List<String> groupPaths =  groupsResults.groupPaths();
        return groupPaths.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    public List<String> optOutGroupingsPaths(String owner, String optOutUid) {
        logger.info("optOutGroupingsPaths; owner: " + owner + "; optOutUid: " + optOutUid + ";");

        List<String> includes = getGroupPaths(owner, optOutUid, pathHasInclude());
        includes = includes.stream().map(path -> parentGroupingPath(path)).collect(Collectors.toList());
        List<String> optOutPaths = optableGroupings(OptType.OUT.value());
        optOutPaths.retainAll(includes);
        return new ArrayList<>(new HashSet<>(optOutPaths));
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    public List<GroupingPath> optInGroupingPaths(String owner, String optInUid) {
        logger.info("optInGroupingsPaths; owner: " + owner + "; optInUid: " + optInUid + ";");

        List<GroupingPath> optInGroupingPaths = new ArrayList<>();
        List<String> includes = getGroupPaths(owner, optInUid, pathHasInclude());
        includes = includes.stream().map(path -> parentGroupingPath(path)).collect(Collectors.toList());

        List<String> optInPaths = optableGroupings(OptType.IN.value());
        optInPaths.removeAll(includes);
        optInPaths = new ArrayList<>(new HashSet<>(optInPaths));

        optInGroupingPaths = optInPaths.parallelStream().map(path -> new GroupingPath(path,
                grouperApiService.descriptionOf(path))).collect(Collectors.toList());

        return optInGroupingPaths;
    }

    /**
     * List grouping paths than can be opted into or out of.
     */
    public List<String> optableGroupings(String optAttr) {
        if (!optAttr.equals(OptType.IN.value()) && !optAttr.equals(OptType.OUT.value())) {
            throw new AccessDeniedException();
        }
        AttributeAssignmentsResults attributeAssignmentsResults =
                new AttributeAssignmentsResults(grouperApiService.groupsOf(ASSIGN_TYPE_GROUP, optAttr));
        return attributeAssignmentsResults.getOwnerGroupNames();
    }

    /**
     * Helper - adminLists
     */
    public List<String> allGroupingsPaths() {
        AttributeAssignmentsResults attributeAssignmentsResults =
                new AttributeAssignmentsResults(grouperApiService.groupsOf(ASSIGN_TYPE_GROUP, TRIO));
        return attributeAssignmentsResults.getGroupNames();
    }

    /**
     * Remove one of the words (:exclude, :include, :owners ...) from the end of the string.
     */
    public String parentGroupingPath(String group) {
        if (group != null) {
            if (group.endsWith(GroupType.EXCLUDE.value())) {
                return group.substring(0, group.length() - GroupType.EXCLUDE.value().length());
            } else if (group.endsWith(GroupType.INCLUDE.value())) {
                return group.substring(0, group.length() - GroupType.INCLUDE.value().length());
            } else if (group.endsWith(GroupType.OWNERS.value())) {
                return group.substring(0, group.length() - GroupType.OWNERS.value().length());
            } else if (group.endsWith(GroupType.BASIS.value())) {
                return group.substring(0, group.length() - GroupType.BASIS.value().length());
            }
            return group;
        }
        return "";
    }

    /**
     * Helper - membershipResults
     * Get the name of a grouping from groupPath.
     */
    public String nameGroupingPath(String groupPath) {
        String parentPath = parentGroupingPath(groupPath);
        if ("".equals(parentPath)) {
            return "";
        }
        return parentPath.substring(parentPath.lastIndexOf(":") + 1, parentPath.length());
    }

    /**
     * Helper - isSoleOwner
     */
    public List<String> getGroupingOwners(String currentUser, String groupPath) {
        List<String> owners = new ArrayList<>();
        List<String> path = new ArrayList<>();
        path.add(groupPath + GroupType.OWNERS.value());
        WsSubjectLookup lookup = grouperApiService.subjectLookup(currentUser);
        WsGetMembersResults wsGetMembersResults = grouperApiService.membersResults(
                SUBJECT_ATTRIBUTE_NAME_UID,
                lookup,
                path);

        List<WsSubject> subjects = Arrays.asList(wsGetMembersResults.getResults()[0].getWsSubjects());
        subjects.forEach(subject -> {
            String ownerUid = subject.getAttributeValue(1);
            //TODO Remove the if statement after old/outdated UH Grouper users have been pruned
            if (!ownerUid.isEmpty()) {
                owners.add(ownerUid);
            }
        });

        return owners;
    }

    public Boolean isSoleOwner(String currentUser, String groupPath, String uidToCheck) {
        List<String> ownersInGrouping = getGroupingOwners(currentUser, groupPath);
        if (ownersInGrouping.size() >= 2) {
            return false;
        }
        return ownersInGrouping.contains(uidToCheck);
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

    /**
     * Helper - adminLists
     * Take a list of grouping path strings and return a list of GroupingPath objects.
     */
    public List<GroupingPath> makePaths(List<String> groupingPaths) {
        if (groupingPaths == null || groupingPaths.isEmpty()) {
            return Collections.emptyList();
        }

        return groupingPaths.parallelStream()
                .map(path -> new GroupingPath(path,
                        grouperApiService.descriptionOf(path)))
                .collect(Collectors.toList());
    }
}
