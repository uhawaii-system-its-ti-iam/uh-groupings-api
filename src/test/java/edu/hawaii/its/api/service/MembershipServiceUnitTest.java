package edu.hawaii.its.api.service;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

public class MembershipServiceUnitTest {
    @Test
    public void batchDeleteOne() {
        GrouperFactoryService grouperService = new GrouperFactoryServiceImpl() {
            public WsDeleteMemberResults makeWsDeleteMemberResults(String group, String memberToDelete) {
                System.out.println("DELETING " + memberToDelete + " from " + group);
                return new WsDeleteMemberResults();
            }
        };
        MembershipServiceImpl membershipService = new MembershipServiceImpl();
        membershipService.setGrouperFactoryService(grouperService);
        List<String> groupPaths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            groupPaths.add("groupPath-" + i);
            for (int j = 0; j < 2; j++) {
                String userToRemove = "userToRemove-" + j;
                membershipService.makeWsBatchDeleteMemberResults(groupPaths, userToRemove);
            }
        }
    }
}