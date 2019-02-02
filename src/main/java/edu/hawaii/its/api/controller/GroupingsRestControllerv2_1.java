package edu.hawaii.its.api.controller;

import edu.hawaii.its.api.service.*;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsHTTPException;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.MembershipAssignment;
import edu.hawaii.its.api.type.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@RestController
//todo Possibly tack on version number to Base RequestMapping?
// Will have to consider this for test code as well
// Possibly split legacy code into separate classes? Not sure how dangerous this is
@RequestMapping("/api/groupings/v2.1")
public class GroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(GroupingsRestControllerv2_1.class);

    @Value("${app.groupings.controller.uuid}")
    private String uuid;

    @Value("${app.iam.request.form}")
    private String requestForm;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private GroupingFactoryService groupingFactoryService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @PostConstruct
    public void init() {
        Assert.hasLength(uuid, "Property 'app.groupings.controller.uuid' is required.");
        logger.info("GroupingsRestController started.");
    }

    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @ResponseBody
    public String hello() {
        return "University of Hawaii Groupings API";
    }

    /**
     * Get all admins and groupings
     *
     * @return List of all admins and all groupings
     */
    @RequestMapping(value = "/adminsGroupings",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<AdminListsHolder> adminsGroupings(@RequestHeader("current_user") String currentUser) {
        logger.info("Entered REST adminsGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.adminLists(currentUser));
    }

    /**
     * Get a member's attributes based off username or id number
     *
     * @param uid: Username or id number of user to obtain attributes about
     * @return Map of user attributes
     */
    @RequestMapping(value = "/members/{uid}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> memberAttributes(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST memberAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getUserAttributes(currentUser, uid));
    }

    /**
     * Get a list of a groupings a user is in and can opt into
     *
     * @return List of members grouping memberships
     */
    @RequestMapping(value = "/members/{uid}/groupings",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<MembershipAssignment> memberGroupings(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST memberGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.getMembershipAssignment(currentUser, uid));
    }

    //todo Maybe come back to this using listOwned?

    /**
     * Get an owner's owned groupings by username or UH id number
     *
     * @param uid: Username of owner to get list of groupings they own
     * @return List of owner's owned groupings
     */
    @GetMapping("/owners/{uid}/groupings")
    @RequestMapping(value = "/owners/{uid}/groupings",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Grouping>> ownerGroupings(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST ownerGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.restGroupingsOwned(currentUser, uid));
    }

    /**
     * Get a specific grouping with potential for page, size, sortstring, or ascending/descending
     *
     * @param path: Path of specific grouping
     * @param page: Page of grouping to retrieve (starts at 1)
     * @param size: Size of page of grouping to retrieve
     * @param sortString: Page of grouping to retrieve
     * @param isAscending: Page of grouping to retrieve (starts at 1)
     * @return Grouping found at specified path
     */
//    @RequestMapping(value = "/groupings/{path}",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public ResponseEntity<Grouping> getGrouping(@RequestHeader("current_user") String currentUser,
//            @PathVariable String path,
//            @RequestParam(required = false) Integer page,
//            @RequestParam(required = false) Integer size,
//            @RequestParam(required = false) String sortString,
//            @RequestParam(required = false) Boolean isAscending) {
//        logger.info("Entered REST getGrouping...");
//        return ResponseEntity
//                .ok()
//                .body(groupingAssignmentService.getPaginatedGrouping(path, currentUser, page, size, sortString, isAscending));
//    }

    /**
     * Get a specific grouping
     *
     * @param path: Path of specific grouping
     * @return Grouping found at specified path
     */
        @RequestMapping(value = "/groupings/{path}",
                method = RequestMethod.GET,
                produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseBody
        public ResponseEntity<Grouping> getGrouping(@RequestHeader("current_user") String currentUser,
                @PathVariable String path) throws Exception {
            logger.info("Entered REST getGrouping...");
            return ResponseEntity
                    .ok()
                    .body(groupingAssignmentService.getGrouping(path, currentUser));
        }

    /**
     * Get a specific group
     *
     * @param path:        Path of specified parent grouping
     * @param componentId:
     * @return Group found as result
     */
    @RequestMapping(value = "/groupings/{path}/components/{componentId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Group> getGroup(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String componentId) throws Exception {
        logger.info("Entered REST getGroup...");
        if (componentId.equals("composite"))
            componentId = "";
        componentId = ":" + componentId;
        //        try {
        //            return ResponseEntity
        //                    .ok()
        //                    .body(groupingAssignmentService.getGroupMembers(currentUser, path, componentId));
        //        } catch (Exception e) {
        ////            return new ResponseEntity<Group>("504 error", HttpStatus.BAD_GATEWAY);
        //            return ResponseEntity.
        //                    status(HttpStatus.BAD_GATEWAY).
        //                    body(groupingAssignmentService.getGroupMembers(currentUser, path, componentId));
        //        }
        try {
            ResponseEntity re = ResponseEntity
                    .ok()
                    .body(groupingAssignmentService.getGroupMembers(currentUser, path, componentId));
            return re;
        } catch (GroupingsHTTPException ghe) {
            return new ResponseEntity(HttpStatus.GATEWAY_TIMEOUT);
        }
    }

    /**
     * Get a specific member of a group
     *
     * @param path:        Path of specified parent grouping
     * @param componentId:
     * @return Group found as result
     */
    @RequestMapping(value = "/groupings/{path}/components/{componentId}/members/{uid}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Person>> searchMembers(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String componentId,
            @PathVariable String uid) throws Exception {
        logger.info("Entered REST getMembers...");
        if (componentId.equals("composite"))
            componentId = "";
        String groupPath = path + ":" + componentId;
        return ResponseEntity
                .ok()
                .body(memberAttributeService.searchMembers(groupPath, uid));
    }

    /**
     * Get a specific grouping by page
     *
     * @param path: Path of specific grouping
     * @return Grouping found at specified path
     */
    //    @RequestMapping(value = "/groupings/{path}",
    //            params = { "page", "size" },
    //            method = RequestMethod.GET,
    //            produces = MediaType.APPLICATION_JSON_VALUE)
    //    @ResponseBody
    //    public ResponseEntity<Grouping> getPaginatedGrouping(@RequestHeader("current_user") String currentUser,
    //            @PathVariable String path,
    //            @RequestParam(value = "page") Integer page,
    //            @RequestParam(value = "size") Integer size) {
    //        logger.info("Entered REST getPaginatedGrouping...");
    //        return ResponseEntity
    //                .ok()
    //                .body(groupingAssignmentService.getPaginatedGrouping(path, currentUser, page, size));
    //    }
    //
    //    /**
    //     * Get a specific subset of members in a group based on a filter string
    //     *
    //     * @param path: Path of specific group
    //     * @return Members found in group that contains filterString
    //     */
    //    @RequestMapping(value = "/groupings/{path}",
    //            params = { "page", "size", "uid" },
    //            method = RequestMethod.GET,
    //            produces = MediaType.APPLICATION_JSON_VALUE)
    //    @ResponseBody
    //    public ResponseEntity<Group> getPaginatedAndFilteredGroup(@RequestHeader("current_user") String curentUser,
    //            @PathVariable String path,
    //            @RequestParam(value = "page") Integer page,
    //            @RequestParam(value = "size") Integer size,
    //            @RequestParam(value = "uid") String filterString) {
    //
    //        logger.info("Entered REST getPaginatedAndFilteredGroup...");
    //
    //        // todo
    //        throw new UnsupportedOperationException();
    //    }

    // todo Test mapping; subject to change

    /**
     * Get a group asynchronously
     *
     * @param path:        Path of specified parent grouping
     * @param componentId:
     * @return Group found as result
     */
    @RequestMapping(value = "/groupings/{path}/components/{componentId}/async",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> getAsyncMembers(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String componentId) throws Exception {
        logger.info("Entered REST getAsyncMembers...");
        if (componentId.equals("composite"))
            componentId = "";
        componentId = ":" + componentId;

        Future<Group> future = groupingAssignmentService.getAsynchronousMembers(currentUser, path, componentId);

        try {
            while (true) {
                if (future.isDone()) {
                    return ResponseEntity
                            .ok()
                            .body(future.get());
                } else {
                    logger.info("Processing...");
                }
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        return null;
        //        return ResponseEntity
        //                .ok()
        //                .body(future.get());
    }

    /**
     * Create a new admin
     *
     * @param uid: uid of admin to add
     * @return Information about results of the operation
     */
    @RequestMapping(value = "/admins/{uid}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupingsServiceResult> addNewAdmin(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST addNewAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.addAdmin(currentUser, uid));
    }

    /**
     * Create a new grouping
     * Not implemented yet, even REST API controller function might not be doable at this stage
     *
     * @param path: String containing the path of grouping
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> addNewGrouping(
            @RequestHeader("current_user") String currentUser, @PathVariable String path) {
        logger.info("Entered REST addNewGrouping");
        return ResponseEntity
                .ok()
                .body(groupingFactoryService.addGrouping(currentUser, path));
    }

    /**
     * Update grouping to add a new owner
     *
     * @param path: path of grouping to update
     * @param uid:  uid/uuid of new owner to add
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/owners/{uid}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupingsServiceResult> addOwner(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.assignOwnership(path, currentUser, uid));
    }

    /**
     * Update grouping to add new include member
     *
     * @param path: path of grouping to update
     * @param uid:  uid or uuid of member to add to include
     * @return Information about results of the operation
     */
    @RequestMapping(value = "/groupings/{path}/includeMembers/{uid}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> includeMembers(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST includeMembers...");
        path = path + INCLUDE;
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMember(currentUser, path, uid));
    }

    /**
     * Update grouping to add new exclude member
     *
     * @param path: path of grouping to update
     * @param uid:  uid or uuid of member to add to exclude
     * @return Information about results of the operation
     */
    @RequestMapping(value = "/groupings/{path}/excludeMembers/{uid}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> excludeMembers(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST excludeMembers...");
        path = path + EXCLUDE;
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMember(currentUser, path, uid));
    }

    /**
     * Update grouping to enable given preference
     *
     * @param path:         path of grouping to update
     * @param preferenceId: id of preference to update
     * @return Information about result of operation
     */
    @RequestMapping(value = "/groupings/{path}/preferences/{preferenceId}/enable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> enablePreference(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String preferenceId) {
        logger.info("Entered REST enablePreference");
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!OPT_IN.equals(preferenceId) && !OPT_OUT.equals(preferenceId) && !LISTSERV.equals(preferenceId)
                && !RELEASED_GROUPING.equals(preferenceId)) {
            throw new UnsupportedOperationException();
        } else {
            if (OPT_IN.equals(preferenceId)) {
                results = groupAttributeService.changeOptInStatus(path, currentUser, true);
            } else if (OPT_OUT.equals(preferenceId)) {
                results = groupAttributeService.changeOptOutStatus(path, currentUser, true);
            } else if (LISTSERV.equals(preferenceId)) {
                results.add(groupAttributeService.changeListservStatus(path, currentUser, true));
            } else {
                results.add(groupAttributeService.changeReleasedGroupingStatus(path, currentUser, true));
            }
        }
        return ResponseEntity
                .ok()
                .body(results);
    }

    /**
     * Update grouping to disable given preference
     *
     * @param path:         path of grouping to update
     * @param preferenceId: id of preference to update
     * @return Information about result of operation
     */
    @RequestMapping(value = "/groupings/{path}/preferences/{preferenceId}/disable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> disablePreference(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String preferenceId) {
        logger.info("Entered REST disablePreference");
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!OPT_IN.equals(preferenceId) && !OPT_OUT.equals(preferenceId) && !LISTSERV.equals(preferenceId)
                && !RELEASED_GROUPING.equals(preferenceId)) {
            throw new UnsupportedOperationException();
        } else {
            if (OPT_IN.equals(preferenceId)) {
                results = groupAttributeService.changeOptInStatus(path, currentUser, false);
            } else if (OPT_OUT.equals(preferenceId)) {
                results = groupAttributeService.changeOptOutStatus(path, currentUser, false);
            } else if (LISTSERV.equals(preferenceId)) {
                results.add(groupAttributeService.changeListservStatus(path, currentUser, false));
            } else {
                results.add(groupAttributeService.changeReleasedGroupingStatus(path, currentUser, false));
            }
        }
        return ResponseEntity
                .ok()
                .body(results);
    }

    /**
     * Delete an admin
     *
     * @param uid: uid or uuid of admin to delete
     * @return Information about results of the operation
     */
    @RequestMapping(value = "/admins/{uid}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupingsServiceResult> deleteNewAdmin(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST deleteNewAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteAdmin(currentUser, uid));
    }

    /**
     * Delete a grouping
     * Not implemented yet, even REST API controller function might not be doable at this stage
     *
     * @param path: path of grouping to delete
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> deleteNewGrouping(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path) {
        logger.info("Entered REST deleteNewGrouping");
        return ResponseEntity
                .ok()
                .body(groupingFactoryService.markGroupForPurge(currentUser, path));
    }

    /**
     * Delete a grouping owner
     *
     * @param path: path of grouping to modify
     * @param uid:  uid or uuid of owner to delete
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/owners/{uid}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupingsServiceResult> deleteOwner(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteOwner");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.removeOwnership(path, currentUser, uid));
    }

    /**
     * Remove grouping include member
     *
     * @param path: path of grouping to modify
     * @param uid:  uid or uuid of grouping include member to remove
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/includeMembers/{uid}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupingsServiceResult> deleteInclude(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteInclude");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteGroupMemberByUsername(currentUser, path + INCLUDE, uid));
    }

    /**
     * Remove grouping exclude member
     *
     * @param path: path of grouping to modify
     * @param uid:  uid or uuid of grouping exclude member to remove
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/excludeMembers/{uid}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupingsServiceResult> deleteExclude(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteExclude");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteGroupMemberByUsername(currentUser, path + EXCLUDE, uid));
    }

    //todo Implement when manager feature is implemented in v2.2
    // Should all return HTTP code 405
    //////////////////////////////////////////
    // PLANNED API FUNCTIONS (2.2)
    /////////////////////////////////////////

    /**
     * Get a manager's groupings (planned implementation for v2.2)
     *
     * @param uid: Username of manager to get list of groupings they manage
     * @return List of manager's groupings they manage
     */
    @RequestMapping(value = "/managers/{uid}/groupings",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView managerGroupings(@PathVariable String uid) {
        logger.info("Entered REST managerGroupings...");
        throw new UnsupportedOperationException();
        //todo implement method
    }

    /**
     * Demote an owner to manager
     *
     * @param path: path of grouping to update
     * @param uid:  uid of new owner to add
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/owners/{uid}/demote",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView demoteOwner(@RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST demoteOwner...");
        throw new UnsupportedOperationException();
        //todo Implement method
    }

    /**
     * Add a new manager
     *
     * @param path: path of grouping to update
     * @param uid:  uid of manager to add
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/managers/{uid}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView addManager(@RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST addManager");
        throw new UnsupportedOperationException();
        //todo Implement method
    }

    //todo Might be worth changing id to something more clear and self-explanatory

    /**
     * Enable a manager permission
     *
     * @param path: path of grouping to update
     * @param uid:  uid of manager to change permissions
     * @param id:   id of permission to enable
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/managers/{uid}/permissions/{id}/enable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView enableManagerPermission(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid, @PathVariable String id) {
        logger.info("Entered REST enableManagerPermission");
        throw new UnsupportedOperationException();
        //todo Implement method
    }

    /**
     * Disable a manager permissions
     *
     * @param path: path of grouping to update
     * @param uid:  uid of manager to change permissions
     * @param id:   id of permission to disable
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/managers/{uid}/permissions/{id}/disable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView disableManagerPermission(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid, @PathVariable String id) {
        logger.info("Entered REST disableManagerPermission");
        throw new UnsupportedOperationException();
        //todo Implement method
    }

    /**
     * Promote a manager to an owner
     *
     * @param path: path of grouping to update
     * @param uid:  uid of manager to promote
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/managers/{uid}/promote",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView promoteManager(@RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST promoteManager");
        throw new UnsupportedOperationException();
        //todo Implement method
    }

    /**
     * Remove a manager
     *
     * @param path: path of grouping to update
     * @param uid:  uid of manager to remove
     * @return Information about results of operation
     */
    @RequestMapping(value = "/groupings/{path}/managers/{uid}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView deleteManager(@RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteManager");
        throw new UnsupportedOperationException();
        //todo Implement method
    }

    /**
     * if the user is allowed to opt into the grouping
     * this will add them to the include group of that grouping
     * if the user is in the exclude group, they will be removed from it
     *
     * @param path : the path to the grouping where the user will be opting in
     * @param uid  : the uid of the user that will be opted in
     * @return information about the success of opting in
     */
    @RequestMapping(value = "/groupings/{path}/includeMembers/{uid}/self",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> optIn(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST optIn...");
        return ResponseEntity
                .ok()
                .body(membershipService.optIn(currentUser, path, uid));
    }

    /**
     * if the user is allowed to opt out of the grouping
     * this will add them to the exclude group of that grouping
     * if the user is in the include group of that Grouping, they will be removed from it
     *
     * @param path : the path to the grouping where the user will be opting out
     * @param uid  : the uid of the user that will be opted out
     * @return information about the success of opting out
     */
    @RequestMapping(value = "/groupings/{path}/excludeMembers/{uid}/self",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> optOut(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST optOut...");
        return ResponseEntity
                .ok()
                .body(membershipService.optOut(currentUser, path, uid));
    }
}
