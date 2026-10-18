package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGetMemberships;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;

public class GetMembershipsCommand
        extends GrouperCommand<GetMembershipsCommand>
        implements Command<GetMembershipsResults> {

    private final GcGetMemberships gcGetMemberships;

    public GetMembershipsCommand() {
        this.gcGetMemberships = new GcGetMemberships()
                .assignContentType("text/x-json");
    }

    @Override
    public GetMembershipsResults execute() {
        return new GetMembershipsResults(gcGetMemberships.execute());
    }

    @Override
    protected GetMembershipsCommand self() {
        return this;
    }

    public GetMembershipsCommand addGroup(String groupPath) {
        gcGetMemberships.addGroupName(groupPath);
        return this;
    }

    public GetMembershipsCommand addSubject(String uhIdentifier) {
        gcGetMemberships.addWsSubjectLookup(subjectLookup(uhIdentifier));
        return this;
    }

    public GetMembershipsCommand immediateOnly() {
        gcGetMemberships.assignMemberFilter(WsMemberFilter.Immediate);
        return this;
    }
}
