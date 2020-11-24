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

    @Test public void construction(){ assertNotNull(grouperConfiguration); }

    @Test public void getApiTest() { assertNotNull(grouperConfiguration.getApi()); }

    @Test public void getSettingsTest() { assertEquals(grouperConfiguration.getSettings(), SETTINGS); }

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
}
