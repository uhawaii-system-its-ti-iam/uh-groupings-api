package App;

import APICalls.GrouperFunction;
import GroupingsFunctions.*;
import edu.internet2.middleware.grouperClient.api.GcAddMember;
import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zac on 12/12/16.
 */

@RestController
public class GroupingsClient{

    public GroupingsClient(String userName, String grouping){
    }

    @RequestMapping("/addGrouping")
    public WsGroupSaveResults addGrouping(@RequestParam(required = true) String newGrouping){
    }

    @RequestMapping("/addMember")
    public WsAddMemberResults addMember(@RequestParam(required = true) String grouping, @RequestParam(required = true) String userToAdd){
        new GcDeleteMember().assignGroupName(grouping + ":exclude").addSubjectIdentifier(userName).execute();
        return new GcAddMember().assignGroupName(grouping + ":include").addSubjectIdentifier(userName).execute();
    }

    @RequestMapping("/assignOwnership")
    public WsAssignGrouperPrivilegesResults assignOwnership(String newOwner){
    }

    @RequestMapping("/deleteGrouping")
    public WsGroupDeleteResults deleteGrouping(){
    }

    @RequestMapping("/deleteMember")
    public WsDeleteMemberResults deleteMember(String userToDelete){
    }

    @RequestMapping("/removeOwnership")
    public WsAssignGrouperPrivilegesResults removeOwnership(String userToRemove){
    }

    @RequestMapping("/getMembers")
    public WsGetMembersResults getMembers(){
    }

    @RequestMapping("/getOwners")
    public WsGetPermissionAssignmentsResults getOwners(){
        //have to check if this is the right Type to return
    }

    @RequestMapping("/groupingsImIn")
    public WsGetGroupsResults groupingsImIn(){
    }

    @RequestMapping("/groupingsIOwn")
    public WsGetPermissionAssignmentsResults groupingsIOwn(){
        //have to check if this is the right Type to return
    }
}
