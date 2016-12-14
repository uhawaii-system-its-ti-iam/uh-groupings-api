package GroupingsFunctions;

import APICalls.AssignMembership;

public class AddMember{

   public AddMember(String userName, String grouping, String userToAdd){
        AssignMembership addMember = new APICalls.AssignMembership(userToAdd, grouping);
    }
}