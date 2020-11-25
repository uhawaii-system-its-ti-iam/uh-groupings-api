package edu.hawaii.its.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "groupings")
public class GrouperConfiguration {

    private final Map<String, String> api = new HashMap<>();

    public Map<String, String> getApi() {
        return api;
    }



    @Value("${groupings.api.attributes}") private String ATTRIBUTES;
    public String getAttributes() {
        return api.get("attributes");
    }

    @Value("${groupings.api.for_groups}") private String FOR_GROUPS;
    public String getForGroups() {
        return api.get("for_groups");
    }

    @Value("${groupings.api.for_memberships}") private String FOR_MEMBERSHIPS;
    public String getForMemberships() {
        return api.get("for_memberships");
    }
    @Value("${groupings.api.current_user}") private String CURRENT_USER;
    public String getCurrentUser() {
        return api.get("current_user");
    }

    @Value("${groupings.api.settings}") private String SETTINGS;
    public String getSettings() {
        return api.get("settings");
    }

    @Value("${groupings.api.grouping_admins}") private String GROUPING_ADMINS;
    public String getGroupingAdmins() {
        return api.get("grouping_admins");
    }

    @Value("${groupings.api.grouping_apps}") private String GROUPING_APPS;
    public String getGroupingApps() {
        return api.get("grouping_apps");
    }

    @Value("${groupings.api.grouping_owners}") private String GROUPING_OWNERS;
    public String getGroupingOwners() {
        return api.get("grouping_owners");
    }

    @Value("${groupings.api.grouping_superusers}") private String GROUPING_SUPERUSERS;
    public String getGroupingSuperuser() {
        return api.get("grouping_superusers");
    }

    @Value("${groupings.api.stale_subject_id}") private String STALE_SUBJECT_ID;
    public String getStaleSubjectId() {
        return api.get("stale_subject_id");
    }

    @Value("${groupings.api.success_allowed}") private String SUCCESS_ALLOWED;
    public String getSuccessAllowed() {
        return api.get("success_allowed");
    }

    @Value("${groupings.api.timeout}") private Integer TIMEOUT;
    public Integer getTimeout() {
        return Integer.parseInt(api.get("timeout"));
    }

    @Value("${groupings.api.last_modified}") private String LAST_MODIFIED;
    public String getLastModified() {
        return api.get("last_modified");
    }

    @Value("${groupings.api.yyyymmddThhmm}") private String YYYYMMDDTHHMM;
    public String getYyyymmddthhmm() {
        return api.get("yyyymmddThhmm");
    }

    @Value("${groupings.api.uhgrouping}") private String UHGROUPING;
    public String getUhgrouping() {
        return api.get("uhgrouping");
    }

    @Value("${groupings.api.destinations}") private String DESTINATIONS;
    public String getDestinations() {
        return api.get("destinations");
    }

    @Value("${groupings.api.listserv}") private String LISTSERV;
    public String getListserv() {
        return api.get("listserv");
    }

    @Value("${groupings.api.releasedgrouping}") private String RELEASED_GROUPING;
    public String getReleasedGrouping() {
        return api.get("releasedgrouping");
    }

    @Value("${groupings.api.trio}") private String TRIO;
    public String getTrio() {
        return api.get("trio");
    }

    @Value("${groupings.api.purge_grouping}") private String PURGE_GROUPING;
    public String getPurgeGrouping() {
        return api.get("purge_grouping");
    }

    @Value("${groupings.api.self_opted}") private String SELF_OPTED;
    public String getSelfOpted() {
        return api.get("self_opted");
    }

    @Value("${groupings.api.anyone_can}") private String ANYONE_CAN;
    public String getAnyoneCan() {
        return api.get("anyone_can");
    }

    @Value("${groupings.api.opt_in}") private String OPT_IN;
    public String getOptIn() {
        return api.get("opt_in");
    }

    @Value("${groupings.api.opt_out}") private String OPT_OUT;
    public String getOptOut() {
        return api.get("opt_out");
    }

    @Value("${groupings.api.basis}") private String BASIS;
    public String getBasis() {
        return api.get("basis");
    }

    @Value("${groupings.api.basis_plus_include}") private String BASIS_PLUS_INCLUDE;
    public String getBasisPlusInclude() {
        return api.get("basis_plus_include");
    }

    @Value("${groupings.api.exclude}") private String EXCLUDE;
    public String getExclude() {
        return api.get("exclude");
    }

    @Value("${groupings.api.include}") private String INCLUDE;
    public String getInclude() {
        return api.get("include");
    }

    @Value("${groupings.api.owners}") private String OWNERS;
    public String getOwners() {
        return api.get("owners");
    }

    @Value("${groupings.api.assign_type_group}") private String ASSIGN_TYPE_GROUP;
    public String getAssignTypeGroup() {
        return api.get("assign_type_group");
    }

    @Value("${groupings.api.assign_type_immediate_membership}") private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;
    public String getAssignTypeImmediateMembership() {
        return api.get("assign_type_immediate_membership");
    }

