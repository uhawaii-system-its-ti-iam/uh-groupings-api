package edu.hawaii.its.api.wrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;

public class MemberFilterTest {
    MemberFilter memberFilter;

    @Test
    public void allFilter() {
        memberFilter = MemberFilter.ALL;
        assertThat(memberFilter.value(), equalTo(WsMemberFilter.All));
    }

    @Test
    public void effectiveFilter() {
        memberFilter = MemberFilter.EFFECTIVE;
        assertThat(memberFilter.value(), equalTo(WsMemberFilter.Effective));
    }

    @Test
    public void immediateFilter() {
        memberFilter = MemberFilter.IMMEDIATE;
        assertThat(memberFilter.value(), equalTo(WsMemberFilter.Immediate));
    }

    @Test
    public void nonimmediateFilter() {
        memberFilter = MemberFilter.NONIMMEDIATE;
        assertThat(memberFilter.value(), equalTo(WsMemberFilter.NonImmediate));
    }

    @Test
    public void compositeFilter() {
        memberFilter = MemberFilter.COMPOSITE;
        assertThat(memberFilter.value(), equalTo(WsMemberFilter.Composite));
    }
}
