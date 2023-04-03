package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

import java.util.List;

/**
 * A wrapper for GcGetMembers. When groupPath(s) are passed, GetMembersCommand on execute fetches, from grouper, results
 * containing all the members of that group at groupPath.
 */
public class GetMembersCommand extends GrouperCommand implements Command<GetMembersResults> {

    private final GcGetMembers gcGetMembers;

    public GetMembersCommand() {
        gcGetMembers = new GcGetMembers();
        gcGetMembers.assignIncludeSubjectDetail(true);
    }

    @Override public GetMembersResults execute() {
        WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
        JsonUtil.printJson(wsGetMembersResults);
        return new GetMembersResults(wsGetMembersResults);
    }

    public GetMembersCommand addGroupPaths(List<String> groupPaths) {
        for (String groupPath : groupPaths) {
            gcGetMembers.addGroupName(groupPath);
        }
        return this;
    }

    public GetMembersCommand addGroupPath(String groupPath) {
        gcGetMembers.addGroupName(groupPath);
        return this;
    }

    public GetMembersCommand setPageNumber(Integer pageNumber) {
        gcGetMembers.assignPageNumber(pageNumber);
        return this;
    }

    public GetMembersCommand setPageSize(Integer pageSize) {
        gcGetMembers.assignPageSize(pageSize);
        return this;
    }

    public GetMembersCommand setAscending(boolean isAscending) {
        gcGetMembers.assignAscending(isAscending);
        return this;
    }

    public GetMembersCommand sortBy(String sortString) {
        gcGetMembers.assignSortString(sortString);
        return this;
    }
}
