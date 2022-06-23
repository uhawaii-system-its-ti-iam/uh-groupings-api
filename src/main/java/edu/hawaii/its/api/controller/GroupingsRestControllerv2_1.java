package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.RemoveMemberResult;
import edu.hawaii.its.api.type.SyncDestination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/api/groupings/v2.1")
public class GroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(GroupingsRestControllerv2_1.class);

    @Value("${app.groupings.controller.uuid}")
    private String uuid;

    @Value("${groupings.api.opt_in}")
    private String OPT_IN;

    @Value("${groupings.api.opt_out}")
    private String OPT_OUT;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    final private static String CURRENT_USER = "current_user";

    @PostConstruct
    public void init() {
        Assert.hasLength(uuid, "Property 'app.groupings.controller.uuid' is required.");
        logger.info("GroupingsRestController started.");
    }

    /**
     * Get a hello string, this is a test endpoint.
     */
    @GetMapping(value = "/")
    @ResponseBody
    public ResponseEntity<String> hello() {
        return ResponseEntity
                .ok()
                .body("University of Hawaii Groupings");
    }

    /**
     * Get a list of all admins and a list of all groupings.
     */
    @GetMapping(value = "/admins-and-groupings")
    @ResponseBody
    public ResponseEntity<AdminListsHolder> adminsGroupings(@RequestHeader(CURRENT_USER) String currentUser) {
        logger.info("Entered REST adminsGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.adminLists(currentUser));
    }

    /**
     * Create a new admin.
     */
    @PostMapping(value = "/admins/{uid:[\\w-:.]+}")
    public ResponseEntity<AddMemberResult> addAdmin(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST addAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.addAdmin(currentUser, uid));
    }

    /**
     * Remove an admin.
     */
    @DeleteMapping(value = "/admins/{uid:[\\w-:.]+}")
    public ResponseEntity<RemoveMemberResult> removeAdmin(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST removeAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.removeAdmin(currentUser, uid));
    }

    /**
     * Delete a user from multiple groupings.
     */
    @DeleteMapping(value = "/admins/{paths}/{uid}")
    public ResponseEntity<List<RemoveMemberResult>> removeFromGroups(
            @RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable List<String> paths,
            @PathVariable String uid) {
        logger.info("Entered REST removeFromGroups...");
        return ResponseEntity
                .ok()
                .body(membershipService.removeFromGroups(currentUser, uid, paths));
    }

    /**
     * Remove all members from basis, include, exclude.
     */
    @DeleteMapping(value = "/groupings/{path}/{include}/{exclude}/reset-group")
    public ResponseEntity<List<RemoveMemberResult>> resetGroup(@RequestHeader(CURRENT_USER) String owner,
            @PathVariable String path,
            @PathVariable List<String> include, @PathVariable List<String> exclude) {
        logger.info("Entered REST resetGroups...");
        return ResponseEntity
                .ok()
                .body(membershipService.resetGroup(owner, path,
                        include, exclude));
    }

    /**
     * Get a member's attributes based off username or id number.
     */
    @GetMapping(value = "/members/{uid:[\\w-:.<>]+}")
    @ResponseBody
    public ResponseEntity<Person> memberAttributes(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST memberAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getMemberAttributes(currentUser, uid));
    }

    /**
     * Get a list of a groupings a user is in and can opt into.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}")
    @ResponseBody
    public ResponseEntity<Grouping> getGrouping(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @RequestParam(required = true) Integer page,
            @RequestParam(required = true) Integer size,
            @RequestParam(required = true) String sortString,
            @RequestParam(required = true) Boolean isAscending) {
        logger.info("Entered REST getGrouping...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService
                        .getPaginatedGrouping(path, currentUser, page, size, sortString, isAscending));
    }

    /**
     * Get a List of memberships as which uid has.
     */
    @GetMapping(value = "/members/{uid:[\\w-:.]+}/groupings")
    @ResponseBody
    public ResponseEntity<List<Membership>> membershipResults(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST membershipResults...");
        return ResponseEntity
                .ok()
                .body(membershipService.membershipResults(currentUser, uid));
    }

    /**
     * Get a list of all the paths associated with the groupings which uid as the ability top opt into.
     */
    @GetMapping(value = "/groupings/members/{uid}/opt-in-groups")
    @ResponseBody
    public ResponseEntity<List<GroupingPath>> optInGroupingPaths(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST optInGroups...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService
                        .optInGroupingPaths(currentUser, uid));
    }

    /**
     * Make a user of uid a member of the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/{uid:[\\w-:.]+}/self")
    public ResponseEntity<List<AddMemberResult>> optIn(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path, @PathVariable String uid) {
        logger.info("Entered REST optIn...");
        return ResponseEntity
                .ok()
                .body(membershipService.optIn(currentUser, path, uid));
    }

    /**
     * Make a user of uid a member of the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/{uid:[\\w-:.]+}/self")
    public ResponseEntity<List<AddMemberResult>> optOut(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path, @PathVariable String uid) {
        logger.info("Entered REST optOut...");
        return ResponseEntity
                .ok()
                .body(membershipService.optOut(currentUser, path, uid));
    }

    /**
     * Add a list of users to the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/{uids}")
    public ResponseEntity<List<AddMemberResult>> addIncludeMembers(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path, @PathVariable List<String> uids) {
        logger.info("Entered REST addIncludeMembers...");
        return ResponseEntity
                .ok()
                .body(membershipService.addIncludeMembers(currentUser, path, uids));
    }

    /**
     * Add a list of users to the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/{uids}")
    public ResponseEntity<List<AddMemberResult>> addExcludeMembers(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path, @PathVariable List<String> uids) {
        logger.info("Entered REST addExcludeMembers...");
        return ResponseEntity
                .ok()
                .body(membershipService.addExcludeMembers(currentUser, path, uids));
    }

    /**
     * Remove a list of users from the include group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/{uids}")
    public ResponseEntity<List<RemoveMemberResult>> removeIncludeMembers(
            @RequestHeader(CURRENT_USER) String currentUser, @PathVariable String path,
            @PathVariable List<String> uids) {
        logger.info("Entered REST removeIncludeMembers...");
        return ResponseEntity
                .ok()
                .body(membershipService.removeIncludeMembers(currentUser, path, uids));
    }

    /**
     * Remove a list of users from the exclude include group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/{uids}")
    public ResponseEntity<List<RemoveMemberResult>> removeExcludeMembers(
            @RequestHeader(CURRENT_USER) String currentUser, @PathVariable String path,
            @PathVariable List<String> uids) {
        logger.info("Entered REST removeExcludeMembers...");
        return ResponseEntity
                .ok()
                .body(membershipService.removeExcludeMembers(currentUser, path, uids));
    }

    /**
     * Get an owner's owned groupings by username or UH id number.
     */
    @GetMapping("/owners/{uid:[\\w-:.]+}/groupings")
    public ResponseEntity<List<GroupingPath>> ownerGroupings(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST ownerGroupings...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getOwnedGroupings(currentUser, uid));
    }

    /**
     * Update grouping to add a new owner.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uid}")
    public ResponseEntity<List<AddMemberResult>> addOwners(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @PathVariable List<String> uid) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(membershipService.addOwnerships(path, currentUser, uid));
    }

    /**
     * Delete a grouping owner.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uid}")
    public ResponseEntity<List<RemoveMemberResult>> removeOwners(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @PathVariable List<String> uid) {
        logger.info("Entered REST removeOwners");
        return ResponseEntity
                .ok()
                .body(membershipService.removeOwnerships(path, currentUser, uid));
    }

    /**
     * Update grouping description.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingsServiceResult> updateDescription(
            @RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @RequestBody(required = false) String dtoString) {
        logger.info("Entered REST updateDescription");
        return ResponseEntity
                .ok()
                .body(groupAttributeService.updateDescription(path, currentUser, dtoString));
    }

    /**
     * Update grouping to enable given preference.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/sync-destination/{id:[\\w-:.]+}/enable")
    public ResponseEntity<GroupingsServiceResult> enableSyncDest(
            @RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @PathVariable String id) {
        return ResponseEntity
                .ok()
                .body(groupAttributeService.changeGroupAttributeStatus(path, currentUser, id, true));
    }

    /**
     * Update grouping to disable given preference.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/sync-destination/{id:[\\w-:.]+}/disable")
    public ResponseEntity<GroupingsServiceResult> disableSyncDest(
            @RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @PathVariable String id) {
        return ResponseEntity
                .ok()
                .body(groupAttributeService.changeGroupAttributeStatus(path, currentUser, id, false));
    }

    /**
     * Update grouping to enable given preference.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/preference/{id:[\\w-:.]+}/enable")
    public ResponseEntity<List<GroupingsServiceResult>> enablePreference(
            @RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @PathVariable String id) {
        logger.info("Entered REST enablePreference");
        List<GroupingsServiceResult> results;

        if (OPT_IN.equals(id)) {
            results = groupAttributeService.changeOptInStatus(path, currentUser, true);
        } else if (OPT_OUT.equals(id)) {
            results = groupAttributeService.changeOptOutStatus(path, currentUser, true);
        } else {
            throw new UnsupportedOperationException();
        }

        return ResponseEntity
                .ok()
                .body(results);
    }

    /**
     * Update grouping to disable given preference.
     */
    @RequestMapping(value = "/groupings/{path:[\\w-:.]+}/preference/{id:[\\w-:.]+}/disable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> disablePreference(
            @RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path,
            @PathVariable String id) {
        logger.info("Entered REST disablePreference");
        List<GroupingsServiceResult> results;

        if (OPT_IN.equals(id)) {
            results = groupAttributeService.changeOptInStatus(path, currentUser, false);
        } else if (OPT_OUT.equals(id)) {
            results = groupAttributeService.changeOptOutStatus(path, currentUser, false);
        } else {
            throw new UnsupportedOperationException();
        }
        return ResponseEntity
                .ok()
                .body(results);
    }

    /**
     * Get the list of sync destinations.
     */
    @RequestMapping(value = "/groupings/{path:[\\w-:.]+}/sync-destinations",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<SyncDestination>> getSyncDestinations(@RequestHeader(CURRENT_USER) String
            currentUser,
            @PathVariable String path) throws Exception {
        logger.info("Entered REST getAllSyncDestinations...");
        return ResponseEntity
                .ok()
                .body(groupAttributeService.getAllSyncDestinations(currentUser, path));
    }

    /**
     * True if currentUser is an owner.
     */
    @GetMapping(value = "/owners")
    @ResponseBody
    public ResponseEntity<Boolean> hasOwnerPrivs(@RequestHeader(CURRENT_USER) String currentUser) {
        logger.info("Entered REST hasOwnerPrivs...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.isOwner(currentUser));
    }

    /**
     * True if currentUser is an admin.
     */
    @GetMapping(value = "/admins")
    @ResponseBody
    public ResponseEntity<Boolean> hasAdminPrivs(@RequestHeader(CURRENT_USER) String currentUser) {
        logger.info("Entered REST hasAdminPrivs...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.isAdmin(currentUser));
    }

    /**
     * Get the number of groupings that the current user owns
     */
    @GetMapping(value = "/owners/{uid:[\\w-:.]+}/grouping")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfGroupings(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST getNumberOfGroupings...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getNumberOfGroupings(currentUser, uid));
    }

    /**
     * Get the number of memberships the current user has
     */
    @GetMapping(value = "/groupings/members/{uid:[\\w-:.<>]+}/memberships")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfMemberships(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST getNumberOfMemberships...");
        return ResponseEntity
                .ok()
                .body(membershipService.getNumberOfMemberships(currentUser, uid));
    }

    /**
     * Check if the user is a sole owner of a grouping
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uid}")
    @ResponseBody
    public ResponseEntity<Boolean> isSoleOwner(@RequestHeader(CURRENT_USER) String currentUser,
            @PathVariable String path, @PathVariable String uid) {
        logger.info("Entered REST getGroupingOwners...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.isSoleOwner(currentUser, path, uid));
    }
}