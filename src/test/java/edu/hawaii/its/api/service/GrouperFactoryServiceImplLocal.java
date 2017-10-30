package edu.hawaii.its.api.service;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("GrouperFactoryService")
@Profile(value = {"default", "dev", "localTest"})
public class GrouperFactoryServiceImplLocal implements GrouperFactoryService {

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

    @Value("${groupings.api.trio}")
    private String TRIO;

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

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private PersonRepository personRepository;

    public GrouperFactoryServiceImplLocal() {

    }

    @Override
    public WsGroupSaveResults addEmptyGroup(String username, String path) {

        Group newGroup = new Group(path);
        groupRepository.save(newGroup);

        WsGroupSaveResults wsGroupSaveResults = new WsGroupSaveResults();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(SUCCESS);
        wsGroupSaveResults.setResultMetadata(wsResultMeta);

        return wsGroupSaveResults;
    }

    /**
     * @param username: username of user to be looked up
     * @return a WsSubjectLookup with username as the subject identifier
     */
    @Override
    public WsSubjectLookup makeWsSubjectLookup(String username) {
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        wsSubjectLookup.setSubjectIdentifier(username);

        return wsSubjectLookup;
    }

    /**
     * @param group: group to be looked up
     * @return a WsGroupLookup with group as the group name
     */
    @Override
    public WsGroupLookup makeWsGroupLookup(String group) {
        WsGroupLookup groupLookup = new WsGroupLookup();
        groupLookup.setGroupName(group);

        return groupLookup;
    }

    @Override
    public WsStemLookup makeWsStemLookup(String stemName, String stemUuid) {
        return new WsStemLookup(stemName, stemUuid);
    }

    @Override
    public WsStemSaveResults makeWsStemSaveResults(String username, String stemPath) {
        WsStemSaveResults wsStemSaveResults = new WsStemSaveResults();
        WsStemSaveResult wsStemSaveResult = new WsStemSaveResult();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(SUCCESS);
        wsStemSaveResult.setResultMetadata(wsResultMeta);
        wsStemSaveResults.setResultMetadata(wsResultMeta);
        wsStemSaveResults.setResults(new WsStemSaveResult[]{wsStemSaveResult});

        return wsStemSaveResults;
    }

    @Override
    public WsAttributeAssignValue makeWsAttributeAssignValue(String time) {
        WsAttributeAssignValue dateTimeValue = new WsAttributeAssignValue();
        dateTimeValue.setValueSystem(time);

        return dateTimeValue;
    }

    @Override
    public WsAddMemberResults makeWsAddMemberResults(String group, WsSubjectLookup lookup, String newMember) {
        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();

        Grouping ownedGrouping = groupingRepository.findByOwnersPath(group);
        Person owner = personRepository.findByUsername(lookup.getSubjectIdentifier());

        if (ownedGrouping.getOwners().getMembers().contains(owner)) {
            wsAddMemberResults = makeWsAddMemberResults(group, newMember);
        } else {
            WsResultMeta wsResultMeta = new WsResultMeta();
            wsResultMeta.setResultCode(FAILURE);
        }

        return wsAddMemberResults;
    }

    @Override
    public WsAddMemberResults makeWsAddMemberResults(String group, WsSubjectLookup lookup, List<String> newMembers) {
        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(SUCCESS);

        for (String username : newMembers) {
            WsResultMeta wsResultMetaData = makeWsAddMemberResults(group, lookup, username).getResultMetadata();
            if (wsResultMetaData.getResultCode().equals(FAILURE)) {
                wsResultMeta = wsResultMetaData;
            }
        }

        wsAddMemberResults.setResultMetadata(wsResultMeta);

        return wsAddMemberResults;
    }

