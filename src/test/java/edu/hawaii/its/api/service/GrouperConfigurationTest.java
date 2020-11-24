package edu.hawaii.its.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GrouperConfigurationTest {

    @Autowired
    GrouperConfiguration grouperConfiguration;

    @Value("${groupings.api.settings}") private String SETTINGS;

    @Value("${groupings.api.grouping_admins}") private String GROUPING_ADMINS;
    @Value("${groupings.api.grouping_apps}") private String GROUPING_APPS;
    @Value("${groupings.api.grouping_owners}") private String GROUPING_OWNERS;
    @Value("${groupings.api.grouping_superusers}") private String GROUPING_SUPERUSERS;
    @Value("${groupings.api.attributes}") private String ATTRIBUTES;
    @Value("${groupings.api.for_groups}") private String FOR_GROUPS;
    @Value("${groupings.api.for_memberships}") private String FOR_MEMBERSHIPS;
    @Value("${groupings.api.last_modified}") private String LAST_MODIFIED;
    @Value("${groupings.api.yyyymmddThhmm}") private String YYYYMMDDTHHMM;
    @Value("${groupings.api.uhgrouping}") private String UHGROUPING;
    @Value("${groupings.api.destinations}") private String DESTINATIONS;
    @Value("${groupings.api.listserv}") private String LISTSERV;
    @Value("${groupings.api.releasedgrouping}") private String RELEASED_GROUPING;
    @Value("${groupings.api.trio}") private String TRIO;
    @Value("${groupings.api.purge_grouping}") private String PURGE_GROUPING;
    @Value("${groupings.api.self_opted}") private String SELF_OPTED;
    @Value("${groupings.api.anyone_can}") private String ANYONE_CAN;
    @Value("${groupings.api.opt_in}") private String OPT_IN;
    @Value("${groupings.api.opt_out}") private String OPT_OUT;
    @Value("${groupings.api.basis}") private String BASIS;
    @Value("${groupings.api.basis_plus_include}") private String BASIS_PLUS_INCLUDE;
    @Value("${groupings.api.exclude}") private String EXCLUDE;
    @Value("${groupings.api.include}") private String INCLUDE;
    @Value("${groupings.api.owners}") private String OWNERS;
    @Value("${groupings.api.assign_type_group}") private String ASSIGN_TYPE_GROUP;
    @Value("${groupings.api.assign_type_immediate_membership}") private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;
    @Value("${groupings.api.subject_attribute_name_uhuuid}") private String SUBJECT_ATTRIBUTE_NAME_UID;
    @Value("${groupings.api.operation_assign_attribute}") private String OPERATION_ASSIGN_ATTRIBUTE;
    @Value("${groupings.api.operation_remove_attribute}") private String OPERATION_REMOVE_ATTRIBUTE;
    @Value("${groupings.api.operation_replace_values}") private String OPERATION_REPLACE_VALUES;
    @Value("${groupings.api.privilege_opt_out}") private String PRIVILEGE_OPT_OUT;
    @Value("${groupings.api.privilege_opt_in}") private String PRIVILEGE_OPT_IN;
    @Value("${groupings.api.every_entity}") private String EVERY_ENTITY;
    @Value("${groupings.api.is_member}") private String IS_MEMBER;
    @Value("${groupings.api.success}") private String SUCCESS;
    @Value("${groupings.api.failure}") private String FAILURE;
    @Value("${groupings.api.stem}") private String STEM;
    @Value("${groupings.api.person_attributes.username}") private String UID;
    @Value("${groupings.api.person_attributes.first_name}") private String FIRST_NAME;
    @Value("${groupings.api.person_attributes.last_name}") private String LAST_NAME;
    @Value("${groupings.api.person_attributes.composite_name}") private String COMPOSITE_NAME;
    @Value("${groupings.api.insufficient_privileges}") private String INSUFFICIENT_PRIVILEGES;

    @Value("${groupings.api.test.username}") private String USERNAME;
    @Value("${groupings.api.test.name}") private String NAME;
    @Value("${groupings.api.test.uhuuid}") private String UHUUID;
    @Value("${groupings.api.test.surname}") private String SN;
    @Value("${groupings.api.test.given_name}") private String GIVEN_NAME;
    @Value("${groupings.api.googlegroup}") private String GOOGLE_GROUP;

    @Value("${groupings.api.attribute_assign_id_size}") private Integer ATTRIBUTES_ASSIGN_ID_SIZE;
    @Value("${groupings.api.composite_type.complement}") private String COMPLEMENT;
    @Value("${groupings.api.composite_type.intersection}") private String INTERSECTION;
    @Value("${groupings.api.composite_type.union}") private String UNION;

    @Value("${groupings.api.success_allowed}") private String SUCCESS_ALLOWED;
    @Value("${groupings.api.person_attributes.uhuuid}") private String UHUUID_KEY;



    @Value("${groupings.api.timeout}") private Integer TIMEOUT;
    @Test public void getTimeoutTest() {assertEquals(grouperConfiguration.getTimeout(), TIMEOUT);}

    @Value("${groupings.api.stale_subject_id}") private String STALE_SUBJECT_ID;
    @Test public void getStaleSubjectIdTest() {assertEquals(grouperConfiguration.getStaleSubjectId(), STALE_SUBJECT_ID);}

    @Value("${groupings.api.test.admin_user}") private String ADMIN;
    @Test public void getAdminTest() {assertEquals(grouperConfiguration.getAdmin(), ADMIN);}

    @Value("${groupings.api.grouping_owners}") private String OWNERS_GROUP;
    @Test public void getOwnersGroupTest() {assertEquals(grouperConfiguration.getOwnersGroup(), OWNERS_GROUP);}





    @Test public void getSuccessAllowedTest() {assertEquals(grouperConfiguration.getSuccessAllowed(), SUCCESS_ALLOWED);}
    @Test public void getUhuuidKeyTest() {assertEquals(grouperConfiguration.getUhuuidKey(), UHUUID_KEY);}

    @Test public void getAttributesAssignIdSizeTest() { assertEquals(grouperConfiguration.getAttributesAssignIdSize(), ATTRIBUTES_ASSIGN_ID_SIZE);}
    @Test public void getComplementTest() { assertEquals(grouperConfiguration.getComplement(), COMPLEMENT);}
    @Test public void getIntersectionTest() { assertEquals(grouperConfiguration.getIntersection(), INTERSECTION);}
    @Test public void getUnionTest() { assertEquals(grouperConfiguration.getUnion(), UNION);}


    @Test public void construction(){ assertNotNull(grouperConfiguration); }

    @Test public void getApiTest() { assertNotNull(grouperConfiguration.getApi()); }

    @Test public void getSettingsTest() { assertEquals(grouperConfiguration.getSettings(), SETTINGS); }

    public void getGroupingAdminsTest() { assertEquals(grouperConfiguration.getGroupingAdmins(), GROUPING_ADMINS); }

    public void getGroupingAppsTest() { assertEquals(grouperConfiguration.getGroupingApps(), GROUPING_APPS); }

    public void getGroupingOwnersTest() { assertEquals(grouperConfiguration.getGroupingOwners(), GROUPING_OWNERS); }

    public void getGroupingSuperuserTest() { assertEquals(grouperConfiguration.getGroupingSuperuser(), GROUPING_SUPERUSERS); }

    public void getAttributesTest() { assertEquals(grouperConfiguration.getAttributes(), ATTRIBUTES); }

    public void getForGroupsTest() { assertEquals(grouperConfiguration.getForGroups(), FOR_GROUPS); }

    public void getForMembershipsTest() { assertEquals(grouperConfiguration.getForMemberships(), FOR_MEMBERSHIPS); }

    public void getLastModifiedTest() { assertEquals(grouperConfiguration.getLastModified(), LAST_MODIFIED); }

    public void getYyyymmddthhmmTest() { assertEquals(grouperConfiguration.getYyyymmddthhmm(), YYYYMMDDTHHMM); }

    public void getUhgroupingTest() { assertEquals(grouperConfiguration.getUhgrouping(), UHGROUPING); }

    public void getDestinationsTest() { assertEquals(grouperConfiguration.getDestinations(), DESTINATIONS); }

    public void getListservTest() { assertEquals(grouperConfiguration.getListserv(), LISTSERV); }

    public void getReleasedGroupingTest() { assertEquals(grouperConfiguration.getReleasedGrouping(), RELEASED_GROUPING); }

    public void getTrioTest() { assertEquals(grouperConfiguration.getTrio(), TRIO); }

    public void getPurgeGroupingTest() { assertEquals(grouperConfiguration.getPurgeGrouping(), PURGE_GROUPING); }

    public void getSelfOptedTest() { assertEquals(grouperConfiguration.getSelfOpted(), SELF_OPTED); }

    public void getAnyoneCanTest() { assertEquals(grouperConfiguration.getAnyoneCan(), ANYONE_CAN); }

    public void getOptInTest() { assertEquals(grouperConfiguration.getOptIn(), OPT_IN); }

    public void getOptOutTest() { assertEquals(grouperConfiguration.getOptOut(), OPT_OUT); }

    @Test public void getBasisTest() { assertEquals(grouperConfiguration.getBasis(), BASIS); }

    @Test public void getBasisPlusIncludeTest() { assertEquals(grouperConfiguration.getBasisPlusInclude(), BASIS_PLUS_INCLUDE); }

    @Test public void getExcludeTest() { assertEquals(grouperConfiguration.getExclude(), EXCLUDE); }

    @Test public void getIncludeTest() { assertEquals(grouperConfiguration.getInclude(), INCLUDE); }

    @Test public void getOwnersTest() { assertEquals(grouperConfiguration.getOwners(), OWNERS); }

    @Test public void getAssignTypeGroupTest() { assertEquals(grouperConfiguration.getAssignTypeGroup(), ASSIGN_TYPE_GROUP); }

    @Test public void getAssignTypeImmediateMembershipTest() { assertEquals(grouperConfiguration.getAssignTypeImmediateMembership(), ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP); }

    @Test public void getSubjectAttributeNameUidTest() { assertEquals(grouperConfiguration.getSubjectAttributeNameUid(), SUBJECT_ATTRIBUTE_NAME_UID); }

    @Test public void getOperationAssignAttributeTest() { assertEquals(grouperConfiguration.getOperationAssignAttribute(), OPERATION_ASSIGN_ATTRIBUTE); }

    @Test public void getOperationRemoveAttributeTest() { assertEquals(grouperConfiguration.getOperationRemoveAttribute(), OPERATION_REMOVE_ATTRIBUTE); }

    @Test public void getOperationReplaceValuesTest() { assertEquals(grouperConfiguration.getOperationReplaceValues(), OPERATION_REPLACE_VALUES); }

    @Test public void getPrivilegeOptOutTest() { assertEquals(grouperConfiguration.getPrivilegeOptOut(), PRIVILEGE_OPT_OUT); }

    @Test public void getPrivilegeOptInTest() { assertEquals(grouperConfiguration.getPrivilegeOptIn(), PRIVILEGE_OPT_IN); }

    @Test public void getEveryEntityTest() { assertEquals(grouperConfiguration.getEveryEntity(), EVERY_ENTITY); }

    @Test public void getIsMemberTest() { assertEquals(grouperConfiguration.getIsMember(), IS_MEMBER); }

    @Test public void getSuccessTest() { assertEquals(grouperConfiguration.getSuccess(), SUCCESS); }

    @Test public void getFailureTest() { assertEquals(grouperConfiguration.getFailure(), FAILURE); }

    @Test public void getStemTest() { assertEquals(grouperConfiguration.getStem(), STEM); }

    @Test public void getUidTest() { assertEquals(grouperConfiguration.getUid(), UID); }

    @Test public void getFirstNameTest() { assertEquals(grouperConfiguration.getFirstName(), FIRST_NAME); }

    @Test public void getLastNameTest() { assertEquals(grouperConfiguration.getLastName(), LAST_NAME); }

    @Test public void getCompositeNameTest() { assertEquals(grouperConfiguration.getCompositeName(), COMPOSITE_NAME); }

    @Test public void getInsufficientPrivilegesTest() { assertEquals(grouperConfiguration.getInsufficientPrivileges(), INSUFFICIENT_PRIVILEGES); }

    @Test public void getUsernameTest() {assertEquals(grouperConfiguration.getUsername(), USERNAME);}

    @Test public void getNameTest() {assertEquals(grouperConfiguration.getName(), NAME);}

    @Test public void getUhuuidTest() {assertEquals(grouperConfiguration.getUhuuid(), UHUUID);}

    @Test public void getSnTest() {assertEquals(grouperConfiguration.getSn(), SN);}

    @Test public void getGivenNameTest() {assertEquals(grouperConfiguration.getGivenName(), GIVEN_NAME);}

    @Test public void getGoogleGroupTest() {assertEquals(grouperConfiguration.getGoogleGroup(), GOOGLE_GROUP);}
}
