package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.AttributeAssignmentsResults;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("groupingAssignmentService")
public class GroupingAssignmentServiceImpl implements GroupingAssignmentService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.subject_attribute_name_uhuuid}")
    private String SUBJECT_ATTRIBUTE_NAME_UID;

    @Value("${groupings.api.stem}")
    private String STEM;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    public static final Log logger = LogFactory.getLog(GroupingAssignmentServiceImpl.class);

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired GroupAttributeService groupAttributeService;

    /**
     * Fetch a grouping from Grouper or the database.
     */
    @Override
    public Grouping getGrouping(String groupingPath, String ownerUsername) {
        logger.info("getGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + ";");

        Grouping compositeGrouping;

        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) &&
                !memberAttributeService.isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        compositeGrouping = new Grouping(groupingPath);

        String basis = groupingPath + BASIS;
        String include = groupingPath + INCLUDE;
        String exclude = groupingPath + EXCLUDE;
        String owners = groupingPath + OWNERS;

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
    @Override
    public Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size,
            String sortString, Boolean isAscending) {
        logger.info(
                "getPaginatedGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + "; page: " + page
                        + "; size: " + size + "; sortString: " + sortString + "; isAscending: " + isAscending + ";");
        if (!memberAttributeService.isOwner(groupingPath, ownerUsername) && !memberAttributeService.isAdmin(
                ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        Grouping compositeGrouping = new Grouping(groupingPath);
        String basis = groupingPath + BASIS;
        String include = groupingPath + INCLUDE;
        String exclude = groupingPath + EXCLUDE;
        String owners = groupingPath + OWNERS;

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
    @Override
    public AdminListsHolder adminLists(String adminUsername) {
        if (!memberAttributeService.isAdmin(adminUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        AdminListsHolder adminListsHolder = new AdminListsHolder();
        List<String> groupingPathStrings = allGroupingsPaths();

        List<String> adminGrouping = Arrays.asList(GROUPING_ADMINS);
        Group admin = getMembers(adminUsername, adminGrouping).get(GROUPING_ADMINS);
        adminListsHolder.setAllGroupingPaths(helperService.makePaths(groupingPathStrings));
        adminListsHolder.setAdminGroup(admin);
        return adminListsHolder;
    }

    //returns a group from grouper or the database
    @Override
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
            groupMembers = helperService.makeGroups(members);
        }
        return groupMembers;
    }

    @Override
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
            groupMembers = helperService.makeGroups(members);
        }

        return groupMembers;
    }

    // Sets the attributes of a grouping in grouper or the database to match the attributes of the supplied grouping.
    @Override
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

    // Returns the list of groups that the user is in, searching by username or uhUuid.
    @Override
    public List<String> getGroupPaths(String ownerUsername, String username) {
        logger.info("getGroupPaths; username: " + username + ";");

        if (!ownerUsername.equals(username) && !memberAttributeService.isAdmin(ownerUsername)) {
            return new ArrayList<>();
        }
        List<WsGroup> groups =
                Arrays.asList(grouperApiService.groupsResults(username, grouperApiService.stemLookup(STEM),
                        StemScope.ALL_IN_SUBTREE).getResults()[0].getWsGroups());

        return helperService.extractGroupPaths(groups);
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    @Override
    public List<String> optOutGroupingsPaths(String owner, String optOutUid) {
        logger.info("optOutGroupingsPaths; owner: " + owner + "; optOutUid: " + optOutUid + ";");

        List<String> groupingsIn = getGroupPaths(owner, optOutUid);
        List<String> includes =
                groupingsIn.stream().filter(path -> path.endsWith(INCLUDE)).collect(Collectors.toList());
        includes = includes.stream().map(path -> helperService.parentGroupingPath(path)).collect(Collectors.toList());
        List<String> optOutPaths = optableGroupings(OPT_OUT);
        optOutPaths.retainAll(includes);
        return new ArrayList<>(new HashSet<>(optOutPaths));
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    @Override
    public List<GroupingPath> optInGroupingPaths(String owner, String optInUid) {
        logger.info("optInGroupingsPaths; owner: " + owner + "; optInUid: " + optInUid + ";");

        List<GroupingPath> optInGroupingPaths = new ArrayList<>();
        List<String> groupingsIn = getGroupPaths(owner, optInUid);
        List<String> includes =
                groupingsIn.stream().filter(path -> path.endsWith(INCLUDE)).collect(Collectors.toList());
        List<String> excludes =
                groupingsIn.stream().filter(path -> path.endsWith(EXCLUDE)).collect(Collectors.toList());
        includes = includes.stream().map(path -> helperService.parentGroupingPath(path)).collect(Collectors.toList());
        excludes = excludes.stream().map(path -> helperService.parentGroupingPath(path)).collect(Collectors.toList());

        List<String> optInPaths = optableGroupings(OPT_IN);
        optInPaths.removeAll(includes);
        optInPaths.addAll(excludes);
        optInPaths = new ArrayList<>(new HashSet<>(optInPaths));
        optInPaths.forEach(path -> {
            optInGroupingPaths.add(new GroupingPath(path));
        });
        return optInGroupingPaths;
    }

    /**
     * List grouping paths than can be opted into or out of.
     */
    @Override
    public List<String> optableGroupings(String optAttr) {
        if (!optAttr.equals(OPT_IN) && !optAttr.equals(OPT_OUT)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        AttributeAssignmentsResults attributeAssignmentsResults =
                new AttributeAssignmentsResults(grouperApiService.groupsOf(ASSIGN_TYPE_GROUP, optAttr));
        return attributeAssignmentsResults.getOwnerGroupNames();
    }

    @Override
    public List<String> allGroupingsPaths() {
        AttributeAssignmentsResults attributeAssignmentsResults =
                new AttributeAssignmentsResults(grouperApiService.groupsOf(ASSIGN_TYPE_GROUP, TRIO));
        return attributeAssignmentsResults.getGroupNames();
    }

    @Override
    public List<String> getGroupingOwners(String currentUser, String groupPath) {
        List<String> owners = new ArrayList<>();
        List<String> path = new ArrayList<>();
        path.add(groupPath + OWNERS);
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

    @Override
    public Boolean isSoleOwner(String currentUser, String groupPath, String uidToCheck) {
        List<String> ownersInGrouping = getGroupingOwners(currentUser, groupPath);
        if (ownersInGrouping.size() >= 2) {
            return false;
        }
        return ownersInGrouping.contains(uidToCheck);
    }
}
