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

    private final Map<String, String> api = new HashMap<>();

    public Map<String, String> getApi() { return api; }

    public String getSettings() { return api.get("settings");}

    public String getBasis() {return api.get("basis");}

    public String getBasisPlusInclude() {return api.get("basis_plus_include");}

    public String getExclude() {return api.get("exclude");}

    public String getInclude() {return api.get("include");}

    public String getOwners() {return api.get("owners");}

    public String getAssignTypeGroup() {return api.get("assign_type_group");}

    public String getAssignTypeImmediateMembership() {return api.get("assign_type_immediate_membership");}

    public String getSubjectAttributeNameUid() {return api.get("subject_attribute_name_uhuuid");}

    public String getOperationAssignAttribute() {return api.get("operation_assign_attribute");}

    public String getOperationRemoveAttribute() {return api.get("operation_remove_attribute");}

    public String getOperationReplaceValues() {return api.get("operation_replace_values");}

    public String getPrivilegeOptOut() {return api.get("privilege_opt_out");}

    public String getPrivilegeOptIn() {return api.get("privilege_opt_in");}

    public String getEveryEntity() {return api.get("every_entity");}

    public String getIsMember() {return api.get("is_member");}

    public String getSuccess() {return api.get("success");}

    public String getFailure() {return api.get("failure");}

    public String getStem() { return api.get("stem"); }

    public String getUid() {
        return api.get("person_attributes.username");
    }

    public String getFirstName() { return api.get("person_attributes.first_name"); }

    public String getLastName() {
        return api.get("person_attributes.last_name");
    }

    public String getCompositeName() { return api.get("person_attributes.composite_name"); }

    public String getInsufficientPrivileges() { return api.get("insufficient_privileges"); }

}
