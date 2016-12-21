package GroupingsFunctions;


import APICalls.GetMemberships;

public class GetOwners{

    public GetOwners(String userName, String grouping){

        GetMemberships getMemberships = new GetMemberships(userName, grouping + ":basis+include");
        getMemberships.owners();
    }
}