    @Override
    public WsAddMemberResults makeWsAddMemberResults(String group, String newMember) {
        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(SUCCESS);
        wsAddMemberResults.setResultMetadata(wsResultMeta);

        Grouping grouping = groupingRepository.findByIncludePathOrExcludePathOrCompositePathOrOwnersPath(group, group, group, group);
        Person newGroupMember = personRepository.findByUsername(newMember);
        Membership membership;

        boolean inBasis = grouping.getBasis().getMembers().contains(newGroupMember);
        boolean inExclude = grouping.getExclude().getMembers().contains(newGroupMember);
        boolean inInclude = grouping.getInclude().getMembers().contains(newGroupMember);


        if (group.endsWith(EXCLUDE)) {
            if (inBasis) {
                grouping.getExclude().getMembers().add(newGroupMember);
                membership = new Membership(newGroupMember, grouping.getExclude());
                membershipRepository.save(membership);
            } else if (inInclude) {
                membership = membershipRepository.findByPersonAndGroup(newGroupMember, grouping.getInclude());
                grouping.getInclude().getMembers().remove(newGroupMember);
                membershipRepository.delete(membership);
            }
        } else if (group.endsWith(INCLUDE)) {
            if(inExclude){
                membership = membershipRepository.findByPersonAndGroup(newGroupMember, grouping.getExclude());
                grouping.getExclude().getMembers().remove(newGroupMember);
                membershipRepository.delete(membership);
            }
            else if(!inBasis){
                //TODO make addMember and deleteMember methods
                grouping.getInclude().getMembers().add(newGroupMember);
                membership = new Membership(newGroupMember, grouping.getInclude());
                membershipRepository.save(membership);
            }
        }

        groupingRepository.save(grouping);

        return wsAddMemberResults;
    }

    @Override
    public WsDeleteMemberResults makeWsDeleteMemberResults(String group, String memberToDelete) {
        WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(SUCCESS);
        wsDeleteMemberResults.setResultMetadata(wsResultMeta);

        Grouping grouping = groupingRepository.findByIncludePathOrExcludePathOrCompositePathOrOwnersPath(group, group, group, group);
        Person personToDelete = personRepository.findByUsername(memberToDelete);
        Membership membership;

        if (group.endsWith(EXCLUDE)) {
            membership = membershipRepository.findByPersonAndGroup(personToDelete, grouping.getExclude());
            grouping.getExclude().getMembers().remove(personToDelete);
            membershipRepository.delete(membership);

        } else if (group.endsWith(INCLUDE)) {
            membership = membershipRepository.findByPersonAndGroup(personToDelete, grouping.getInclude());
            grouping.getInclude().getMembers().remove(personToDelete);
            membershipRepository.delete(membership);
        }

        groupingRepository.save(grouping);

        return wsDeleteMemberResults;
    }

    @Override
    public WsDeleteMemberResults makeWsDeleteMemberResults(String group, WsSubjectLookup lookup, String memberToDelete) {
        WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();

        Grouping ownedGrouping = groupingRepository.findByOwnersPath(group);
        Person owner = personRepository.findByUsername(lookup.getSubjectIdentifier());

        if (ownedGrouping.getOwners().getMembers().contains(owner)) {
            wsDeleteMemberResults = makeWsDeleteMemberResults(group, memberToDelete);
        } else {
            WsResultMeta wsResultMeta = new WsResultMeta();
            wsResultMeta.setResultCode(FAILURE);
        }

        return wsDeleteMemberResults;
    }

