package GroupingsFunctions;

import APICalls.RemoveMembership;

public class DeleteMember{

    public DeleteMember(String userName, String grouping, String userToDelete){
        RemoveMembership removeMembership = new RemoveMembership(userToDelete, grouping);
    }
}