    @Value("${groupings.api.subject_attribute_name_uhuuid}") private String SUBJECT_ATTRIBUTE_NAME_UHUUID;
    public String getSubjectAttributeNameUid() {
        return api.get("subject_attribute_name_uhuuid");
    }

    @Value("${groupings.api.operation_assign_attribute}") private String OPERATION_ASSIGN_ATTRIBUTE;
    public String getOperationAssignAttribute() {
        return api.get("operation_assign_attribute");
    }

    @Value("${groupings.api.operation_remove_attribute}") private String OPERATION_REMOVE_ATTRIBUTE;
    public String getOperationRemoveAttribute() {
        return api.get("operation_remove_attribute");
    }

    @Value("${groupings.api.operation_replace_values}") private String OPERATION_REPLACE_VALUES;
    public String getOperationReplaceValues() {
        return api.get("operation_replace_values");
    }

    @Value("${groupings.api.privilege_opt_out}") private String PRIVILEGE_OPT_OUT;
    public String getPrivilegeOptOut() {
        return api.get("privilege_opt_out");
    }

    @Value("${groupings.api.privilege_opt_in}") private String PRIVILEGE_OPT_IN;
    public String getPrivilegeOptIn() {
        return api.get("privilege_opt_in");
    }

    @Value("${groupings.api.every_entity}") private String EVERY_ENTITY;
    public String getEveryEntity() {
        return api.get("every_entity");
    }

    @Value("${groupings.api.is_member}") private String IS_MEMBER;
    public String getIsMember() {
        return api.get("is_member");
    }

    @Value("${groupings.api.success}") private String SUCCESS;
    public String getSuccess() {
        return api.get("success");
    }

    @Value("${groupings.api.failure}") private String FAILURE;
    public String getFailure() {
        return api.get("failure");
    }

    @Value("${groupings.api.stem}") private String STEM;
    public String getStem() {
        return api.get("stem");
    }

    @Value("${groupings.api.person_attributes.username}") private String PERSON_ATTRIBUTES_USERNAME;
    public String getPersonAttributesUsername() {
        return api.get("person_attributes.username");
    }

    @Value("${groupings.api.person_attributes.uhuuid}") private String PERSON_ATTRIBUTES_UHUUID;
    public String getPersonAttributesUhuuid() {
        return api.get("person_attributes.uhuuid");
    }

    @Value("${groupings.api.person_attributes.first_name}") private String PERSON_ATTRIBUTES_FIRST_NAME;
    public String getPersonAttributesFirstName() {
        return api.get("person_attributes.first_name");
    }

    @Value("${groupings.api.person_attributes.last_name}") private String PERSON_ATTRIBUTES_LAST_NAME;
    public String getPersonAttributesLastName() {
        return api.get("person_attributes.last_name");
    }

    @Value("${groupings.api.person_attributes.composite_name}") private String PERSON_ATTRIBUTES_COMPOSITE_NAME;
    public String getPersonAttributesCompositeName() {
        return api.get("person_attributes.composite_name");
    }

    @Value("${groupings.api.insufficient_privileges}") private String INSUFFICIENT_PRIVILEGES;
    public String getInsufficientPrivileges() {
        return api.get("insufficient_privileges");
    }

    @Value("${groupings.api.test.username}") private String TEST_USERNAME;
    public String getTestUsername() {
        return api.get("test.username");
    }

    @Value("${groupings.api.test.name}") private String TEST_NAME;
    public String getTestName() {
        return api.get("test.name");
    }

    @Value("${groupings.api.test.uhuuid}") private String TEST_UHUUID;
    public String getTestUhuuid() {
        return api.get("test.uhuuid");
    }

    @Value("${groupings.api.test.admin_user}") private String TEST_ADMIN_USER;
    public String getTestAdminUser() {
        return api.get("test.admin_user");
    }

    @Value("${groupings.api.test.surname}") private String TEST_SN;
    public String getTestSn() {
        return api.get("test.surname");
    }

    @Value("${groupings.api.test.given_name}") private String TEST_GIVEN_NAME;
    public String getTestGivenName() {
        return api.get("test.given_name");
    }

    @Value("${groupings.api.googlegroup}") private String GOOGLE_GROUP;
    public String getGoogleGroup() {
        return api.get("googlegroup");
    }

    @Value("${groupings.api.attribute_assign_id_size}") private Integer ATTRIBUTES_ASSIGN_ID_SIZE;
    public Integer getAttributesAssignIdSize() {
        return Integer.parseInt(api.get("attribute_assign_id_size"));
    }

    @Value("${groupings.api.composite_type.complement}") private String COMPOSITE_TYPE_COMPLEMENT;
    public String getCompositeTypeComplement() {
        return api.get("composite_type.complement");
    }

    @Value("${groupings.api.composite_type.intersection}") private String COMPOSITE_TYPE_INTERSECTION;
    public String getCompositeTypeIntersection() {
        return api.get("composite_type.intersection");
    }

    @Value("${groupings.api.composite_type.union}") private String COMPOSITE_TYPE_UNION;
    public String getCompositeTypeUnion() {
        return api.get("composite_type.union");
    }
}
