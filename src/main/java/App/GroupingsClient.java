package App;

import APICalls.GrouperFunction;
import GroupingsFunctions.*;

/**
 * Created by zac on 12/12/16.
 */
public class GroupingsClient extends GrouperFunction{

    public GroupingsClient(String userName, String grouping){
        super(userName, grouping);
    }

    public void addGrouping(String newGrouping){
        AddGrouping addGrouping = new AddGrouping(getUserName(), getGrouping(), newGrouping);
    }

    public void addMember(String userToAdd){
        AddMember addMember = new AddMember(getUserName(), getGrouping(), userToAdd);
    }

    public void assignOwnership(String newOwner){
        AssignOwnership assignOwnership = new AssignOwnership(getUserName(), getGrouping(), newOwner);
    }

    public void deleteGrouping(){

    }

    public void deleteMember(String userToDelete){
        DeleteMember deleteMember = new DeleteMember(getUserName(), getGrouping(), userToDelete);
    }

    public void removeOwnership(String userToRemove){
        RemoveOwnership removeOwnership = new RemoveOwnership(getUserName(), getGrouping(), userToRemove);
    }

    public void getMembers(){
        GetMembers getMembers = new GetMembers(getUserName(), getGrouping());
    }

    public void getOwners(){
        GetOwners getOwners = new GetOwners(getUserName(), getGrouping());
    }

    public void groupingsImIn(){
        GroupingsImIn groupingsImIn = new GroupingsImIn(getUserName(), getGrouping());
    }

    public void groupingsIOwn(){
        GroupingsIOwn groupingsIOwn = new GroupingsIOwn(getUserName(), getGrouping());
    }
}
