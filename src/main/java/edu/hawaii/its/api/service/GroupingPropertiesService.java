package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class GroupingPropertiesService {


    @Autowired
    @Qualifier("grouperService")
    private GrouperService grouperService;

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

    public GrouperService getGrouperService() {
        GrouperService grouperService1 = grouperService;
        return grouperService1;
    }
    public HasMembersResults getHasMembersResultsBean() {
        return this.hasMembersResults;
    }
    public FindGroupsResults getFindGroupsResults() {
        return this.findGroupsResults;
    }
    public SubjectsResults getSubjectsResults(){
        return this.subjectsResults;
    }
    public GroupSaveResults  getGroupSaveResults() { return this.groupSaveResults; }
    public AssignAttributesResults getAssignAttributesResults() { return this.assignAttributesResults; }
    public GetMembersResults getMembersResults() { return this.getMembersResults; }

}