    @Override
    public WsDeleteMemberResults makeWsDeleteMemberResults(String group, WsSubjectLookup lookup, List<String> membersToDelete) {
        WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(SUCCESS);

        for (String username : membersToDelete) {
            WsResultMeta wsResultMetaData = makeWsDeleteMemberResults(group, lookup, username).getResultMetadata();
            if (wsResultMetaData.getResultCode().equals(FAILURE)) {
                wsResultMeta = wsResultMetaData;
            }
        }

        wsDeleteMemberResults.setResultMetadata(wsResultMeta);

        return wsDeleteMemberResults;
    }


    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResults(String assignType,
                                                                                 //TODO
                                                                                 String attributeDefNameName) {
//        return new GcGetAttributeAssignments()
//                .addAttributeDefNameName(attributeDefNameName)
//                .assignAttributeAssignType(assignType)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResults(String assignType,
                                                                                 //TODO
                                                                                 String attributeDefNameName0,
                                                                                 String attributeDefNameName1) {
//        return new GcGetAttributeAssignments()
//                .addAttributeDefNameName(attributeDefNameName0)
//                .addAttributeDefNameName(attributeDefNameName1)
//                .assignAttributeAssignType(assignType)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResults(String assignType,
                                                                                 //TODO
                                                                                 String attributeDefNameName,
                                                                                 List<String> ownerGroupNames) {

//        GcGetAttributeAssignments getAttributeAssignments = new GcGetAttributeAssignments()
//                .addAttributeDefNameName(attributeDefNameName)
//                .assignAttributeAssignType(assignType);
//
//        ownerGroupNames.forEach(getAttributeAssignments::addOwnerGroupName);
//
//        return getAttributeAssignments.execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResults(String assignType,
                                                                                 //TODO
                                                                                 String attributeDefNameName0,
                                                                                 String attributeDefNameName1,
                                                                                 List<String> ownerGroupNames) {

//        GcGetAttributeAssignments getAttributeAssignments = new GcGetAttributeAssignments()
//                .addAttributeDefNameName(attributeDefNameName0)
//                .addAttributeDefNameName(attributeDefNameName1)
//                .assignAttributeAssignType(assignType);
//
//        ownerGroupNames.forEach(getAttributeAssignments::addOwnerGroupName);
//
//        return getAttributeAssignments.execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsForMembership(String assignType,
                                                                                              //TODO
                                                                                              String attributeDefNameName,
                                                                                              String membershipId) {
//        return new GcGetAttributeAssignments()
//                .addAttributeDefNameName(attributeDefNameName)
//                .addOwnerMembershipId(membershipId)
//                .assignAttributeAssignType(assignType)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsForGroup(String assignType,
                                                                                         String group) {
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
        List<WsAttributeDefName> wsAttributeDefNames = new ArrayList<>();

        Grouping grouping = groupingRepository.findByPath(group);
        if (grouping.isListservOn()) {
            WsAttributeAssign wsAttributeAssign = new WsAttributeAssign();
            WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
            wsAttributeDefName.setName(LISTSERV);
            wsAttributeAssign.setAttributeDefNameName(LISTSERV);
            wsAttributeDefNames.add(wsAttributeDefName);
        }
        if (grouping.isOptInOn()) {
            WsAttributeAssign wsAttributeAssign = new WsAttributeAssign();
            WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
            wsAttributeDefName.setName(OPT_IN);
            wsAttributeAssign.setAttributeDefNameName(OPT_IN);
            wsAttributeDefNames.add(wsAttributeDefName);
        }
        if (grouping.isOptOutOn()) {
            WsAttributeAssign wsAttributeAssign = new WsAttributeAssign();
            WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
            wsAttributeDefName.setName(OPT_OUT);
            wsAttributeAssign.setAttributeDefNameName(OPT_OUT);
            wsAttributeDefNames.add(wsAttributeDefName);
        }

        wsGetAttributeAssignmentsResults.setWsAttributeDefNames(wsAttributeDefNames.toArray(new WsAttributeDefName[wsAttributeDefNames.size()]));

        return wsGetAttributeAssignmentsResults;
    }

    @Override
    public WsGetAttributeAssignmentsResults makeWsGetAttributeAssignmentsResultsForGroup(String assignType,
                                                                                         String attributeDefNameName,
                                                                                         String group) {
        return makeWsGetAttributeAssignmentsResultsForGroup(assignType, group);
    }

    @Override
    public WsHasMemberResults makeWsHasMemberResults(String group, String username) {
        WsHasMemberResults wsHasMemberResults = new WsHasMemberResults();
        WsHasMemberResult wsHasMemberResult = new WsHasMemberResult();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsHasMemberResult.setResultMetadata(wsResultMeta);
        wsHasMemberResults.setResults(new WsHasMemberResult[]{wsHasMemberResult});

        Group groupToCheck = groupRepository.findByPath(group);
        Person person = personRepository.findByUsername(username);

        if (groupToCheck.getMembers().contains(person)) {
            wsResultMeta.setResultCode(IS_MEMBER);
        } else {
            wsResultMeta.setResultCode("not member");
        }

        return wsHasMemberResults;
    }

