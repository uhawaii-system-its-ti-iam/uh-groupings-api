package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.el.ListELResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service("groupingFactoryService")
public class GroupingFactoryServiceImpl implements GroupingFactoryService {

    //todo For some reason cannot resolve
//    @Value("${grouperClient.webService.login}")
//    private String APP_USER;

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
    private String RELEASED;

    @Value("${groupings.api.trio}")
    private String TRIO;

    @Value("${groupings.api.purge_grouping}")
    private String PURGE;

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

    @Value("$groupings.api.stem}")
    private String STEM;

    @Value("${groupings.api.test.username}")
    private String USERNAME;

    @Value("${groupings.api.test.name}")
    private String NAME;

    @Value("${groupings.api.test.uuid}")
    private String UUID;

    @Value("${groupings.api.person_attributes.uuid}")
    private String UUID_KEY;

    @Value("${groupings.api.person_attributes.username}")
    private String UID_KEY;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME_KEY;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME_KEY;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME_KEY;

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MembershipService membershipService;


    @Override
    public List<GroupingsServiceResult> addGrouping(String adminUsername, String groupingPath) {


        List<GroupingsServiceResult> addGroupingResults = new ArrayList<>();
        String action = adminUsername + " is adding a Grouping: " + groupingPath;


        WsSubjectLookup admin = grouperFactoryService.makeWsSubjectLookup(adminUsername);
        WsSubjectLookup api = grouperFactoryService.makeWsSubjectLookup("_groupings_api_2");

        //make sure that adminUsername is actually an admin
        if (!memberAttributeService.isSuperuser(adminUsername)) {

            GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + adminUsername + " does not have permission to add this grouping", action
            );

            addGroupingResults.add(gsr);

            return addGroupingResults;
        }

        //make sure that there is not already a group there
        if (!pathIsEmpty(adminUsername, groupingPath)) {

            GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(
                    FAILURE + ": a group already exists at " + groupingPath, action);

            addGroupingResults.add(gsr);

            return addGroupingResults;
        }


        //Use hash map in order to easily create and add members by using key and entry values, not including composite
        Map<String, List<String>> memberLists = new HashMap<>();
        memberLists.put(BASIS_PLUS_INCLUDE, new ArrayList<>());
        memberLists.put(BASIS, new ArrayList<>());
        memberLists.put(INCLUDE, new ArrayList<>());
        memberLists.put(EXCLUDE, new ArrayList<>());
        memberLists.put(OWNERS, new ArrayList<>());

        // a stem the same as a folder
        //create main stem to contain groups that build into the main group
        grouperFactoryService.makeWsStemSaveResults(adminUsername, groupingPath);

        //create basis stem to contain groups or users that make up the basis
        grouperFactoryService.makeWsStemSaveResults(adminUsername, groupingPath + BASIS);


        //Creates groups and assigns the grouper api privileges
        for (Map.Entry<String, List<String>> entry : memberLists.entrySet()) {
            String groupPath = groupingPath + entry.getKey();

            //make the groups in grouper
            addGroupingResults.add(helperService.makeGroupingsServiceResult(
                    grouperFactoryService.addEmptyGroup(adminUsername, groupPath), action));
            grouperFactoryService.makeWsAssignGrouperPrivilegesLiteResult(groupPath, "groupAttrUpdate",
                    api, admin,true);

        }

        // Creates the composite and make complement of basis+include minus exclude
        addGroupingResults.add(helperService.makeGroupingsServiceResult(
                grouperFactoryService.addCompositeGroup(adminUsername, groupingPath, "complement",
                groupingPath + BASIS_PLUS_INCLUDE, groupingPath + EXCLUDE),
                "create " + groupingPath + " and complement of " + EXCLUDE));

        //Assigns grouper api privilege to composite
        grouperFactoryService.makeWsAssignGrouperPrivilegesLiteResult(groupingPath, "groupAttrUpdate",
                api, admin,true);


        String basisUid = getGroupId(groupingPath + BASIS);
        String includeUid = getGroupId(groupingPath + INCLUDE);

        //add memberships for BASIS_PLUS_INCLUDE (basis group and include group)
        addGroupingResults.add(helperService.makeGroupingsServiceResult(
                grouperFactoryService.makeWsAddMemberResultsGroup(groupingPath + BASIS_PLUS_INCLUDE,
                        admin, basisUid), "add " + groupingPath + BASIS + " to " + groupingPath
                        + BASIS_PLUS_INCLUDE));

        addGroupingResults.add(helperService.makeGroupingsServiceResult(
                grouperFactoryService.makeWsAddMemberResultsGroup(groupingPath + BASIS_PLUS_INCLUDE,
                        admin, includeUid), "add " + groupingPath + INCLUDE + " to " + groupingPath
                        + BASIS_PLUS_INCLUDE));

