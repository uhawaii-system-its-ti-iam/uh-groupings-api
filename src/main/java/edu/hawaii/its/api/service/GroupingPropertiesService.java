package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.FindGroupsResults;
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

    public GrouperService getGrouperService() {
        GrouperService grouperService1 = grouperService;
        return grouperService1;
    }
    public HasMembersResults getHasMembersResultsBean() {
        HasMembersResults hasMembersResults1 = hasMembersResults;
        return hasMembersResults1;
    }

    public FindGroupsResults getFindGroupsResults() {
        return this.findGroupsResults;
    }

    public SubjectsResults getSubjectsResults(){
        return this.subjectsResults;
    }
}

