package edu.hawaii.its.api.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingFactoryService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingAssignment;
import edu.hawaii.its.api.type.GroupingsServiceResult;

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
    public ResponseEntity<AdminListsHolder> adminsGroupings(Principal principal) {
        logger.info("Entered REST adminsGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.adminLists(principal.getName()));
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
    public ResponseEntity<Map<String, String>> memberAttributes(Principal principal, @PathVariable String uid) {
        logger.info("Entered REST memberAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getUserAttributes(principal.getName(), uid));
    }

    /**
     * Get a list of a member's grouping memberships
     *
     * @param uid: Username or id number of member to get list of grouping memberships
     * @return List of members grouping memberships
     */
    @RequestMapping(value = "/members/{uid}/groupings",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<String>> memberGroupings(Principal principal, @PathVariable String uid) {
        logger.info("Entered REST memberGroupings...");
        return ResponseEntity
                .ok()
                .body(helperService.extractGroupings(groupingAssignmentService.getGroupPaths(principal.getName(), uid)));
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
    public ResponseEntity<List<Grouping>> ownerGroupings(Principal principal, @PathVariable String uid) {
        logger.info("Entered REST ownerGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.groupingsOwned(groupingAssignmentService.getGroupPaths(principal, uid)));
    }

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
    public ResponseEntity<Grouping> getGrouping(Principal principal, @PathVariable String path) {
        logger.info("Entered REST getGrouping...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.getGrouping(path, principal.getName()));
    }

    //todo Is this the way we want/can do this? Might be better to split GET calls based on Groups individually
    /**
     * Get a specific grouping
     *
     * @param path: Path of specific grouping
     * @return Grouping found at specified path
     */
    @RequestMapping(value = "/api/groupings/{path}/get",
            params = { "page", "size" },
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<Grouping> getPaginatedGrouping(Principal principal,
            @PathVariable String path,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size) {
        logger.info("Entered REST getPaginatedGrouping");
        throw new UnsupportedOperationException();
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
    public ResponseEntity<GroupingsServiceResult> addNewAdmin(Principal principal, @PathVariable String uid) {
        logger.info("Entered REST addNewAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.addAdmin(principal.getName(), uid));
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
    public ResponseEntity<List<GroupingsServiceResult>> addNewGrouping(Principal principal, @PathVariable String path) {
        logger.info("Entered REST addNewGrouping");
        return ResponseEntity
                .ok()
                .body(groupingFactoryService.addGrouping(principal.getName(), path));
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
    public ResponseEntity<GroupingsServiceResult> addOwner(Principal principal, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.assignOwnership(path, principal.getName(), uid));
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
    public ResponseEntity<List<GroupingsServiceResult>> includeMembers(Principal principal, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST includeMembers...");
        path = path + INCLUDE;
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMember(principal.getName(), path, uid));
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
    public ResponseEntity<List<GroupingsServiceResult>> excludeMembers(Principal principal, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST excludeMembers...");
        path = path + EXCLUDE;
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMember(principal.getName(), path, uid));
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
    public ResponseEntity<List<GroupingsServiceResult>> enablePreference(Principal principal, @PathVariable String path,
            @PathVariable String preferenceId) {
        logger.info("Entered REST enablePreference");
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!OPT_IN.equals(preferenceId) && !OPT_OUT.equals(preferenceId) && !LISTSERV.equals(preferenceId)
                && !RELEASED_GROUPING.equals(preferenceId)) {
            throw new UnsupportedOperationException();
        } else {
            if (OPT_IN.equals(preferenceId)) {
                results = groupAttributeService.changeOptInStatus(path, principal.getName(), true);
            } else if (OPT_OUT.equals(preferenceId)) {
                results = groupAttributeService.changeOptOutStatus(path, principal.getName(), true);
            } else if (LISTSERV.equals(preferenceId)) {
                results.add(groupAttributeService.changeListservStatus(path, principal.getName(), true));
            } else {
                results.add(groupAttributeService.changeReleasedGroupingStatus(path, principal.getName(), true));
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
    public ResponseEntity<List<GroupingsServiceResult>> disablePreference(Principal principal,
            @PathVariable String path,
            @PathVariable String preferenceId) {
        logger.info("Entered REST disablePreference");
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (!OPT_IN.equals(preferenceId) && !OPT_OUT.equals(preferenceId) && !LISTSERV.equals(preferenceId)
                && !RELEASED_GROUPING.equals(preferenceId)) {
            throw new UnsupportedOperationException();
        } else {
            if (OPT_IN.equals(preferenceId)) {
                results = groupAttributeService.changeOptInStatus(path, principal.getName(), false);
            } else if (OPT_OUT.equals(preferenceId)) {
                results = groupAttributeService.changeOptOutStatus(path, principal.getName(), false);
            } else if (LISTSERV.equals(preferenceId)) {
                results.add(groupAttributeService.changeListservStatus(path, principal.getName(), false));
            } else {
                results.add(groupAttributeService.changeReleasedGroupingStatus(path, principal.getName(), false));
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
    public ResponseEntity<GroupingsServiceResult> deleteNewAdmin(Principal principal, @PathVariable String uid) {
        logger.info("Entered REST deleteNewAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteAdmin(principal.getName(), uid));
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
    public ResponseEntity<List<GroupingsServiceResult>> deleteNewGrouping(Principal principal,
            @PathVariable String path) {
        logger.info("Entered REST deleteNewGrouping");
        return ResponseEntity
                .ok()
                .body(groupingFactoryService.deleteGrouping(principal.getName(), path));
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
    public ResponseEntity<GroupingsServiceResult> deleteOwner(Principal principal, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteOwner");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.removeOwnership(path, principal.getName(), uid));
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
    public ResponseEntity<GroupingsServiceResult> deleteInclude(Principal principal, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteInclude");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteGroupMemberByUsername(principal.getName(), path + INCLUDE, uid));
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
    public ResponseEntity<GroupingsServiceResult> deleteExclude(Principal principal, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteExclude");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteGroupMemberByUsername(principal.getName(), path + EXCLUDE, uid));
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
    public RedirectView demoteOwner(Principal principal, @PathVariable String path,
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
    public RedirectView addManager(Principal principal, @PathVariable String path, @PathVariable String uid) {
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
    public RedirectView enableManagerPermission(Principal principal, @PathVariable String path,
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
    public RedirectView disableManagerPermission(Principal principal, @PathVariable String path,
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
    public RedirectView promoteManager(Principal principal, @PathVariable String path, @PathVariable String uid) {
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
    public RedirectView deleteManager(Principal principal, @PathVariable String path, @PathVariable String uid) {
        logger.info("Entered REST deleteManager");
        throw new UnsupportedOperationException();
        //todo Implement method
    }
}
