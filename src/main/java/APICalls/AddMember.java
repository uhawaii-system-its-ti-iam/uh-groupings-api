package APICalls;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

/**
 * Created by zac on 12/12/16.
 */
public class AddMember extends GrouperFunction{


    AddMember(String userName, String grouping) {
        super(userName, grouping);
    }

    private void addTheMember() {
        WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupName(grouping + ":include").addSubjectIdentifier(userName).execute();
        System.out.println(wsAddMemberResults.getResults()[0].getWsSubject().getName());

        WsDeleteMemberResults wsDeleteMemberResults = new GcDeleteMember().assignGroupName(grouping + ":exclude").addSubjectIdentifier(userName).execute();
        System.out.println(wsDeleteMemberResults.getResults()[0].getWsSubject().getName());

    }
}