    @Override
    public WsAssignAttributesResults makeWsAssignAttributesResults(String attributeAssignType,
                                                                   //TODO
                                                                   String attributeAssignOperation,
                                                                   String ownerGroupName,
                                                                   String attributeDefNameName,
                                                                   String attributeAssignValueOperation,
                                                                   WsAttributeAssignValue value) {

//        return new GcAssignAttributes()
//                .assignAttributeAssignType(attributeAssignType)
//                .assignAttributeAssignOperation(attributeAssignOperation)
//                .addOwnerGroupName(ownerGroupName)
//                .addAttributeDefNameName(attributeDefNameName)
//                .assignAttributeAssignValueOperation(attributeAssignValueOperation)
//                .addValue(value)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsAssignAttributesResults makeWsAssignAttributesResultsForMembership(String attributeAssignType,
                                                                                //TODO
                                                                                String attributeAssignOperation,
                                                                                String attributeDefNameName,
                                                                                String ownerMembershipId) {

//        return new GcAssignAttributes()
//                .assignAttributeAssignType(attributeAssignType)
//                .assignAttributeAssignOperation(attributeAssignOperation)
//                .addAttributeDefNameName(attributeDefNameName)
//                .addOwnerMembershipId(ownerMembershipId)
//                .execute();
        throw new NotImplementedException();
    }


    @Override
    public WsAssignAttributesResults makeWsAssignAttributesResultsForGroup(String attributeAssingType,
                                                                           String attributeAssignOperation,
                                                                           String attributeDefNameName,
                                                                           String ownerGroupName) {
        WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
        WsResultMeta wsResultMeta = new WsResultMeta();
        wsResultMeta.setResultCode(FAILURE);

        Grouping grouping = groupingRepository.findByPath(ownerGroupName);

        if (setGroupingAttribute(grouping, attributeDefNameName, true)) {
            wsResultMeta.setResultCode(SUCCESS);
        }

        wsAssignAttributesResults.setResultMetadata(wsResultMeta);
        return wsAssignAttributesResults;
    }

    @Override
    public WsAssignAttributesResults makeWsAssignAttributesResultsForGroup(WsSubjectLookup lookup,
                                                                           String attributeAssingType,
                                                                           String attributeAssignOperation,
                                                                           String attributeDefNameName,
                                                                           String ownerGroupName) {
        WsAssignAttributesResults wsAssignAttributesResults;

        Grouping grouping = groupingRepository.findByPath(ownerGroupName);
        Person person = personRepository.findByUsername(lookup.getSubjectIdentifier());

        if (grouping.getOwners().getMembers().contains(person)) {
            wsAssignAttributesResults = makeWsAssignAttributesResultsForGroup(attributeAssingType,
                    attributeAssignOperation,
                    attributeDefNameName,
                    ownerGroupName);
        } else {
            wsAssignAttributesResults = new WsAssignAttributesResults();
            WsResultMeta wsResultMeta = new WsResultMeta();
            wsResultMeta.setResultCode(FAILURE);
            wsAssignAttributesResults.setResultMetadata(wsResultMeta);
        }
        return wsAssignAttributesResults;
    }

    @Override
    public WsAssignGrouperPrivilegesLiteResult makeWsAssignGrouperPrivilegesLiteResult(String groupName,
                                                                                       //TODO
                                                                                       String privilegeName,
                                                                                       WsSubjectLookup lookup,
                                                                                       boolean allowed) {

//        return new GcAssignGrouperPrivilegesLite()
//                .assignGroupName(groupName)
//                .assignPrivilegeName(privilegeName)
//                .assignSubjectLookup(lookup)
//                .assignAllowed(allowed)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetGrouperPrivilegesLiteResult makeWsGetGrouperPrivilegesLiteResult(String groupName,
                                                                                 //TODO
                                                                                 String privilegeName,
                                                                                 WsSubjectLookup lookup) {

//        return new GcGetGrouperPrivilegesLite()
//                .assignGroupName(groupName)
//                .assignPrivilegeName(privilegeName)
//                .assignSubjectLookup(lookup)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetMembershipsResults makeWsGetMembershipsResults(String groupName,
                                                               //TODO
                                                               WsSubjectLookup lookup) {

//        return new GcGetMemberships()
//                .addGroupName(groupName)
//                .addWsSubjectLookup(lookup)
//                .execute();
        throw new NotImplementedException();
    }

