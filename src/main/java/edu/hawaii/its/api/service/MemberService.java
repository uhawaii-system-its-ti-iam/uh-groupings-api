package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersCommand;
import edu.hawaii.its.api.wrapper.HasMembersResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    @Autowired
    ExecutorService grouperApi;

    private static final String SUCCESS = "SUCCESS";

    public boolean isAdmin(String uhIdentifier) {
        return isMember(GROUPING_ADMINS, uhIdentifier);
    }

    public boolean isOwner(String uhIdentifier) {
        return isMember(OWNERS_GROUP, uhIdentifier);
    }

    public boolean isOwner(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.OWNERS.value(), uhIdentifier);
    }

    public boolean isInclude(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.INCLUDE.value(), uhIdentifier);
    }

    public boolean isExclude(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.EXCLUDE.value(), uhIdentifier);
    }

    public boolean isMember(String groupPath, String uhIdentifier) {
        HasMemberResult hasMemberResult = memberResult(groupPath, uhIdentifier);
        return hasMemberResult.getResultCode().equals("IS_MEMBER");
    }

    private HasMemberResult memberResult(String groupingPath, String uhIdentifier) {
        HasMembersResults hasMembersResults = grouperApi.execute(new HasMembersCommand()
                .assignGroupPath(groupingPath)
                .addUhIdentifier(uhIdentifier));
        return hasMembersResults.getResult();
    }
}
