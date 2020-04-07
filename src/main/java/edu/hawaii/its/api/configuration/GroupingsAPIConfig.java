package edu.hawaii.its.api.configuration;

import com.sun.istack.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix="groupings.api")
@Validated
public class GroupingsAPIConfig {

    //MemberAttributeServiceImpl
    @NotNull
    private String SETTINGS = "${groupings.api.settings}";
    @NotNull
    private String ADMIN = "${groupings.api.test.admin_user}";
    @NotNull
    private String GROUPING_SUPERUSERS = "${groupings.api.grouping_superusers}";
    @NotNull
    private String ATTRIBUTES = "${groupings.api.attributes}";
    @NotNull
    private String FOR_GROUPS = "${groupings.api.for_groups}";
    @NotNull
    private String FOR_MEMBERSHIPS = "${groupings.api.for_memberships}";
    @NotNull
    private String LAST_MODIFIED = "${groupings.api.last_modified}";
    @NotNull
    private String YYYYMMDDTHHMM = "${groupings.api.yyyymmddThhmm}";
    @NotNull
    private String UHGROUPING = "${groupings.api.uhgrouping}";
    @NotNull
    private String DESTINATIONS = "${groupings.api.destinations}";
    @NotNull
    private String TRIO = "${groupings.api.trio}";
    @NotNull
    private String PURGE_GROUPING = "${groupings.api.purge_grouping}";
    @NotNull
    private String SELF_OPTED = "${groupings.api.self_opted}";
    @NotNull
    private String ANYONE_CAN = "${groupings.api.anyone_can}";
    @NotNull
    private String OPT_IN = "${groupings.api.opt_in}";
    @NotNull
    private String OPT_OUT = "${groupings.api.opt_out}";
    @NotNull
    private String BASIS = "${groupings.api.basis}";
    @NotNull
    private String BASIS_PLUS_INCLUDE = "${groupings.api.basis_plus_include}";
    @NotNull
    private String OWNERS_GROUP = "${groupings.api.grouping_owners}";
    @NotNull
    private String ASSIGN_TYPE_GROUP = "${groupings.api.assign_type_group}";
    @NotNull
    private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP ="${groupings.api.assign_type_immediate_membership}";
    @NotNull
    private String SUBJECT_ATTRIBUTE_NAME_UID = "${groupings.api.subject_attribute_name_uhuuid}";
    @NotNull
    private String OPERATION_ASSIGN_ATTRIBUTE = "${groupings.api.operation_assign_attribute}";
    @NotNull
    private String OPERATION_REMOVE_ATTRIBUTE = "${groupings.api.operation_remove_attribute}";
    @NotNull
    private String OPERATION_REPLACE_VALUES = "${groupings.api.operation_replace_values}";
    @NotNull
    private String PRIVILEGE_OPT_OUT = "${groupings.api.privilege_opt_out}";
    @NotNull
    private String PRIVILEGE_OPT_IN = "${groupings.api.privilege_opt_in}";
    @NotNull
    private String EVERY_ENTITY = "${groupings.api.every_entity}";
    @NotNull
    private String IS_MEMBER = "${groupings.api.is_member}";
    @NotNull
    private String SUCCESS = "${groupings.api.success}";
    @NotNull
    private String FAILURE = "${groupings.api.failure}";
    @NotNull
    private String SUCCESS_ALLOWED = "${groupings.api.success_allowed}";
    @NotNull
    private String STEM = "${groupings.api.stem}";
    @NotNull
    private String UID = "${groupings.api.person_attributes.username}";
    @NotNull
    private String FIRST_NAME ="${groupings.api.person_attributes.first_name}";
    @NotNull
    private String LAST_NAME = "${groupings.api.person_attributes.last_name}";
    @NotNull
    private String COMPOSITE_NAME = "${groupings.api.person_attributes.composite_name}";
    @NotNull
    private String INSUFFICIENT_PRIVILEGES = "${groupings.api.insufficient_privileges}";

    public String getSETTINGS(){ return SETTINGS; }

    public void setSETTINGS(String newVal){ this.SETTINGS = newVal; }

    public String getADMIN(){ return ADMIN; }

    public void setADMIN(String newVal){ this.ADMIN = newVal; }

    public String getGROUPING_SUPERUSERS(){ return GROUPING_SUPERUSERS; }

    public void setGROUPING_SUPERUSERS(String newVal){ this.GROUPING_SUPERUSERS = newVal; }

    public String getATTRIBUTES(){ return ATTRIBUTES; }

    public void setATTRIBUTES(String newVal){ this.ATTRIBUTES = newVal; }

    public String getFOR_GROUPS(){ return FOR_GROUPS; }

    public void setFOR_GROUPS(String newVal){ this.FOR_GROUPS = newVal; }

    public String getFOR_MEMBERSHIPS(){ return FOR_MEMBERSHIPS; }

    public void setFOR_MEMBERSHIPS(String newVal){ this.FOR_MEMBERSHIPS = newVal; }

    public String getLAST_MODIFIED(){ return LAST_MODIFIED; }

    public void setLAST_MODIFIED(String newVal){ this.LAST_MODIFIED = newVal; }

    public String getYYYYMMDDTHHMM(){ return YYYYMMDDTHHMM; }

    public void setYYYYMMDDTHHMM(String newVal){ this.YYYYMMDDTHHMM = newVal; }

    public String getUHGROUPING(){ return UHGROUPING; }

    public void setUHGROUPING(String newVal){ this.UHGROUPING = newVal; }

    public String getDESTINATIONS(){ return DESTINATIONS; }

