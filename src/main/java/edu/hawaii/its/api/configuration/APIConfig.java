package edu.hawaii.its.api.configuration;

import com.sun.istack.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix="groupings.api")
@Validated
public class APIConfig {

    //application.properties
    /*@NotNull private String settings;
    @NotNull private String attribute_assign_id_size;
    @NotNull private String grouping_admins;
    @NotNull private String grouping_apps;
    @NotNull private String grouping_owners;
    @NotNull private String grouping_superusers;
    @NotNull private String attributes;
    @NotNull private String for_groups;
    @NotNull private String for_memberships;
    @NotNull private String last_modified;
    @NotNull private String yyyymmddThhmm;
    @NotNull private String uhgrouping;
    @NotNull private String destinations;
    @NotNull private String listserv;
    @NotNull private String releasedgrouping;
    @NotNull private String googlegroup;
    @NotNull private String trio;
    @NotNull private String purge_grouping;
    @NotNull private String self_opted;
    @NotNull private String anyone_can;
    @NotNull private String opt_in;
    @NotNull private String opt_out;
    @NotNull private String basis;
    @NotNull private String basis_plus_include;
    @NotNull private String exclude;
    @NotNull private String include;
    @NotNull private String owners;
    @NotNull private String assign_type_group;
    @NotNull private String assign_type_immediate_membership;
    @NotNull private String subject_attribute_name_uhuuid;
    @NotNull private String operation_assign_attribute;
    @NotNull private String operation_remove_attribute;
    @NotNull private String operation_replace_values;
    @NotNull private String privilege_opt_out;
    @NotNull private String privilege_opt_in;
    @NotNull private String every_entity;
    @NotNull private String is_member;
    @NotNull private String stale_subject_id;
    @NotNull private String stem;
    @NotNull private String success;
    @NotNull private String failure;
    @NotNull private String not_in_group;
    @NotNull private String success_allowed;
    @NotNull private String current_user;
    @NotNull private String timeout;
    @NotNull private String insufficient_privileges;*/

    //DatabaseSetupServiceImpl
    @NotNull
    private String GROUPING_ADMINS = "${groupings.api.grouping_admins}";
    @NotNull
    private String GROUPING_OWNERS = "${groupings.api.grouping_owners}";
    @NotNull
    private String GROUPING_APPS = "${groupings.api.grouping_apps}";
    @NotNull
    private String USERNAME = "${groupings.api.test.username}";
    @NotNull
    private String NAME = "${groupings.api.test.name}";
    @NotNull
    private String Basis = "${groupings.api.basis}";
    @NotNull
    private String EXCLUDE = "${groupings.api.exclude}";
    @NotNull
    private String INCLUDE = "${groupings.api.include}";
    @NotNull
    private String OWNERS = "${groupings.api.owners}";
    @NotNull
    private String UHUUID = "${groupings.api.test.uhuuid}";
    @NotNull
    private String SN = "${groupings.api.test.surname}";
    @NotNull
    private String GIVEN_NAME = "${groupings.api.test.given_name}";
    @NotNull
    private String RELEASED_GROUPING = "${groupings.api.releasedgrouping}";
    @NotNull
    private String LISTSERV = "${groupings.api.listserv}";
    @NotNull
    private String GOOGLE_GROUP = "${groupings.api.googlegroup}";


    public String getGROUPING_ADMINS(){
        return GROUPING_ADMINS;
    }

    public void setGROUPING_ADMINS(String newVal){
        this.GROUPING_ADMINS = newVal;
    }

    public String getGROUPING_OWNERS(){
        return GROUPING_OWNERS;
    }

    public void setGROUPING_OWNERS(String newVal){
        this.GROUPING_OWNERS = newVal;
    }

    public String getGROUPING_APPS(){
        return GROUPING_APPS;
    }

    public void setGROUPING_APPS(String newVal){
        this.GROUPING_APPS = newVal;
    }

    public String getUSERNAME(){
        return USERNAME;
    }

    public void setUSERNAME(String newVal){
        this.USERNAME = newVal;
    }

    public String getNAME(){
        return NAME;
    }

    public void setNAME(String newVal){
        this.NAME = newVal;
    }

    public String getBasis(){
        return Basis;
    }

    public void setBasis(String newVal){
        this.Basis = newVal;
    }

    public String getEXCLUDE(){
        return EXCLUDE;
    }

    public void setEXCLUDE(String newVal){
        this.EXCLUDE = newVal;
    }

    public String getINCLUDE(){
        return INCLUDE;
    }

    public void setINCLUDE(String newVal){
        this.INCLUDE = newVal;
    }

    public String getOWNERS(){
        return OWNERS;
    }

    public void setOWNERS(String newVal){
        this.OWNERS = newVal;
    }

    public String getUHUUID(){
        return UHUUID;
    }

    public void setUHUUID(String newVal){
        this.UHUUID = newVal;
    }

    public String getSN(){
        return SN;
    }

    public void setSN(String newVal){
        this.SN = newVal;
    }

    public String getGIVEN_NAME(){
        return GIVEN_NAME;
    }

    public void setGIVEN_NAME(String newVal){
        this.GIVEN_NAME = newVal;
    }

    public String getRELEASED_GROUPING(){
        return RELEASED_GROUPING;
    }

    public void setRELEASED_GROUPING(String newVal){
        this.RELEASED_GROUPING = newVal;
    }

    public String getLISTSERV(){
        return LISTSERV;
    }

    public void setLISTSERV(String newVal){
        this.LISTSERV = newVal;
    }

    public String getGOOGLE_GROUP(){
        return GOOGLE_GROUP;
    }

    public void setGOOGLE_GROUP(String newVal){
        this.GOOGLE_GROUP = newVal;
    }

    /*@NotNull private String GROUPING_ADMINS = "${groupings.api.grouping_admins}";
    @NotNull private String GROUPING_OWNERS = "${groupings.api.grouping_owners}";
    @NotNull private String GROUPING_APPS = "${groupings.api.grouping_apps}";
    @NotNull private String USERNAME = "${groupings.api.test.username}";
    @NotNull private String NAME = "${groupings.api.test.name}";
    @NotNull private String Basis = "${groupings.api.basis}";
    @NotNull private String EXCLUDE = "${groupings.api.exclude}";
    @NotNull private String INCLUDE = "${groupings.api.include}";
    @NotNull private String OWNERS = "${groupings.api.owners}";
    @NotNull private String UHUUID = "${groupings.api.test.uhuuid}";
    @NotNull private String SN = "${groupings.api.test.surname}";
    @NotNull private String GIVEN_NAME = "${groupings.api.test.given_name}";
    @NotNull private String RELEASED_GROUPING = "${groupings.api.releasedgrouping}";
    @NotNull private String LISTSERV = "${groupings.api.listserv}";
    @NotNull private String GOOGLE_GROUP = "${groupings.api.googlegroup}";*/

}
