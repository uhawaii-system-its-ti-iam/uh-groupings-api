package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.groupings.GroupingGroupMember;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

public class GetMembersResultTest {

    @Test
    public void getSubjectsIncludesOrphanMember() {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("test:mtest102-l:mtest102-l:include");

        WsSubject orphan = new WsSubject();
        orphan.setId("25528222");
        orphan.setName("");
        orphan.setResultCode("SUCCESS");
        orphan.setAttributeValues(new String[]{"", "", "", "", "", "", "", "", ""});

        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResult.setWsGroup(wsGroup);
        wsGetMembersResult.setWsSubjects(new WsSubject[]{orphan});

        GetMembersResult getMembersResult = new GetMembersResult(wsGetMembersResult);
        List<Subject> subjects = getMembersResult.getSubjects();

        assertEquals(1, subjects.size());
        assertEquals("25528222", subjects.get(0).getUhUuid());
        assertEquals("", subjects.get(0).getName());
        assertFalse(subjects.get(0).hasUHAttributes());
        assertTrue(subjects.get(0).isOrphan());
    }

    @Test
    public void groupingGroupMemberMarksEntityNotFoundNameAsOrphan() {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("test:mtest102-l:mtest102-l:include");

        WsSubject orphan = new WsSubject();
        orphan.setId("25528222");
        orphan.setName("25528222 entity not found");
        orphan.setResultCode("SUCCESS");
        orphan.setAttributeValues(new String[]{"", "", "", "", "", "", "", "", ""});

        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResult.setWsGroup(wsGroup);
        wsGetMembersResult.setWsSubjects(new WsSubject[]{orphan});

        GroupingGroupMembers groupingGroupMembers =
                new GroupingGroupMembers(new GetMembersResult(wsGetMembersResult));
        GroupingGroupMember member = groupingGroupMembers.getMembers().get(0);

        assertEquals("25528222 entity not found", member.getName());
        assertTrue(member.isOrphan());
    }

    @Test
    public void getSubjectsExcludesMemberWithNoUhUuidAndNoAttributes() {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("test:mtest102-l:mtest102-l:include");

        WsSubject noIdentity = new WsSubject();
        noIdentity.setResultCode("SUCCESS");
        noIdentity.setAttributeValues(null);

        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResult.setWsGroup(wsGroup);
        wsGetMembersResult.setWsSubjects(new WsSubject[]{noIdentity});

        GetMembersResult getMembersResult = new GetMembersResult(wsGetMembersResult);
        assertTrue(getMembersResult.getSubjects().isEmpty());
    }

}
