package APICalls;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetGroupsRequest;

public class GetGroups extends GrouperFunction{

    private GcGetGroups gcGetGroups;

    public GetGroups(String userName, String grouping){

        super(userName, grouping);
        groups();
        }


    public WsGetGroupsResults groups() {

        WsGetGroupsResults wsGetGroupsResults = new GcGetGroups().addSubjectIdentifier(userName).execute();
        System.out.println(wsGetGroupsResults.getResults()[0].getWsSubject().getName());
        return wsGetGroupsResults;
    }

//TODO set last modified attribute
}