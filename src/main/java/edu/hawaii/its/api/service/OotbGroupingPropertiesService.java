package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OotbGroupingPropertiesService {


    @Autowired
    @Qualifier("HasMembersResultsOOTBBean")
    private HasMembersResults hasMembersResults;

    @Autowired
    @Qualifier("FindGroupsResultsOOTBBean")
    private FindGroupsResults findGroupsResults;

    @Autowired
    @Qualifier("GetSubjectsResultsOOTBBean")
    private SubjectsResults subjectsResults;

    @Autowired
    @Qualifier("GroupSaveResultsOOTBBean")
    private GroupSaveResults groupSaveResults;

    @Autowired
    @Qualifier("AssignAttributesOOTBBean")
    private AssignAttributesResults assignAttributesResults;

    @Autowired
    @Qualifier("GetMembersResultsOOTBBean")
    private GetMembersResults getMembersResults;

    @Autowired
    @Qualifier("AddMemberResultsOOTBBean")
    private AddMembersResults addMembersResults;

    @Autowired
    @Qualifier("RemoveMembersResultsOOTBBean")
    private RemoveMembersResults removeMembersResults;

    @Autowired
    @Qualifier("AttributeAssignmentResultsOOTBBean")
    private GroupAttributeResults groupAttributeResults;

    @Autowired
    @Qualifier("GetGroupsResultsOOTBBean")
    private GetGroupsResults getGroupsResults;

    public HasMembersResults getHasMembersResultsBean() {
        return this.hasMembersResults;
    }
    public FindGroupsResults getFindGroupsResults() {
        return this.findGroupsResults;
    }

    public SubjectsResults getSubjectsResults() {
        return this.subjectsResults;
    }

    public GroupSaveResults getGroupSaveResults() {
        return this.groupSaveResults;
    }

    public AssignAttributesResults getAssignAttributesResults() {
        return this.assignAttributesResults;
    }
    public GetMembersResults getMembersResults() {
        return this.getMembersResults;
    }

    public AddMembersResults getAddMembersResults() {
        return this.addMembersResults;
    }

    public RemoveMembersResults getRemoveMembersResults() {
        return removeMembersResults;
    }

    public GroupAttributeResults getGroupAttributeResults() {
        return groupAttributeResults;
    }

    public GetGroupsResults getGroupsResults() {
        return getGroupsResults;
    }

}

