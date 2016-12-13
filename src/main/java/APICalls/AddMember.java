package APICalls;

import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;

/**
 * Created by zac on 12/12/16.
 */
public class AddMember {
    private String identifier;
    private String grouping;

    AddMember(String identifier, String grouping) {
        this.identifier = identifier;
        this.grouping = grouping;
        addTheMember();
    }

    private void addTheMember() {
        WsAddMemberResults wsAddMemberResults = new GcAddMember().assignGroupName(grouping + ":include").addSubjectIdentifier(identifier).execute();
        System.out.println(wsAddMemberResults.getResults()[0].getWsSubject().getName());

        WsDeleteMemberResults wsDeleteMemberResults = new GcDeleteMember().assignGroupName(grouping + ":exclude").addSubjectIdentifier(identifier).execute();
        System.out.println(wsDeleteMemberResults.getResults()[0].getWsSubject().getName());

    }
}