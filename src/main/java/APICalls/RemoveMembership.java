package APICalls;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

/**
 * Created by zac on 12/12/16.
 */
public class RemoveMembership extends GrouperFunction{
    private String userName;
    private String grouping;

    public RemoveMembership(String userName, String grouping) {
        super(userName, grouping);
        removeMembership();
    }

    private void removeMembership() {
        WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupName(grouping + ":exclude").addSubjectIdentifier(userName).execute();
        System.out.println(wsAddMemberResults.getResults()[0].getWsSubject().getName());

        WsDeleteMemberResults wsDeleteMemberResults = new GcDeleteMember().assignGroupName(grouping + ":include").addSubjectIdentifier(userName).execute();
        System.out.println(wsDeleteMemberResults.getResults()[0].getWsSubject().getName());

    }
      //TODO set last modified attribute
}
