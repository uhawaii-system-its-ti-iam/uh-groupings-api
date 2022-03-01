package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.SyncDestination;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Value("${groupings.api.subject_attribute_name_uhuuid}")
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

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.timeout}")
    private Integer TIMEOUT;

    @Value("${groupings.api.stale_subject_id}")
    private String STALE_SUBJECT_ID;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    public static final Log logger = LogFactory.getLog(GroupingAssignmentServiceImpl.class);

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired GroupAttributeService groupAttributeService;

    // returns a list of all of the groups in groupPaths that are also groupings
    @Override
    public List<Grouping> groupingsIn(List<String> groupPaths) {
        List<String> groupingsIn = helperService.extractGroupings(groupPaths);
        List<Grouping> groupings = helperService.makeGroupings(groupingsIn);

        groupings.forEach(this::setGroupingAttributes);

        return groupings;
    }

    @Override
    public List<Grouping> restGroupingsExclude(String actingUsername, String ownerUsername) {
        return excludeGroups(getGroupPaths(actingUsername, ownerUsername));
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

    //returns a list of groupings that corresponds to all of the owner groups in groupPaths
    @Override
    public List<String> groupingsOwnedPaths(List<String> groupPaths) {
        List<String> ownerGroups = groupPaths
                .stream()
                .filter(groupPath -> groupPath.endsWith(OWNERS))
                .map(groupPath -> groupPath.substring(0, groupPath.length() - OWNERS.length()))
                .collect(Collectors.toList());

        // make sure the owner group actually correspond to a grouping
        return helperService.extractGroupings(ownerGroups);
    }

    @Override
    public List<Grouping> excludeGroups(List<String> groupPaths) {
        List<String> excludeGroups = groupPaths
                .stream()
                .filter(groupPath -> groupPath.endsWith(EXCLUDE))
                .map(groupPath -> groupPath.substring(0, groupPath.length() - EXCLUDE.length()))
                .collect(Collectors.toList());

        // make sure the owner group actually correspond to a grouping
        List<String> excludeGroupings = helperService.extractGroupings(excludeGroups);

        return helperService.makeGroupings(excludeGroupings);
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

    //fetch a grouping from Grouper or the database
    @Override
    public Grouping getGrouping(String groupingPath, String ownerUsername) {
        logger.info("getGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + ";");

        Grouping compositeGrouping;

        if (memberAttributeService.isOwner(groupingPath, ownerUsername) || memberAttributeService
                .isAdmin(ownerUsername)) {
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

            compositeGrouping.setDescription(grouperFactoryService.getDescription(groupingPath));
            compositeGrouping.setBasis(groups.get(basis));
            compositeGrouping.setExclude(groups.get(exclude));
            compositeGrouping.setInclude(groups.get(include));
            compositeGrouping.setComposite(groups.get(groupingPath));
            compositeGrouping.setOwners(groups.get(owners));

        } else {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        return compositeGrouping;
    }

    // Fetch a grouping from Grouper Database, but paginated based on given page + size
    // sortString sorts the database by whichever sortString category is given (e.g. "uid" will sort list by uid) before returning page
    // isAscending puts the database in ascending or descending order before returning page
    @Override
    public Grouping getPaginatedGrouping(String groupingPath, String ownerUsername, Integer page, Integer size,
            String sortString, Boolean isAscending) {
        logger.info(
                "getPaginatedGrouping; grouping: " + groupingPath + "; username: " + ownerUsername + "; page: " + page
                        + "; size: " + size + "; sortString: " + sortString + "; isAscending: " + isAscending + ";");

        if (memberAttributeService.isOwner(groupingPath, ownerUsername) || memberAttributeService
                .isAdmin(ownerUsername)) {

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

            compositeGrouping.setDescription(grouperFactoryService.getDescription(groupingPath));
            compositeGrouping.setBasis(groups.get(basis));
            compositeGrouping.setExclude(groups.get(exclude));
            compositeGrouping.setInclude(groups.get(include));
            compositeGrouping.setComposite(groups.get(groupingPath));
            compositeGrouping.setOwners(groups.get(owners));

            System.out.println("CompositeGroupingComingBack" + compositeGrouping);
            return compositeGrouping;
        } else {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
    }

    //returns an adminLists object containing the list of all admins and all groupings
    @Override
    public AdminListsHolder adminLists(String adminUsername) {
        if (!memberAttributeService.isAdmin(adminUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        AdminListsHolder adminListsHolder = new AdminListsHolder();

        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(ASSIGN_TYPE_GROUP, TRIO);

        List<WsGroup> groups = new ArrayList<>(Arrays.asList(attributeAssignmentsResults.getWsGroups()));

        List<String> groupingPathStrings = groups.stream().map(WsGroup::getName).collect(Collectors.toList());

        List<String> adminGrouping = new ArrayList<>(1);
        adminGrouping.add(GROUPING_ADMINS);
        Group admin = getMembers(adminUsername, adminGrouping).get(GROUPING_ADMINS);
        adminListsHolder.setAllGroupingPaths(helperService.makePaths(groupingPathStrings));
        adminListsHolder.setAdminGroup(admin);
        return adminListsHolder;
    }

    //returns a list of groupings corresponding to the include group or exclude group (includeOrrExclude) in groupPaths that
    //have the self-opted attribute set in the membership
    public List<Grouping> groupingsOpted(String includeOrrExclude, String username, List<String> groupPaths) {
        logger.info("groupingsOpted; includeOrrExclude: " + includeOrrExclude + "; username: " + username + ";");

        List<String> groupingsOpted = new ArrayList<>();

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
    public Map<String, Group> getMembers(String ownerUsername, List<String> groupPaths) {
        logger.info("getMembers; user: " + ownerUsername + "; groups: " + groupPaths + ";");

        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ownerUsername);
        WsGetMembersResults members = grouperFactoryService.makeWsGetMembersResults(
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

    @Override
    public Map<String, Group> getPaginatedMembers(String ownerUsername, List<String> groupPaths, Integer page,
            Integer size,
            String sortString, Boolean isAscending) {
        logger.info("getPaginatedMembers; ownerUsername: " + ownerUsername + "; groups: " + groupPaths +
                "; page: " + page + "; size: " + size + "; sortString: " + sortString + "; isAscending: " + isAscending
                + ";");

        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ownerUsername);
        WsGetMembersResults members = grouperFactoryService.makeWsGetMembersResults(
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
        } else {

            Map<String, String> attributes = new HashMap<>();
            for (int i = 0; i < subject.getAttributeValues().length; i++) {
                attributes.put(attributeNames[i], subject.getAttributeValue(i));
            }
            // uhUuid is the only attribute not actually in the WsSubject attribute array.
            attributes.put(UHUUID, subject.getId());

            return new Person(attributes);
        }
    }

    // Sets the attributes of a grouping in grouper or the database to match the attributes of the supplied grouping.
    public Grouping setGroupingAttributes(Grouping grouping) {
        logger.info("setGroupingAttributes; grouping: " + grouping + ";");

        boolean isOptInOn = false;
        boolean isOptOutOn = false;

        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsForGroup(
                        ASSIGN_TYPE_GROUP,
                        grouping.getPath());

        WsAttributeDefName[] attributeDefNames = wsGetAttributeAssignmentsResults.getWsAttributeDefNames();
        if (attributeDefNames != null && attributeDefNames.length > 0) {
            for (WsAttributeDefName defName : attributeDefNames) {
                String name = defName.getName();
                if (name.equals(OPT_IN)) {
                    isOptInOn = true;
                } else if (name.equals(OPT_OUT)) {
                    isOptOutOn = true;
                }
            }
        }

        grouping.setOptInOn(isOptInOn);
        grouping.setOptOutOn(isOptOutOn);

        // Set the sync destinations.
        List<SyncDestination> syncDestinations = groupAttributeService.getSyncDestinations(grouping);
        grouping.setSyncDestinations(syncDestinations);

        return grouping;
    }

    // Returns the list of groups that the user is in, searching by username or uhUuid.
    @Override
    public List<String> getGroupPaths(String ownerUsername, String username) {
        logger.info("getGroupPaths; username: " + username + ";");

        if (ownerUsername.equals(username) || memberAttributeService.isAdmin(ownerUsername)) {
            WsGetGroupsResults wsGetGroupsResults;

            wsGetGroupsResults = grouperFactoryService.makeWsGetGroupsResults(
                    username,
                    grouperFactoryService.makeWsStemLookup(STEM),
                    StemScope.ALL_IN_SUBTREE
            );

            WsGetGroupsResult groupResults = wsGetGroupsResults.getResults()[0];
            List<WsGroup> groups = new ArrayList<>();

            if (groupResults.getWsGroups() != null) {
                groups = new ArrayList<>(Arrays.asList(groupResults.getWsGroups()));
            }

            return extractGroupPaths(groups);

        } else {
            return new ArrayList<>();
        }
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

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    @Override
    public List<String> getOptOutGroups(String owner, String optOutUid) {
        logger.info("getOptOutGroups; owner: " + owner + "; optOutUid: " + optOutUid + ";");

        List<String> groupPaths = getGroupPaths(owner, optOutUid);
        List<String> trios = new ArrayList<>();
        List<String> opts = new ArrayList<>();
        List<String> includes = groupPaths
                .stream()
                .map(group -> group + INCLUDE)
                .collect(Collectors.toList());

        WsGetAttributeAssignmentsResults assignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(
                        ASSIGN_TYPE_GROUP,
                        TRIO,
                        OPT_OUT);

        if (assignmentsResults.getWsAttributeAssigns() != null) {
            for (WsAttributeAssign assign : assignmentsResults.getWsAttributeAssigns()) {
                if (assign.getAttributeDefNameName() != null) {
                    if (assign.getAttributeDefNameName().equals(TRIO)) {
                        String name = assign.getOwnerGroupName();
                        trios.add(assign.getOwnerGroupName());
                    } else if (assign.getAttributeDefNameName().equals(OPT_OUT)) {
                        String name = assign.getOwnerGroupName();
                        opts.add(assign.getOwnerGroupName());
                    }
                }
            }
            //opts intersection trios
            opts.retainAll(trios);
            //includes intersection opts
            includes.retainAll(opts);
            //opts - (opts intersection groupPaths)
            //opts union excludes
            opts.addAll(includes);
        }
        //get rid of duplicates
        return new ArrayList<>(new HashSet<>(opts));
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    @Override
    public List<String> getOptInGroups(String owner, String optInUid) {
        logger.info("getOptInGroups; owner: " + owner + "; optInUid: " + optInUid + ";");

        List<String> groupPaths = getGroupPaths(owner, optInUid);
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
                        String name = assign.getOwnerGroupName();
                        trios.add(assign.getOwnerGroupName());
                    } else if (assign.getAttributeDefNameName().equals(OPT_IN)) {
                        String name = assign.getOwnerGroupName();
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
        return new ArrayList<>(new HashSet<>(opts));
    }

    /**
     * List grouping paths than can be opted into or out of.
     */
    @Override
    public List<String> optableGroupings(String optAttr) {
        if (!optAttr.equals(OPT_IN) && !optAttr.equals(OPT_OUT)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperFactoryService.makeWsGetAttributeAssignmentsResultsTrio(ASSIGN_TYPE_GROUP, optAttr);
        List<WsAttributeAssign> attributeAssigns = Arrays.asList(attributeAssignmentsResults.getWsAttributeAssigns());
        List<String> optablePaths = new ArrayList<>();
        attributeAssigns.forEach(attributeAssign -> {
            if (attributeAssign.getAttributeDefNameName().equals(optAttr)) {
                optablePaths.add(attributeAssign.getOwnerGroupName());
            }
        });
        return new ArrayList<>(new HashSet<>(optablePaths));
    }

    //returns the list of groupings that the user is allowed to opt-in to
    @Override
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
