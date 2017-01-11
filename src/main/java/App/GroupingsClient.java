package App;

import edu.internet2.middleware.grouperClient.api.*;
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

    /**
     * eventually this is intended to give the user the ability to add a Grouping in one of the Groupings that they own,
     * for now it will send a request to the UH Grouper staff with the users ID, parent Grouping for the new Grouping
     * and the name of the Grouping to be created
     *
     * @param grouping: String containing the path of the parent Grouping
     * @param newGrouping: String containing the name of the Grouping to be created
     * @return
     */
    @RequestMapping("/addGrouping")
    public WsGroupSaveResults addGrouping(@RequestParam String grouping, @RequestParam String newGrouping){
        //TODO
        //currently this method is not to be implemented because responsibility to create a new
        //grouping is still going to go through the UH Grouper staff
        return null;
    }

    /**
     * adds a member to a Grouping that the user owns
     * @param grouping: String containing the path of the Grouping
     * @param userToAdd
     * @return
     */
    @RequestMapping("/addMember")
    public WsAddMemberResults addMember(@RequestParam String grouping, @RequestParam String userToAdd){
        new GcDeleteMember().assignGroupName(grouping + ":exclude").addSubjectIdentifier(userToAdd).execute();
        return new GcAddMember().assignGroupName(grouping + ":include").addSubjectIdentifier(userToAdd).execute();
    }

    /**
     * gives the user read, update and view privileges for the Grouping
     * @param newOwner: String containing the username of the new owner
     * @return
     */
    @RequestMapping("/assignOwnership")
    public WsAssignGrouperPrivilegesResults assignOwnership(String newOwner){
        //TODO
        return null;
    }

    /**
     * removes a Grouping
     * @return
     */
    @RequestMapping("/deleteGrouping")
    public WsGroupDeleteResults deleteGrouping(){
        //TODO
        return null;
    }

    /**
     * removes a member from a Grouping that the user is an owner of
     * @param grouping: String containing the path of the Grouping
     * @param userToDelete: String containing the username of the user to be removed from the Grouping
     * @return
     */
    @RequestMapping("/deleteMember")
    public WsDeleteMemberResults deleteMember(@RequestParam String grouping, @RequestParam String userToDelete){
        new GcAddMember().assignGroupName(grouping + ":exclude").addSubjectIdentifier(userToDelete).execute();
        return new GcDeleteMember().assignGroupName(grouping + ":include").addSubjectIdentifier(userToDelete).execute();
    }

    /**
     * removes ownership privileges from the user specified
     * @param userToRemove: String containing the name of the user who's privileges will be removed
     * @return
     */
    @RequestMapping("/removeOwnership")
    public WsAssignGrouperPrivilegesResults removeOwnership(String userToRemove){
        //TODO
        return null;
    }

    /**
     * finds all the members of a group
     * @param grouping: String containing the path of the Grouping to be searched
     * @return
     */
    @RequestMapping("/getMembers")
    public WsGetMembersResults getMembers(@RequestParam String grouping){
        return new GcGetMembers().addGroupName(grouping).execute();
    }

    /**
     * finds all of the owners of a group
     * @return
     */
    @RequestMapping("/getOwners")
    public WsGetPermissionAssignmentsResults getOwners(){
        //TODO
        //have to check if this is the right Type to return
        return null;
    }

    /**
     * finds the different Groupings that the user is in and allowed to view
     * @param username: String containing the username to be searched for
     * @return
     */
    @RequestMapping("/groupingsImIn")
    public WsGetGroupsResults groupingsImIn(@RequestParam String username){
        return new GcGetGroups().addSubjectIdentifier(username).execute();
    }

    /**
     * finds the different Groupings that the user has owner privileges for
     * @return
     */
    @RequestMapping("/groupingsIOwn")
    public WsGetPermissionAssignmentsResults groupingsIOwn(){
        //Todo
        //have to check if this is the right Type to return
        return null;
    }
}
