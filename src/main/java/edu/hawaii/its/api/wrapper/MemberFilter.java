package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;

public enum MemberFilter {
    ALL(WsMemberFilter.All),
    EFFECTIVE(WsMemberFilter.Effective),
    IMMEDIATE(WsMemberFilter.Immediate),
    NONIMMEDIATE(WsMemberFilter.NonImmediate),
    COMPOSITE(WsMemberFilter.Composite);

    private final WsMemberFilter filter;

    MemberFilter(WsMemberFilter wsMemberFilter) {
        this.filter = wsMemberFilter;
    }

    public WsMemberFilter value() {
        return filter;
    }
}
