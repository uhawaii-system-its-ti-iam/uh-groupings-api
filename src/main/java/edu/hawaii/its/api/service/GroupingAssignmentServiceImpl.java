package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingAssignment;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service("groupingAssignmentService")
public class GroupingAssignmentServiceImpl implements GroupingAssignmentService {

    @Value("${groupings.api.settings}")
    private String SETTINGS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.grouping_owners}")
    private String GROUPING_OWNERS;

    @Value("${groupings.api.grouping_superusers}")
    private String GROUPING_SUPERUSERS;

    @Value("${groupings.api.attributes}")
    private String ATTRIBUTES;

    @Value("${groupings.api.for_groups}")
    private String FOR_GROUPS;

    @Value("${groupings.api.for_memberships}")
    private String FOR_MEMBERSHIPS;

    @Value("${groupings.api.last_modified}")
    private String LAST_MODIFIED;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.uhgrouping}")
    private String UHGROUPING;

    @Value("${groupings.api.destinations}")
    private String DESTINATIONS;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.purge_grouping}")
    private String PURGE_GROUPING;

    @Value("${groupings.api.self_opted}")
    private String SELF_OPTED;

    @Value("${groupings.api.anyone_can}")
    private String ANYONE_CAN;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

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

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.assign_type_immediate_membership}")
    private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;

    @Value("${groupings.api.subject_attribute_name_uuid}")
    private String SUBJECT_ATTRIBUTE_NAME_UID;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Value("${groupings.api.privilege_opt_out}")
    private String PRIVILEGE_OPT_OUT;

    @Value("${groupings.api.privilege_opt_in}")
    private String PRIVILEGE_OPT_IN;

    @Value("${groupings.api.every_entity}")
    private String EVERY_ENTITY;

    @Value("${groupings.api.is_member}")
    private String IS_MEMBER;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.success_allowed}")
    private String SUCCESS_ALLOWED;

    @Value("${groupings.api.stem}")
    private String STEM;

    @Value("${groupings.api.person_attributes.uuid}")
    private String UUID;

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    public static final Log logger = LogFactory.getLog(GroupingAssignmentServiceImpl.class);

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    // returns a list of all of the groups in groupPaths that are also groupings
    @Override
    public List<Grouping> groupingsIn(List<String> groupPaths) {
        List<String> groupingsIn = helperService.extractGroupings(groupPaths);
        List<Grouping> groupings = helperService.makeGroupings(groupingsIn);

        // todo this may be able to be optimized by getting attributes from grouper when getting the group list
        // rather than making individual calls to grouper. Testing will need to be done to see if the will be faster in
        // the majority of cases or only for edge cases
        groupings.forEach(this::setGroupingAttributes);

        return groupings;
    }

    //returns a list of groupings that corresponds to all of the owner groups in groupPaths
    @Override
    public List<Grouping> groupingsOwned(List<String> groupPaths) {
        List<String> ownerGroups = groupPaths
                .stream()
                .filter(groupPath -> groupPath.endsWith(OWNERS))
                .map(groupPath -> groupPath.substring(0, groupPath.length() - OWNERS.length()))
                .collect(Collectors.toList());

        // make sure the owner group actually correspond to a grouping
        List<String> ownedGroupings = helperService.extractGroupings(ownerGroups);

        return helperService.makeGroupings(ownedGroupings);
    }

    //returns a list of all of the groupings corresponding to the include groups in groupPaths that have the self-opted attribute
    //set in the membership
    @Override
    public List<Grouping> groupingsOptedInto(String username, List<String> groupPaths) {
        return groupingsOpted(INCLUDE, username, groupPaths);
    }

    //returns a list of all of the groupings corresponding to the exclude groups in groupPaths that have the self-opted attribute
    //set in the membership
    @Override
    public List<Grouping> groupingsOptedOutOf(String username, List<String> groupPaths) {
        return groupingsOpted(EXCLUDE, username, groupPaths);
    }

    //todo This might be obsolete with getGroup methods, but refactoring may take time
    //todo Change getMembers to getGroupMembers with appropriate exception handlers
    //fetch a grouping from Grouper or the database
    @Override
    public Grouping getGrouping(String groupingPath, String ownerUsername) {
        logger.info("getGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + ";");

        Grouping compositeGrouping = new Grouping();

        if (memberAttributeService.isOwner(groupingPath, ownerUsername) || memberAttributeService
                .isAdmin(ownerUsername)) {
            compositeGrouping = new Grouping(groupingPath);

            Group include = getMembers(ownerUsername, groupingPath + INCLUDE);
            Group exclude = getMembers(ownerUsername, groupingPath + EXCLUDE);
            Group basis = getMembers(ownerUsername, groupingPath + BASIS);
            Group composite = getMembers(ownerUsername, groupingPath);
            Group owners = getMembers(ownerUsername, groupingPath + OWNERS);

            compositeGrouping = setGroupingAttributes(compositeGrouping);

            compositeGrouping.setBasis(basis);
            compositeGrouping.setExclude(exclude);
            compositeGrouping.setInclude(include);
            compositeGrouping.setComposite(composite);
            compositeGrouping.setOwners(owners);

        }
        return compositeGrouping;
    }

    // todo May not need anymore; Pagination will just grab a singular page and allow users to see the stale subjects
    // Fetch a grouping from Grouper of database, but paginated based on given page + size
    @Override
    public Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size) {
        logger.info("getPaginatedGrouping; grouping: " + groupingPath + "; username: " + ownerUsername +
                "; page: " + page + "; size: " + size + "'");

        Grouping compositeGrouping = new Grouping();

        if (memberAttributeService.isOwner(groupingPath, ownerUsername) || memberAttributeService
                .isAdmin(ownerUsername)) {

            // Paginating the basis will remove garbage data, leaving it smaller than the requested size
            // Therefore we need to fill the rest of the current page with more data from another page
            // This is dealing with stale subjects
            // At some point, when this issue is resolved this functionality may not be necessary
            // todo Refactor this as a general case for all Groups and not just Basis
            // todo Possibly refactor to avoid while loops and sanitize input relating to negative page/size values
            // todo Remove workaround for stale subjects, return as is with "User is unavailable" or something similar

            compositeGrouping = getPaginatedGroupingHelper(ownerUsername, groupingPath, page, size);

            // Get base grouping from pagination and isolate basis
            //            compositeGrouping = getPaginatedGroupingHelper(ownerUsername, groupingPath, page, size);
            //            Group basis = compositeGrouping.getBasis();
            //            List<Person> basisList = basis.getMembers();
            //
            //            int i = 1;
            //            while(basisList.size() < size) {
            //
            //                Group basisToAdd = getPaginatedMembers(ownerUsername,groupingPath + BASIS, page + i, size);
            //                List<Person> basisToAddList = basisToAdd.getMembers();
            //
            //                // If the next page is empty, we can assume we are at the end of the group
            //                if(basisToAddList.size() == 0) break;
            //
            //                // Add as much as we need from the next page to the current page
            //                // If it's not enough, repeat with the page after that
            //                List<Person> subBasisToAddList = basisToAddList.subList(0, size - basis.getMembers().size());
            //                basisList.addAll(subBasisToAddList);
            //                i++;
            //            }
            //
            //            basis.setMembers(basisList);
            //            compositeGrouping.setBasis(basis);
        }
        return compositeGrouping;
    }

    // todo Default pagination method until everything is refactored
    @Override
    public Grouping getPaginatedGroupingHelper(String ownerUsername, String groupingPath, Integer page, Integer size) {
        logger.info("getPaginatedGroupingHelper; grouping: " + groupingPath +
                "; page: " + page + "; size: " + size + "'");

        Grouping compositeGrouping = new Grouping();

        compositeGrouping = new Grouping(groupingPath);

        Group include = getPaginatedMembers(ownerUsername, groupingPath + INCLUDE, page, size);
        Group exclude = getPaginatedMembers(ownerUsername, groupingPath + EXCLUDE, page, size);
        Group basis = getPaginatedMembers(ownerUsername, groupingPath + BASIS, page, size);
        Group composite = getPaginatedMembers(ownerUsername, groupingPath, page, size);
        Group owners = getPaginatedMembers(ownerUsername, groupingPath + OWNERS, page, size);

        compositeGrouping = setGroupingAttributes(compositeGrouping);

        compositeGrouping.setBasis(basis);
        compositeGrouping.setExclude(exclude);
        compositeGrouping.setInclude(include);
        compositeGrouping.setComposite(composite);
        compositeGrouping.setOwners(owners);

        return compositeGrouping;
    }

    //get a GroupingAssignment object containing the groups that a user is in and can opt into
    @Override
    public GroupingAssignment getGroupingAssignment(String username) {
        GroupingAssignment groupingAssignment = new GroupingAssignment();
        List<String> groupPaths = getGroupPaths(username, username);

        groupingAssignment.setGroupingsIn(groupingsIn(groupPaths));
        groupingAssignment.setGroupingsOwned(groupingsOwned(groupPaths));
        groupingAssignment.setGroupingsToOptInTo(groupingsToOptInto(username, groupPaths));
        groupingAssignment.setGroupingsToOptOutOf(groupingsToOptOutOf(username, groupPaths));
        groupingAssignment.setGroupingsOptedOutOf(groupingsOptedOutOf(username, groupPaths));
        groupingAssignment.setGroupingsOptedInTo(groupingsOptedInto(username, groupPaths));

        return groupingAssignment;
    }

    //returns an adminLists object containing the list of all admins and all groupings
    @Override
    public AdminListsHolder adminLists(String adminUsername) {
        AdminListsHolder info = new AdminListsHolder();
        List<Grouping> groupings;

        if (memberAttributeService.isSuperuser(adminUsername)) {

            WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                    grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(
                            ASSIGN_TYPE_GROUP,
                            TRIO);

            List<WsGroup> groups = new ArrayList<>(Arrays.asList(attributeAssignmentsResults.getWsGroups()));

            List<String> groupPaths = groups.stream().map(WsGroup::getName).collect(Collectors.toList());

            Group admin = getMembers(adminUsername, GROUPING_ADMINS);
            groupings = helperService.makeGroupings(groupPaths);
            info.setAdminGroup(admin);
            info.setAllGroupings(groupings);
        }
        return info;
    }

    //returns a list of groupings corresponding to the include group or exclude group (includeOrrExclude) in groupPaths that
    //have the self-opted attribute set in the membership
    public List<Grouping> groupingsOpted(String includeOrrExclude, String username, List<String> groupPaths) {
        logger.info("groupingsOpted; includeOrrExclude: " + includeOrrExclude + "; username: " + username + ";");

        List<String> groupingsOpted = new ArrayList<>();

        // todo get the self opted memberships in one grouper call
        List<String> groupsOpted = groupPaths.stream().filter(group -> group.endsWith(includeOrrExclude)
                && memberAttributeService.isSelfOpted(group, username)).map(helperService::parentGroupingPath)
                .collect(Collectors.toList());

        if (groupsOpted.size() > 0) {

            List<WsGetAttributeAssignmentsResults> attributeAssignmentsResults =
                    grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(
                            ASSIGN_TYPE_GROUP,
                            TRIO,
                            groupsOpted);

            List<WsGroup> triosList = new ArrayList<>();
            for (WsGetAttributeAssignmentsResults results : attributeAssignmentsResults) {
                triosList.addAll(Arrays.asList(results.getWsGroups()));
            }

            groupingsOpted.addAll(triosList.stream().map(WsGroup::getName).collect(Collectors.toList()));
        }
        return helperService.makeGroupings(groupingsOpted);
    }

    //returns a group from grouper or the database
    @Override
    public Group getMembers(String ownerUsername, String groupPath) {
        logger.info("getMembers; user: " + ownerUsername + "; group: " + groupPath + ";");

        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ownerUsername);
        WsGetMembersResults members = grouperFactoryService.makeWsGetMembersResults(
                SUBJECT_ATTRIBUTE_NAME_UID,
                lookup,
                groupPath);

        //todo should we use EmptyGroup?
        Group groupMembers = new Group();
        if (members.getResults() != null) {
            groupMembers = makeGroup(members);
        }
        return groupMembers;
    }

    @Override
    public Group getGroupMembers(String ownerUsername, String parentGroupingPath, String componentId) throws Exception {
        logger.info("getGroupMembers; user: " + ownerUsername + "; parentGroupingPath: " + parentGroupingPath +
                "; componentId: " + componentId + ";");

        String groupPath = parentGroupingPath + ":" + componentId;
        Group groupMembers = new Group();

        if (memberAttributeService.isOwner(parentGroupingPath, ownerUsername) || memberAttributeService
                .isAdmin(ownerUsername)) {

            WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ownerUsername);
            WsGetMembersResults members = new WsGetMembersResults();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<WsGetMembersResults> callable = new Callable<WsGetMembersResults>() {
                @Override
                public WsGetMembersResults call() {
                    return grouperFactoryService.makeWsGetMembersResults(SUBJECT_ATTRIBUTE_NAME_UID, lookup, groupPath);
                }
            };

            Future<WsGetMembersResults> future = executor.submit(callable);

            try {
                //todo Move to properties file
                members = future.get(4, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                te.printStackTrace();
                GroupingsHTTPException ghe = new GroupingsHTTPException();
                throw new GroupingsHTTPException("getGroupMembers Operation Timed Out.", ghe, 504);
            }

            if (executor.isTerminated()) {
                executor.shutdown();
            }

            //todo should we use EmptyGroup?
            if (members.getResults() != null) {
                if (componentId.equals(BASIS)) {
                    groupMembers = makeBasisGroup(members);
                } else {
                    groupMembers = makeGroup(members);
                }
            }
        }
        return groupMembers;
    }

    @Override
    public Group getPaginatedMembers(String ownerUsername, String groupPath, Integer page, Integer size) {
        logger.info("getMembers; group: " + groupPath +
                "; page: " + page + "; size: " + size + ";");

        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ownerUsername);
        WsGetMembersResults members = grouperFactoryService.makeWsGetMembersResultsPaginated(
                SUBJECT_ATTRIBUTE_NAME_UID,
                lookup,
                groupPath,
                page,
                size);

        //todo should we use EmptyGroup?
        Group groupMembers = new Group();
        if (members.getResults() != null && groupPath.contains(BASIS)) {
            groupMembers = makeBasisGroup(members);
        } else if (members.getResults() != null) {
            groupMembers = makeGroup(members);
        }
        return groupMembers;
    }

    // todo Doesn't work
    @Override
    public Group getPaginatedAndFilteredMembers(
            String groupPath, String ownerUsername, String filterString, Integer page, Integer size) {
        logger.info("getMembers; group: " + groupPath + "; ownerUsername: " + ownerUsername +
                "; filterString: " + filterString + "; page: " + page + "; size: " + size + ";");

        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ownerUsername);
        WsGetMembershipsResults results = grouperFactoryService.makeWsGetMembersResultsFilteredAndPaginated(
                SUBJECT_ATTRIBUTE_NAME_UID,
                lookup,
                groupPath,
                filterString,
                page,
                size);

        Group groupMembers = new Group();
        //        if(results.getResults() != null) {
        //            groupMembers = makeGroup(members);
        //        }
        return groupMembers;

    }

    //makes a group filled with members from membersResults
    @Override
    public Group makeGroup(WsGetMembersResults membersResults) {
        Group group = new Group();
        try {
            WsSubject[] subjects = membersResults.getResults()[0].getWsSubjects();
            String[] attributeNames = membersResults.getSubjectAttributeNames();

            if (subjects.length > 0) {
                for (WsSubject subject : subjects) {
                    if (subject != null) {
                        group.addMember(makePerson(subject, attributeNames));
                    }
                }
            }
            // Return empty group if for any unforeseen results
        } catch (NullPointerException npe) {
            return new Group();
        }

        return group;
    }

    // todo Remove workaround for stale subjects, return as is with "User is unavailable" or something similar
    // Make group specifically for basis group only
    public Group makeBasisGroup(WsGetMembersResults membersResults) {
        Group group = new Group();
        try {
            WsSubject[] subjects = membersResults.getResults()[0].getWsSubjects();
            String[] attributeNames = membersResults.getSubjectAttributeNames();
            Person personToAdd = new Person();

            if (subjects.length > 0) {
                for (WsSubject subject : subjects) {
                    if (subject != null) {
                        personToAdd = makePerson(subject, attributeNames);
                        if (subject.getSourceId().equals("g:gsa")) {
                            personToAdd.setUsername("User not available.");
                        }
                        group.addMember(personToAdd);
                        //todo Removing fix; instead we will display these users with appropriate information
                        // Add null source id users (some valid users have null source id)
                        //                        if (subject.getSourceId() == null) {
                        //                            group.addMember(makePerson(subject, attributeNames));
                        //                            // Add user to basis if not in intermediate group
                        //                        } else if (!subject.getSourceId().equals("g:gsa")) {
                        //                            group.addMember(makePerson(subject, attributeNames));
                        //                        }
                    }
                }
            }
            // Return empty group if for any unforeseen results
        } catch (NullPointerException npe) {
            return new Group();
        }

        return group;
    }

    //makes a person with all attributes in attributeNames
    @Override
    public Person makePerson(WsSubject subject, String[] attributeNames) {
        if (subject == null || subject.getAttributeValues() == null) {
            return new Person();
        } else {

            Map<String, String> attributes = new HashMap<>();
            for (int i = 0; i < subject.getAttributeValues().length; i++) {
                attributes.put(attributeNames[i], subject.getAttributeValue(i));
            }
            //uuid is the only attribute not actually in the WsSubject attribute array
            attributes.put(UUID, subject.getId());

            return new Person(attributes);
        }
    }

    //sets the attributes of a grouping in grouper or the database to match the attributes of the supplied grouping
    public Grouping setGroupingAttributes(Grouping grouping) {
        logger.info("setGroupingAttributes; grouping: " + grouping + ";");
        boolean isListservOn = false;
        boolean isOptInOn = false;
        boolean isOptOutOn = false;
        boolean isReleasedGroupingOn = false;

        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsForGroup(
                        ASSIGN_TYPE_GROUP,
                        grouping.getPath());

        WsAttributeDefName[] attributeDefNames = wsGetAttributeAssignmentsResults.getWsAttributeDefNames();
        if (attributeDefNames != null && attributeDefNames.length > 0) {
            for (WsAttributeDefName defName : attributeDefNames) {
                String name = defName.getName();
                if (name.equals(LISTSERV)) {
                    isListservOn = true;
                } else if (name.equals(OPT_IN)) {
                    isOptInOn = true;
                } else if (name.equals(OPT_OUT)) {
                    isOptOutOn = true;
                } else if (name.equals(RELEASED_GROUPING)) {
                    isReleasedGroupingOn = true;
                }
            }
        }

        grouping.setListservOn(isListservOn);
        grouping.setOptInOn(isOptInOn);
        grouping.setOptOutOn(isOptOutOn);
        grouping.setReleasedGroupingOn(isReleasedGroupingOn);

        return grouping;
    }

    //returns the list of groups that the user is in, searching by username or uuid
    //todo This returns an error code of 500 for some reason
    //todo Changed to return empty list like AdminsGroupings does
    @Override
    public List<String> getGroupPaths(String ownerUsername, String username) {
        logger.info("getGroupPaths; username: " + username + ";");

        if (ownerUsername.equals(username) || memberAttributeService.isSuperuser(ownerUsername)) {
            WsStemLookup stemLookup = grouperFactoryService.makeWsStemLookup(STEM);
            WsGetGroupsResults wsGetGroupsResults;

            wsGetGroupsResults = grouperFactoryService.makeWsGetGroupsResults(
                    username,
                    stemLookup,
                    StemScope.ALL_IN_SUBTREE
            );

            WsGetGroupsResult groupResults = wsGetGroupsResults.getResults()[0];

            List<WsGroup> groups = new ArrayList<>();

            if (groupResults.getWsGroups() != null) {
                groups = new ArrayList<>(Arrays.asList(groupResults.getWsGroups()));
            }

            return extractGroupPaths(groups);

        } else {
            List<String> results = new ArrayList<>();
            return results;
            //            GroupingsHTTPException ghe = new GroupingsHTTPException();
            //            throw new GroupingsHTTPException("User does not have proper permissions.", ghe, 403);
        }
    }

    public List<String> getGroupPaths(Principal principal, String username) {
        return getGroupPaths(principal.getName(), username);
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
        return names.stream().collect(Collectors.toList());
    }

    //returns the list of groupings that the user is allowed to opt-in to
    public List<Grouping> groupingsToOptInto(String optInUsername, List<String> groupPaths) {
        logger.info("groupingsToOptInto; username: " + optInUsername + "; groupPaths : " + groupPaths + ";");

        List<String> trios = new ArrayList<>();
        List<String> opts = new ArrayList<>();
        List<String> excludes = groupPaths
                .stream()
                .map(group -> group + EXCLUDE)
                .collect(Collectors.toList());

        WsGetAttributeAssignmentsResults assignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(
                        ASSIGN_TYPE_GROUP,
                        TRIO,
                        OPT_IN);

        if (assignmentsResults.getWsAttributeAssigns() != null) {
            for (WsAttributeAssign assign : assignmentsResults.getWsAttributeAssigns()) {
                if (assign.getAttributeDefNameName() != null) {
                    if (assign.getAttributeDefNameName().equals(TRIO)) {
                        trios.add(assign.getOwnerGroupName());
                    } else if (assign.getAttributeDefNameName().equals(OPT_IN)) {
                        opts.add(assign.getOwnerGroupName());
                    }
                }
            }

            //opts intersection trios
            opts.retainAll(trios);
            //excludes intersection opts
            excludes.retainAll(opts);
            //opts - (opts intersection groupPaths)
            opts.removeAll(groupPaths);
            //opts union excludes
            opts.addAll(excludes);

        }

        //get rid of duplicates
        List<String> groups = new ArrayList<>(new HashSet<>(opts));
        return helperService.makeGroupings(groups);
    }

    //returns a list of groupings that the user is allowed to opt-out of
    public List<Grouping> groupingsToOptOutOf(String optOutUsername, List<String> groupPaths) {
        logger.info("groupingsToOptOutOf; username: " + optOutUsername + "; groupPaths: " + groupPaths + ";");

        List<String> trios = new ArrayList<>();
        List<String> opts = new ArrayList<>();
        List<WsAttributeAssign> attributeAssigns = new ArrayList<>();

        List<WsGetAttributeAssignmentsResults> assignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(
                        ASSIGN_TYPE_GROUP,
                        TRIO,
                        OPT_OUT,
                        groupPaths);

        assignmentsResults
                .stream()
                .filter(results -> results.getWsAttributeAssigns() != null)
                .forEach(results -> attributeAssigns.addAll(Arrays.asList(results.getWsAttributeAssigns())));

        if (attributeAssigns.size() > 0) {
            attributeAssigns.stream().filter(assign -> assign.getAttributeDefNameName() != null).forEach(assign -> {
                if (assign.getAttributeDefNameName().equals(TRIO)) {
                    trios.add(assign.getOwnerGroupName());
                } else if (assign.getAttributeDefNameName().equals(OPT_OUT)) {
                    opts.add(assign.getOwnerGroupName());
                }
            });

            opts.retainAll(trios);
        }

        return helperService.makeGroupings(opts);
    }

}
