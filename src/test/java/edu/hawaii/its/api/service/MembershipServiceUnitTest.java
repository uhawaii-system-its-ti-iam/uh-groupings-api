package edu.hawaii.its.api.service;

import static org.junit.Assert.assertTrue;

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
        List<WsDeleteMemberResults> memberResults = new ArrayList<>();
        //Creating 5 groupPaths.
        for (int i = 0; i < 5; i++) {
            groupPaths.add("groupPath-" + i);
            //Creating 5 users to batch delete from those 5 groups.
        }
        for (int j = 0; j < 5; j++) {
            String userToRemove = "userToRemove-" + j;
            memberResults.addAll(membershipService.makeWsBatchDeleteMemberResults(groupPaths, userToRemove));
        }

        //Validates non-concurrent part of threading proving that the amount of member results returned equals the amount of deletes that occurred.
        assertTrue(memberResults.size() == 25);

        //Validates that every returned result is a memberResult, proving all of the threads are working properly.
        memberResults.forEach(
                result -> assertTrue((result.toString()).contains("edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults@")));
    }
}