    public void setDESTINATIONS(String newVal){ this.DESTINATIONS = newVal; }

    public String getTRIO(){ return TRIO; }

    public void setTRIO(String newVal){ this.TRIO = newVal; }

    public String getPURGE_GROUPING(){ return PURGE_GROUPING; }

    public void setPURGE_GROUPING(String newVal){ this.PURGE_GROUPING = newVal; }

    public String getSELF_OPTED(){ return SELF_OPTED; }

    public void setSELF_OPTED(String newVal){ this.SELF_OPTED = newVal; }

    public String getANYONE_CAN(){ return ANYONE_CAN; }

    public void setANYONE_CAN(String newVal){ this.ANYONE_CAN = newVal; }

    public String getOPT_IN(){ return OPT_IN; }

    public void setOPT_IN(String newVal){ this.OPT_IN = newVal; }

    public String getOPT_OUT(){ return OPT_OUT; }

    public void setOPT_OUT(String newVal){ this.OPT_OUT = newVal; }

    public String getBASIS(){ return BASIS; }

    public void setBASIS(String newVal){ this.BASIS = newVal; }

    public String getBASIS_PLUS_INCLUDE(){ return BASIS_PLUS_INCLUDE; }

    public void setBASIS_PLUS_INCLUDE(String newVal){ this.BASIS_PLUS_INCLUDE = newVal; }

    public String getOWNERS_GROUP(){ return OWNERS_GROUP; }

    public void setOWNERS_GROUP(String newVal){ this.OWNERS_GROUP = newVal; }

    public String getASSIGN_TYPE_GROUP(){ return ASSIGN_TYPE_GROUP; }

    public void setASSIGN_TYPE_GROUP(String newVal){ this.ASSIGN_TYPE_GROUP = newVal; }

    public String getASSIGN_TYPE_IMMEDIATE_MEMBERSHIP(){ return ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP; }

    public void setASSIGN_TYPE_IMMEDIATE_MEMBERSHIP(String newVal){ this.ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP = newVal; }

    public String getSUBJECT_ATTRIBUTE_NAME_UID(){ return SUBJECT_ATTRIBUTE_NAME_UID; }

    public void setSUBJECT_ATTRIBUTE_NAME_UID(String newVal){ this.SUBJECT_ATTRIBUTE_NAME_UID = newVal; }

    public String getOPERATION_ASSIGN_ATTRIBUTE(){ return OPERATION_ASSIGN_ATTRIBUTE; }

    public void setOPERATION_ASSIGN_ATTRIBUTE(String newVal){ this.OPERATION_ASSIGN_ATTRIBUTE = newVal; }

    public String getOPERATION_REMOVE_ATTRIBUTE(){ return OPERATION_REMOVE_ATTRIBUTE; }

    public void setOPERATION_REMOVE_ATTRIBUTE(String newVal){ this.OPERATION_REMOVE_ATTRIBUTE = newVal; }

    public String getOPERATION_REPLACE_VALUES(){ return OPERATION_REPLACE_VALUES; }

    public void setOPERATION_REPLACE_VALUES(String newVal){ this.OPERATION_REPLACE_VALUES = newVal; }

    public String getPRIVILEGE_OPT_OUT(){ return PRIVILEGE_OPT_OUT; }

    public void setPRIVILEGE_OPT_OUT(String newVal){ this.PRIVILEGE_OPT_OUT = newVal; }

    public String getPRIVILEGE_OPT_IN(){ return PRIVILEGE_OPT_IN; }

    public void setPRIVILEGE_OPT_IN(String newVal){ this.PRIVILEGE_OPT_IN = newVal; }

    public String getEVERY_ENTITY(){ return EVERY_ENTITY; }

    public void setEVERY_ENTITY(String newVal){ this.EVERY_ENTITY = newVal; }

    public String getIS_MEMBER(){ return IS_MEMBER; }

    public void setIS_MEMBER(String newVal){ this.IS_MEMBER = newVal; }

    public String getSUCCESS(){ return SUCCESS; }

    public void setSUCCESS(String newVal){ this.SUCCESS = newVal; }

    public String getFAILURE(){ return FAILURE; }

    public void setFAILURE(String newVal){ this.FAILURE = newVal; }

    public String getSUCCESS_ALLOWED(){ return SUCCESS_ALLOWED; }

    public void setSUCCESS_ALLOWED(String newVal){ this.SUCCESS_ALLOWED = newVal; }

    public String getSTEM(){ return STEM; }

    public void setSTEM(String newVal){ this.STEM = newVal; }

    public String getUID(){ return UID; }

    public void setUID(String newVal){ this.UID = newVal; }

    public String getFIRST_NAME(){ return FIRST_NAME; }

    public void setFIRST_NAME(String newVal){ this.FIRST_NAME = newVal; }

    public String getLAST_NAME(){ return LAST_NAME; }

    public void setLAST_NAME(String newVal){ this.LAST_NAME = newVal; }

    public String getCOMPOSITE_NAME(){ return COMPOSITE_NAME; }

    public void setCOMPOSITE_NAME(String newVal){ this.COMPOSITE_NAME = newVal; }

    public String getINSUFFICIENT_PRIVILEGES(){ return INSUFFICIENT_PRIVILEGES; }

    public void setINSUFFICIENT_PRIVILEGES(String newVal){ this.INSUFFICIENT_PRIVILEGES = newVal; }





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


    public String getGROUPING_ADMINS(){ return GROUPING_ADMINS; }

    public void setGROUPING_ADMINS(String newVal){ this.GROUPING_ADMINS = newVal; }

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

    public String getGIVEN_NAME(){ return GIVEN_NAME; }

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

}
