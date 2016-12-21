package GroupingsFunctions;

import APICalls.GetMemberships;

public class GetMembers{
    public GetMembers(String userName, String grouping){

        GetMemberships getMemberships = new GetMemberships(userName, grouping + ":basis+include");
        getMemberships.members();
    }

}