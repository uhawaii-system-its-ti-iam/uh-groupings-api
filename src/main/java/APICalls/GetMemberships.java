package APICalls;

import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;

public class GetMemberships extends GrouperFunction{
    WsGetMembersResults wsGetMembersResults;

  public GetMemberships(String userName, String grouping){

        super(userName, grouping);

    }

    public WsGetMembersResults members(){
        wsGetMembersResults = new GcGetMembers().addGroupName(grouping).execute();

        System.out.println(wsGetMembersResults.getResults()[0].getWsGroup().getName());

        return wsGetMembersResults;
    }

    public WsGetMembersResults owners(){

        wsGetMembersResults = new GcGetMembers().addGroupName(grouping).addSubjectAttributeName("owner").execute();

        System.out.println(wsGetMembersResults.getResults()[0].getWsGroup().getName());

        return wsGetMembersResults;
    }
//TODO set last modified attribute
}