        //add the isTrio attribute out to the grouping
        grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                admin,
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                TRIO,
                groupingPath
        );

        return addGroupingResults;
    }



    @Override
    public List<GroupingsServiceResult> deleteGrouping(String adminUsername, String groupingPath) {


        List<GroupingsServiceResult> deleteGroupingResults = new ArrayList<>();
        String action = adminUsername + " is deleting a Grouping: " + groupingPath;

        //make sure that adminUsername is actually an admin
        if (!memberAttributeService.isSuperuser(adminUsername)) {

            GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + adminUsername + " does not have permission to delete this grouping", action
            );

            deleteGroupingResults.add(gsr);

            return deleteGroupingResults;
        }


        if (pathIsEmpty(adminUsername, groupingPath)) {

            GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + adminUsername + "the grouping " + groupingPath + " doesn't exist", action
            );

            deleteGroupingResults.add(gsr);

            return deleteGroupingResults;
        }

        WsSubjectLookup admin = grouperFactoryService.makeWsSubjectLookup(adminUsername);
        WsGroupLookup grouping = grouperFactoryService.makeWsGroupLookup(groupingPath);
        WsStemLookup mainStem = grouperFactoryService.makeWsStemLookup(groupingPath);
        WsStemLookup basisStem = grouperFactoryService.makeWsStemLookup(groupingPath + ":basis");


        deleteGroupingResults.add(helperService.makeGroupingsServiceResult(grouperFactoryService.deleteGroup(admin,
                grouping), "Delete composite group"));

        List<String> memberLists = new ArrayList<String>();
        memberLists.add(":basis");
        memberLists.add(":basis+include");
        memberLists.add(":exclude");
        memberLists.add(":include");
        memberLists.add(":owners");

        for (String group: memberLists) {

            if (pathIsEmpty(adminUsername, groupingPath + group)) {

            }
            else {
                WsGroupLookup groupLookup = grouperFactoryService.makeWsGroupLookup(groupingPath + group);

                deleteGroupingResults.add(helperService.makeGroupingsServiceResult(
                        grouperFactoryService.deleteGroup(admin, groupLookup), "Delete " + group + "group"));
            }
        }

        deleteGroupingResults.add(helperService.makeGroupingsServiceResult(grouperFactoryService.deleteStem(admin,
                basisStem), "Delete basis stem"));
        deleteGroupingResults.add(helperService.makeGroupingsServiceResult(grouperFactoryService.deleteStem(admin,
                mainStem), "Delete " + groupingPath + " stem"));;


        return deleteGroupingResults;
    }

    @Override
    public List<GroupingsServiceResult> markGroupForPurge(String adminUsername, String groupingPath) {

        List<GroupingsServiceResult> purgeGroupingResults = new ArrayList<>();
        String action = adminUsername + " is purging a Grouping: " + groupingPath;

        //make sure that adminUsername is actually an admin
        if (!memberAttributeService.isSuperuser(adminUsername)) {

            GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + adminUsername + " does not have permission to purge this grouping", action
            );

            purgeGroupingResults.add(gsr);

            return purgeGroupingResults;
        }


        if (pathIsEmpty(adminUsername, groupingPath)) {

            GroupingsServiceResult gsr = helperService.makeGroupingsServiceResult(
                    FAILURE + ": " + adminUsername + "the grouping " + groupingPath + " doesn't exist", action
            );

            purgeGroupingResults.add(gsr);

            return purgeGroupingResults;
        }

        WsSubjectLookup admin = grouperFactoryService.makeWsSubjectLookup(adminUsername);


        grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                admin,
                ASSIGN_TYPE_GROUP,
                OPERATION_REMOVE_ATTRIBUTE,
                TRIO,
                groupingPath
        );

        grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                admin,
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                PURGE,
                groupingPath
        );

        List<String> memberLists = new ArrayList<String>();
        memberLists.add(":basis");
        memberLists.add(":basis+include");
        memberLists.add(":exclude");
        memberLists.add(":include");
        memberLists.add(":owners");

        for (String group: memberLists) {

            if (pathIsEmpty(adminUsername, groupingPath + group)) {

            }
            else {

                grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                        admin,
                        ASSIGN_TYPE_GROUP,
                        OPERATION_REMOVE_ATTRIBUTE,
                        LISTSERV,
                        groupingPath + group
                );
                grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                        admin,
                        ASSIGN_TYPE_GROUP,
                        OPERATION_REMOVE_ATTRIBUTE,
                        RELEASED,
                        groupingPath + group
                );
                grouperFactoryService.makeWsAssignAttributesResultsForGroup(
                        admin,
                        ASSIGN_TYPE_GROUP,
                        OPERATION_ASSIGN_ATTRIBUTE,
                        PURGE,
                        groupingPath + group
                );

            }
        }

        return purgeGroupingResults;
    }

    //set of elements in list0 or list1
    private List<String> union(List<String> list0, List<String> list1) {

        if (list0 == null) {
            return list1 != null ? list1 : new ArrayList<>();
        }

        //remove duplicatesspo
        Set<String> treeSet = new TreeSet<>(list0);
        treeSet.addAll(list1);

        return new ArrayList<>(treeSet);
    }

    //set of elements in list0, but not in list1
    private List<String> complement(List<String> list0, List<String> list1) {
        if (list0 == null) {
            return new ArrayList<>();
        }

        if (list1 == null) {
            return list0;
        }

        list0.removeAll(list1);
        return list0;
    }

    //set of elements in both list0 and list1
    private List<String> intersection(List<String> list0, List<String> list1) {
        if (list0 == null || list1 == null) {
            return new ArrayList<>();
        }

        list0.retainAll(list1);
        return new ArrayList<>(list0);

    }

    //returns a group of Persons that have usernames from usernames
    //all other values will be left null
    private Group makeGroup(String groupPath, List<String> usernames) {
        List<Person> people = new ArrayList<>();

        for (String username : usernames) {
            people.add(new Person(null, null, username));
        }

        return new Group(groupPath, people);
    }

    //returns true if there is not a group at groupingPath
    public boolean pathIsEmpty(String adminUsername, String groupingPath) {

        WsFindGroupsResults wsFindGroupsResults = grouperFactoryService.makeWsFindGroupsResults(groupingPath);

        return wsFindGroupsResults.getGroupResults() == null;
    }

    //returns the uid for a group in grouper
    private String getGroupId(String groupPath) {
        WsFindGroupsResults results = grouperFactoryService.makeWsFindGroupsResults(groupPath);
        WsGroup result = results.getGroupResults()[0];
        return result.getUuid();
    }
}
