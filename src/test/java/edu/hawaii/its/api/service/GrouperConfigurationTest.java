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

    @Autowired GrouperConfiguration grouperConfiguration;

    @Value("${groupings.api.current_user}") private String CURRENT_USER;
    @Value("${groupings.api.attributes}") private String ATTRIBUTES;
    @Value("${groupings.api.for_groups}") private String FOR_GROUPS;
    @Value("${groupings.api.for_memberships}") private String FOR_MEMBERSHIPS;
    @Value("${groupings.api.settings}") private String SETTINGS;
    @Value("${groupings.api.grouping_admins}") private String GROUPING_ADMINS;
    @Value("${groupings.api.grouping_apps}") private String GROUPING_APPS;
    @Value("${groupings.api.grouping_owners}") private String GROUPING_OWNERS;
    @Value("${groupings.api.grouping_superusers}") private String GROUPING_SUPERUSERS;
    @Value("${groupings.api.stale_subject_id}") private String STALE_SUBJECT_ID;
    @Value("${groupings.api.success_allowed}") private String SUCCESS_ALLOWED;
    @Value("${groupings.api.timeout}") private Integer TIMEOUT;
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
    @Value("${groupings.api.subject_attribute_name_uhuuid}") private String SUBJECT_ATTRIBUTE_NAME_UHUUID;
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
    @Value("${groupings.api.person_attributes.username}") private String PERSON_ATTRIBUTES_USERNAME;
    @Value("${groupings.api.person_attributes.uhuuid}") private String PERSON_ATTRIBUTES_UHUUID;
    @Value("${groupings.api.person_attributes.first_name}") private String PERSON_ATTRIBUTES_FIRST_NAME;
    @Value("${groupings.api.person_attributes.last_name}") private String PERSON_ATTRIBUTES_LAST_NAME;
    @Value("${groupings.api.person_attributes.composite_name}") private String PERSON_ATTRIBUTES_COMPOSITE_NAME;
    @Value("${groupings.api.insufficient_privileges}") private String INSUFFICIENT_PRIVILEGES;
    @Value("${groupings.api.test.username}") private String TEST_USERNAME;
    @Value("${groupings.api.test.name}") private String TEST_NAME;
    @Value("${groupings.api.test.uhuuid}") private String TEST_UHUUID;
    @Value("${groupings.api.test.admin_user}") private String TEST_ADMIN_USER;
    @Value("${groupings.api.test.surname}") private String TEST_SN;
    @Value("${groupings.api.test.given_name}") private String TEST_GIVEN_NAME;
    @Value("${groupings.api.googlegroup}") private String GOOGLE_GROUP;
    @Value("${groupings.api.attribute_assign_id_size}") private Integer ATTRIBUTES_ASSIGN_ID_SIZE;
    @Value("${groupings.api.composite_type.complement}") private String COMPOSITE_TYPE_COMPLEMENT;
    @Value("${groupings.api.composite_type.intersection}") private String COMPOSITE_TYPE_INTERSECTION;
    @Value("${groupings.api.composite_type.union}") private String COMPOSITE_TYPE_UNION;

    @Test public void grouperConfigurationTest() {
        assertNotNull(grouperConfiguration.getApi());
    }

    @Test public void getAttributesTest() {
        assertEquals(ATTRIBUTES, grouperConfiguration.getAttributes());
    }
    @Test public void getCurrentUserTest() {
        assertEquals(CURRENT_USER, grouperConfiguration.getCurrentUser());
    }

    @Test public void getForGroupsTest() {
        assertEquals(FOR_GROUPS, grouperConfiguration.getForGroups());
    }

    @Test public void getForMembershipsTest() {
        assertEquals(FOR_MEMBERSHIPS, grouperConfiguration.getForMemberships());
    }

    @Test public void getSettingsTest() {
        assertEquals(SETTINGS, grouperConfiguration.getSettings());
    }

    @Test public void getGroupingAdminsTest() {
        assertEquals(GROUPING_ADMINS, grouperConfiguration.getGroupingAdmins());
    }

    @Test public void getGroupingAppsTest() {
        assertEquals(GROUPING_APPS, grouperConfiguration.getGroupingApps());
    }

    @Test public void getGroupingOwnersTest() {
        assertEquals(GROUPING_OWNERS, grouperConfiguration.getGroupingOwners());
    }

    @Test public void getGroupingSuperuserTest() {
        assertEquals(GROUPING_SUPERUSERS, grouperConfiguration.getGroupingSuperuser());
    }

    @Test public void getStaleSubjectIdTest() {
        assertEquals(STALE_SUBJECT_ID, grouperConfiguration.getStaleSubjectId());
    }

    @Test public void getSuccessAllowedTest() {
        assertEquals(SUCCESS_ALLOWED, grouperConfiguration.getSuccessAllowed());
    }

    @Test public void getTimeoutTest() {
        assertEquals(TIMEOUT, grouperConfiguration.getTimeout());
    }

    @Test public void getLastModifiedTest() {
        assertEquals(LAST_MODIFIED, grouperConfiguration.getLastModified());
    }

    @Test public void getYyyymmddthhmmTest() {
        assertEquals(YYYYMMDDTHHMM, grouperConfiguration.getYyyymmddthhmm());
    }

    @Test public void getUhgroupingTest() {
        assertEquals(UHGROUPING, grouperConfiguration.getUhgrouping());
    }

    @Test public void getDestinationsTest() {
        assertEquals(DESTINATIONS, grouperConfiguration.getDestinations());
    }

    @Test public void getListservTest() {
        assertEquals(LISTSERV, grouperConfiguration.getListserv());
    }

    @Test public void getReleasedGroupingTest() {
        assertEquals(RELEASED_GROUPING, grouperConfiguration.getReleasedGrouping());
    }

    @Test public void getTrioTest() {
        assertEquals(TRIO, grouperConfiguration.getTrio());
    }

    @Test public void getPurgeGroupingTest() {
        assertEquals(PURGE_GROUPING, grouperConfiguration.getPurgeGrouping());
    }

    @Test public void getSelfOptedTest() {
        assertEquals(SELF_OPTED, grouperConfiguration.getSelfOpted());
    }

    @Test public void getAnyoneCanTest() {
        assertEquals(ANYONE_CAN, grouperConfiguration.getAnyoneCan());
    }

    @Test public void getOptInTest() {
        assertEquals(OPT_IN, grouperConfiguration.getOptIn());
    }

    @Test public void getOptOutTest() {
        assertEquals(OPT_OUT, grouperConfiguration.getOptOut());
    }

    @Test public void getBasisTest() {
        assertEquals(BASIS, grouperConfiguration.getBasis());
    }

    @Test public void getBasisPlusIncludeTest() {
        assertEquals(BASIS_PLUS_INCLUDE, grouperConfiguration.getBasisPlusInclude());
    }

    @Test public void getExcludeTest() {
        assertEquals(EXCLUDE, grouperConfiguration.getExclude());
    }

    @Test public void getIncludeTest() {
        assertEquals(INCLUDE, grouperConfiguration.getInclude());
    }

    @Test public void getOwnersTest() {
        assertEquals(OWNERS, grouperConfiguration.getOwners());
    }

    @Test public void getAssignTypeGroupTest() {
        assertEquals(ASSIGN_TYPE_GROUP, grouperConfiguration.getAssignTypeGroup());
    }

    @Test public void getAssignTypeImmediateMembershipTest() {
        assertEquals(ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP, grouperConfiguration.getAssignTypeImmediateMembership());
    }

    @Test public void getSubjectAttributeNameUidTest() {
        assertEquals(SUBJECT_ATTRIBUTE_NAME_UHUUID, grouperConfiguration.getSubjectAttributeNameUid());
    }

    @Test public void getOperationAssignAttributeTest() {
        assertEquals(OPERATION_ASSIGN_ATTRIBUTE, grouperConfiguration.getOperationAssignAttribute());
    }

    @Test public void getOperationRemoveAttributeTest() {
        assertEquals(OPERATION_REMOVE_ATTRIBUTE, grouperConfiguration.getOperationRemoveAttribute());
    }

    @Test public void getOperationReplaceValuesTest() {
        assertEquals(OPERATION_REPLACE_VALUES, grouperConfiguration.getOperationReplaceValues());
    }

    @Test public void getPrivilegeOptOutTest() {
        assertEquals(PRIVILEGE_OPT_OUT, grouperConfiguration.getPrivilegeOptOut());
    }

    @Test public void getPrivilegeOptInTest() {
        assertEquals(PRIVILEGE_OPT_IN, grouperConfiguration.getPrivilegeOptIn());
    }

    @Test public void getEveryEntityTest() {
        assertEquals(EVERY_ENTITY, grouperConfiguration.getEveryEntity());
    }

    @Test public void getIsMemberTest() {
        assertEquals(IS_MEMBER, grouperConfiguration.getIsMember());
    }

    @Test public void getSuccessTest() {
        assertEquals(SUCCESS, grouperConfiguration.getSuccess());
    }

    @Test public void getFailureTest() {
        assertEquals(FAILURE, grouperConfiguration.getFailure());
    }

    @Test public void getStemTest() {
        assertEquals(STEM, grouperConfiguration.getStem());
    }

    @Test public void getUidTest() {
        assertEquals(PERSON_ATTRIBUTES_USERNAME, grouperConfiguration.getPersonAttributesUsername());
    }

    @Test public void getPersonAttributesUhuuidTest() {
        assertEquals(PERSON_ATTRIBUTES_UHUUID, grouperConfiguration.getPersonAttributesUhuuid());
    }

    @Test public void getPersonAttributesFirstNameTest() {
        assertEquals(PERSON_ATTRIBUTES_FIRST_NAME, grouperConfiguration.getPersonAttributesFirstName());
    }

    @Test public void getPersonAttributesLastNameTest() {
        assertEquals(PERSON_ATTRIBUTES_LAST_NAME, grouperConfiguration.getPersonAttributesLastName());
    }

    @Test public void getPersonAttributesCompositeNameTest() {
        assertEquals(PERSON_ATTRIBUTES_COMPOSITE_NAME, grouperConfiguration.getPersonAttributesCompositeName());
    }

    @Test public void getInsufficientPrivilegesTest() {
        assertEquals(INSUFFICIENT_PRIVILEGES, grouperConfiguration.getInsufficientPrivileges());
    }

    @Test public void getTestUsernameTest() {
        assertEquals(TEST_USERNAME, grouperConfiguration.getTestUsername());
    }

    @Test public void getTestNameTest() {
        assertEquals(TEST_NAME, grouperConfiguration.getTestName());
    }

    @Test public void getTestUhuuidTest() {
        assertEquals(TEST_UHUUID, grouperConfiguration.getTestUhuuid());
    }

    @Test public void getTestAdminUserTest() {
        assertEquals(TEST_ADMIN_USER, grouperConfiguration.getTestAdminUser());
    }

    @Test public void getTestSnTest() {
        assertEquals(TEST_SN, grouperConfiguration.getTestSn());
    }

    @Test public void getTestGivenNameTest() {
        assertEquals(TEST_GIVEN_NAME, grouperConfiguration.getTestGivenName());
    }

    @Test public void getGoogleGroupTest() {
        assertEquals(GOOGLE_GROUP, grouperConfiguration.getGoogleGroup());
    }

    @Test public void getAttributesAssignIdSizeTest() {
        assertEquals(ATTRIBUTES_ASSIGN_ID_SIZE, grouperConfiguration.getAttributesAssignIdSize());
    }

    @Test public void getCompositeTypeComplementTest() {
        assertEquals(COMPOSITE_TYPE_COMPLEMENT, grouperConfiguration.getCompositeTypeComplement());
    }

    @Test public void getCompositeTypeIntersectionTest() {
        assertEquals(COMPOSITE_TYPE_INTERSECTION, grouperConfiguration.getCompositeTypeIntersection());
    }

    @Test public void getCompositeTypeUnionTest() {
        assertEquals(COMPOSITE_TYPE_UNION, grouperConfiguration.getCompositeTypeUnion());
    }
}