    @Override
    public WsGetMembersResults makeWsGetMembersResults(String subjectAttributeName,
                                                       WsSubjectLookup lookup,
                                                       String groupName) {

        WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        WsSubject[] subjects;

        Group group = groupRepository.findByPath(groupName);
        List<Person> members = group.getMembers();
        List<WsSubject> subjectList = new ArrayList<>();

        for (Person person : members) {
            WsSubject subject = new WsSubject();
            subject.setId(person.getUsername());

            subjectList.add(subject);
        }

        subjects = subjectList.toArray(new WsSubject[subjectList.size()]);

        wsGetMembersResult.setWsSubjects(subjects);
        wsGetMembersResults.setResults(new WsGetMembersResult[]{wsGetMembersResult});

        return wsGetMembersResults;
    }

    @Override
    public WsGetGroupsResults makeWsGetGroupsResults(String username,
                                                     WsStemLookup stemLookup,
                                                     StemScope stemScope) {

        WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
        WsGetGroupsResult wsGetGroupsResult = new WsGetGroupsResult();
        WsGroup[] groups;

        List<WsGroup> wsGroupList = new ArrayList<>();
        List<Group> groupList = groupRepository.findByMembersUsername(username);

        for (Group group : groupList) {
            WsGroup g = new WsGroup();
            g.setName(group.getPath());
            wsGroupList.add(g);
        }

        groups = wsGroupList.toArray(new WsGroup[wsGroupList.size()]);

        wsGetGroupsResult.setWsGroups(groups);
        wsGetGroupsResults.setResults(new WsGetGroupsResult[]{wsGetGroupsResult});

        return wsGetGroupsResults;
    }

    @Override
    public WsAttributeAssign[] makeEmptyWsAttributeAssignArray() {
        return new WsAttributeAssign[0];
    }


    ////////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////////

    private Group buildComposite(Group include, Group exclude, Group basis, String path) {
        Group basisPlusInclude = addIncludedMembers(include, basis);
        Group compositeGroup = removeExcludedMembers(basisPlusInclude, exclude);
        compositeGroup.setPath(path);

        return compositeGroup;
    }

    private Group addIncludedMembers(Group include, Group basis) {
        Group unionGroup = new Group();
        List<Person> unionList = new ArrayList<>();
        unionList.addAll(include.getMembers());
        unionList.addAll(basis.getMembers());

        //remove duplicates
        Set<Person> s = new TreeSet<>();
        s.addAll(unionList);
        unionGroup.setMembers(Arrays.asList(s.toArray(new Person[s.size()])));

        return unionGroup;
    }

    private Group removeExcludedMembers(Group basisPlusInclude, Group exclude) {
        Group basisPlusIncludeMinusExcludeGroup = new Group();
        ArrayList<Person> newBasisPlusInclude = new ArrayList<>();
        newBasisPlusInclude.addAll(basisPlusInclude.getMembers());

        for (Person person : exclude.getMembers()) {
            newBasisPlusInclude.remove(person);
        }
        basisPlusIncludeMinusExcludeGroup.setMembers(newBasisPlusInclude);

        return basisPlusIncludeMinusExcludeGroup;
    }

    private boolean setGroupingAttribute(Grouping grouping, String attributeName, boolean on) {
        if (attributeName.equals(LISTSERV)) {
            grouping.setListservOn(on);
        } else if (attributeName.equals(OPT_IN)) {
            grouping.setOptInOn(on);
        } else if (attributeName.equals(OPT_OUT)) {
            grouping.setOptOutOn(on);
        } else {
            return false;
        }
        groupingRepository.save(grouping);
        return true;

    }
}
