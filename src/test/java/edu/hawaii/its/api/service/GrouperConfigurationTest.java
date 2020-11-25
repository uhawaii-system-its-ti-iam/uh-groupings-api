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

    @Test public void grouperConfigurationTest() {
        assertNotNull(grouperConfiguration.getApi());
    }

    @Value("${groupings.api.attributes}") private String ATTRIBUTES;
    @Test public void getAttributesTest() {
        assertNotNull(ATTRIBUTES);
        assertEquals(grouperConfiguration.getAttributes(), ATTRIBUTES);
    }

    @Value("${groupings.api.for_groups}") private String FOR_GROUPS;
    @Test public void getForGroupsTest() {
        assertEquals(grouperConfiguration.getForGroups(), FOR_GROUPS);
    }

    @Value("${groupings.api.for_memberships}") private String FOR_MEMBERSHIPS;
    @Test public void getForMembershipsTest() {
        assertEquals(grouperConfiguration.getForMemberships(), FOR_MEMBERSHIPS);
    }

    @Value("${groupings.api.settings}") private String SETTINGS;
    @Test public void getSettingsTest() {
        assertEquals(grouperConfiguration.getSettings(), SETTINGS);
    }

    @Value("${groupings.api.grouping_admins}") private String GROUPING_ADMINS;
    @Test public void getGroupingAdminsTest() {
        assertEquals(grouperConfiguration.getGroupingAdmins(), GROUPING_ADMINS);
    }

    @Value("${groupings.api.grouping_apps}") private String GROUPING_APPS;
    @Test public void getGroupingAppsTest() {
        assertEquals(grouperConfiguration.getGroupingApps(), GROUPING_APPS);
    }

    @Value("${groupings.api.grouping_owners}") private String GROUPING_OWNERS;
    @Test public void getGroupingOwnersTest() {
        assertEquals(grouperConfiguration.getGroupingOwners(), GROUPING_OWNERS);
    }

    @Value("${groupings.api.grouping_superusers}") private String GROUPING_SUPERUSERS;
    @Test public void getGroupingSuperuserTest() {
        assertEquals(grouperConfiguration.getGroupingSuperuser(), GROUPING_SUPERUSERS);
    }

    @Value("${groupings.api.stale_subject_id}") private String STALE_SUBJECT_ID;
    @Test public void getStaleSubjectIdTest() {
        assertEquals(grouperConfiguration.getStaleSubjectId(), STALE_SUBJECT_ID);
    }

    @Value("${groupings.api.success_allowed}") private String SUCCESS_ALLOWED;
    @Test public void getSuccessAllowedTest() {
        assertEquals(grouperConfiguration.getSuccessAllowed(), SUCCESS_ALLOWED);
    }

    @Value("${groupings.api.timeout}") private Integer TIMEOUT;
    @Test public void getTimeoutTest() {
        assertEquals(grouperConfiguration.getTimeout(), TIMEOUT);
    }

    @Value("${groupings.api.last_modified}") private String LAST_MODIFIED;
    @Test public void getLastModifiedTest() {
        assertEquals(grouperConfiguration.getLastModified(), LAST_MODIFIED);
    }

    @Value("${groupings.api.yyyymmddThhmm}") private String YYYYMMDDTHHMM;
    @Test public void getYyyymmddthhmmTest() {
        assertEquals(grouperConfiguration.getYyyymmddthhmm(), YYYYMMDDTHHMM);
    }

    @Value("${groupings.api.uhgrouping}") private String UHGROUPING;
    @Test public void getUhgroupingTest() {
        assertEquals(grouperConfiguration.getUhgrouping(), UHGROUPING);
    }

    @Value("${groupings.api.destinations}") private String DESTINATIONS;
    @Test public void getDestinationsTest() {
        assertEquals(grouperConfiguration.getDestinations(), DESTINATIONS);
    }

    @Value("${groupings.api.listserv}") private String LISTSERV;
    @Test public void getListservTest() {
        assertEquals(grouperConfiguration.getListserv(), LISTSERV);
    }

    @Value("${groupings.api.releasedgrouping}") private String RELEASED_GROUPING;
    @Test public void getReleasedGroupingTest() {
        assertEquals(grouperConfiguration.getReleasedGrouping(), RELEASED_GROUPING);
    }

    @Value("${groupings.api.trio}") private String TRIO;
    @Test public void getTrioTest() {
        assertEquals(grouperConfiguration.getTrio(),TRIO);
    }

    @Value("${groupings.api.purge_grouping}") private String PURGE_GROUPING;
    @Test public void getPurgeGroupingTest() {
        assertEquals(grouperConfiguration.getPurgeGrouping(), PURGE_GROUPING);
    }

    @Value("${groupings.api.self_opted}") private String SELF_OPTED;
    @Test public void getSelfOptedTest() {
        assertEquals(grouperConfiguration.getSelfOpted(), SELF_OPTED);
    }

    @Value("${groupings.api.anyone_can}") private String ANYONE_CAN;
    @Test public void getAnyoneCanTest() {
        assertEquals(grouperConfiguration.getAnyoneCan(), ANYONE_CAN);
    }

    @Value("${groupings.api.opt_in}") private String OPT_IN;
    @Test public void getOptInTest() {
        assertEquals(grouperConfiguration.getOptIn(), OPT_IN);
    }

    @Value("${groupings.api.opt_out}") private String OPT_OUT;
    @Test public void getOptOutTest() {
        assertEquals(grouperConfiguration.getOptOut(), OPT_OUT);
    }

    @Value("${groupings.api.basis}") private String BASIS;
    @Test public void getBasisTest() {
        assertEquals(grouperConfiguration.getBasis(), BASIS);
    }

    @Value("${groupings.api.basis_plus_include}") private String BASIS_PLUS_INCLUDE;
    @Test public void getBasisPlusIncludeTest() {
        assertEquals(grouperConfiguration.getBasisPlusInclude(), BASIS_PLUS_INCLUDE);
    }

    @Value("${groupings.api.exclude}") private String EXCLUDE;
    @Test public void getExcludeTest() {
        assertEquals(grouperConfiguration.getExclude(), EXCLUDE);
    }

    @Value("${groupings.api.include}") private String INCLUDE;
    @Test public void getIncludeTest() {
        assertEquals(grouperConfiguration.getInclude(), INCLUDE);
    }

    @Value("${groupings.api.owners}") private String OWNERS;
    @Test public void getOwnersTest() {
        assertEquals(grouperConfiguration.getOwners(), OWNERS);
    }

    @Value("${groupings.api.assign_type_group}") private String ASSIGN_TYPE_GROUP;
    @Test public void getAssignTypeGroupTest() {
        assertEquals(grouperConfiguration.getAssignTypeGroup(), ASSIGN_TYPE_GROUP);
    }

    @Value("${groupings.api.assign_type_immediate_membership}") private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;
    @Test public void getAssignTypeImmediateMembershipTest() {
        assertEquals(grouperConfiguration.getAssignTypeImmediateMembership(), ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP);
    }

    @Value("${groupings.api.subject_attribute_name_uhuuid}") private String SUBJECT_ATTRIBUTE_NAME_UHUUID;
    @Test public void getSubjectAttributeNameUidTest() {
        assertEquals(grouperConfiguration.getSubjectAttributeNameUid(), SUBJECT_ATTRIBUTE_NAME_UHUUID);
    }

    @Value("${groupings.api.operation_assign_attribute}") private String OPERATION_ASSIGN_ATTRIBUTE;
    @Test public void getOperationAssignAttributeTest() {
        assertEquals(grouperConfiguration.getOperationAssignAttribute(), OPERATION_ASSIGN_ATTRIBUTE);
    }

    @Value("${groupings.api.operation_remove_attribute}") private String OPERATION_REMOVE_ATTRIBUTE;
    @Test public void getOperationRemoveAttributeTest() {
        assertEquals(grouperConfiguration.getOperationRemoveAttribute(), OPERATION_REMOVE_ATTRIBUTE);
    }

    @Value("${groupings.api.operation_replace_values}") private String OPERATION_REPLACE_VALUES;
    @Test public void getOperationReplaceValuesTest() {
        assertEquals(grouperConfiguration.getOperationReplaceValues(), OPERATION_REPLACE_VALUES);
    }

    @Value("${groupings.api.privilege_opt_out}") private String PRIVILEGE_OPT_OUT;
    @Test public void getPrivilegeOptOutTest() {
        assertEquals(grouperConfiguration.getPrivilegeOptOut(), PRIVILEGE_OPT_OUT);
    }

    @Value("${groupings.api.privilege_opt_in}") private String PRIVILEGE_OPT_IN;
    @Test public void getPrivilegeOptInTest() {
        assertEquals(grouperConfiguration.getPrivilegeOptIn(), PRIVILEGE_OPT_IN);
    }

    @Value("${groupings.api.every_entity}") private String EVERY_ENTITY;
    @Test public void getEveryEntityTest() {
        assertEquals(grouperConfiguration.getEveryEntity(), EVERY_ENTITY);
    }

    @Value("${groupings.api.is_member}") private String IS_MEMBER;
    @Test public void getIsMemberTest() {
        assertEquals(grouperConfiguration.getIsMember(),IS_MEMBER);
    }

    @Value("${groupings.api.success}") private String SUCCESS;
    @Test public void getSuccessTest() {
        assertEquals(grouperConfiguration.getSuccess(), SUCCESS);
    }

    @Value("${groupings.api.failure}") private String FAILURE;
    @Test public void getFailureTest() {
        assertEquals(grouperConfiguration.getFailure(), FAILURE);
    }

    @Value("${groupings.api.stem}") private String STEM;
    @Test public void getStemTest() {
        assertEquals(grouperConfiguration.getStem(), STEM);
    }

    @Value("${groupings.api.person_attributes.username}") private String UID;
    @Test public void getUidTest() {
        assertEquals(grouperConfiguration.getUid(), UID);
    }

    @Value("${groupings.api.person_attributes.uhuuid}") private String PERSON_ATTRIBUTES_UHUUID;
    @Test public void getPersonAttributesUhuuidTest() {
        assertEquals(grouperConfiguration.getPersonAttributesUhuuid(), PERSON_ATTRIBUTES_UHUUID);
    }

    @Value("${groupings.api.person_attributes.first_name}") private String PERSON_ATTRIBUTES_FIRST_NAME;
    @Test public void getPersonAttributesFirstNameTest() {
        assertEquals(grouperConfiguration.getPersonAttributesFirstName(), PERSON_ATTRIBUTES_FIRST_NAME);
    }

    @Value("${groupings.api.person_attributes.last_name}") private String PERSON_ATTRIBUTES_LAST_NAME;
    @Test public void getPersonAttributesLastNameTest() {
        assertEquals(grouperConfiguration.getPersonAttributesLastName(), PERSON_ATTRIBUTES_LAST_NAME);
    }

    @Value("${groupings.api.person_attributes.composite_name}") private String PERSON_ATTRIBUTES_COMPOSITE_NAME;
    @Test public void getPersonAttributesCompositeNameTest() {
        assertEquals(grouperConfiguration.getPersonAttributesCompositeName(), PERSON_ATTRIBUTES_COMPOSITE_NAME);
    }

    @Value("${groupings.api.insufficient_privileges}") private String INSUFFICIENT_PRIVILEGES;
    @Test public void getInsufficientPrivilegesTest() {
        assertEquals(grouperConfiguration.getInsufficientPrivileges(), INSUFFICIENT_PRIVILEGES);
    }

    @Value("${groupings.api.test.username}") private String TEST_USERNAME;
    @Test public void getTestUsernameTest() {
        assertEquals(grouperConfiguration.getTestUsername(), TEST_USERNAME);
    }

    @Value("${groupings.api.test.name}") private String TEST_NAME;
    @Test public void getTestNameTest() {
        assertEquals(grouperConfiguration.getTestName(), TEST_NAME);
    }

    @Value("${groupings.api.test.uhuuid}") private String TEST_UHUUID;
    @Test public void getTestUhuuidTest() {
        assertEquals(grouperConfiguration.getTestUhuuid(), TEST_UHUUID);
    }

    @Value("${groupings.api.test.admin_user}") private String TEST_ADMIN_USER;
    @Test public void getTestAdminUserTest() {
        assertEquals(grouperConfiguration.getTestAdminUser(), TEST_ADMIN_USER);
    }

    @Value("${groupings.api.test.surname}") private String TEST_SN;
    @Test public void getTestSnTest() {
        assertEquals(grouperConfiguration.getTestSn(), TEST_SN);
    }

    @Value("${groupings.api.test.given_name}") private String TEST_GIVEN_NAME;
    @Test public void getTestGivenNameTest() {
        assertEquals(grouperConfiguration.getTestGivenName(), TEST_GIVEN_NAME);
    }

    @Value("${groupings.api.googlegroup}") private String GOOGLE_GROUP;
    @Test public void getGoogleGroupTest() {
        assertEquals(grouperConfiguration.getGoogleGroup(), GOOGLE_GROUP);
    }

    @Value("${groupings.api.attribute_assign_id_size}") private Integer ATTRIBUTES_ASSIGN_ID_SIZE;
    @Test public void getAttributesAssignIdSizeTest() {
        assertEquals(grouperConfiguration.getAttributesAssignIdSize(), ATTRIBUTES_ASSIGN_ID_SIZE);
    }

    @Value("${groupings.api.composite_type.complement}") private String COMPOSITE_TYPE_COMPLEMENT;
    @Test public void getCompositeTypeComplementTest() {
        assertEquals(grouperConfiguration.getCompositeTypeComplement(), COMPOSITE_TYPE_COMPLEMENT);
    }

    @Value("${groupings.api.composite_type.intersection}") private String COMPOSITE_TYPE_INTERSECTION;
    @Test public void getCompositeTypeIntersectionTest() {
        assertEquals(grouperConfiguration.getCompositeTypeIntersection(), COMPOSITE_TYPE_INTERSECTION);
    }

    @Value("${groupings.api.composite_type.union}") private String COMPOSITE_TYPE_UNION;
    @Test public void getCompositeTypeUnionTest() {
        assertEquals(grouperConfiguration.getCompositeTypeUnion(), COMPOSITE_TYPE_UNION);
    }

}
