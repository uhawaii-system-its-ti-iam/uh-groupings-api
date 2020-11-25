package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GrouperConfiguration;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.HelperService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.GenericServiceResult;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
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
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groupings/v2.1")
public class GroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(GroupingsRestControllerv2_1.class);
    @Autowired
    private GrouperConfiguration grouperConfiguration;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private HelperService helperService;

    @Value("${app.groupings.controller.uuid}")
    private String uuid;

    @PostConstruct
    public void init() {
        Assert.hasLength(uuid, "Property 'app.groupings.controller.uuid' is required.");
        logger.info("GroupingsRestController started.");
    }

    /**
     * Get a hello string, this is a test endpoint.
     */
    @GetMapping(value = "/")
    @ResponseBody public ResponseEntity hello() {
        return ResponseEntity
                .ok()
                .body("University of Hawaii Groupings");
    }

    /**
     * Get a GenericServiceResult to be viewed on swagger, a great helper for observing the contents of a grouper
     * object.
     */
    @GetMapping(value = "/swagger/toString/{path}")
    @ResponseBody
    public ResponseEntity<GenericServiceResult> swaggerToString(@RequestHeader("current_user") String currentUser,
            String path) {
        logger.info("Entered REST swaggerToString");
        return ResponseEntity
                .ok()
                .body(helperService.swaggerToString(currentUser, path));
    }

    /**
     * Get all admins and groupings.
     */
    @GetMapping(value = "/adminsGroupings")
    @ResponseBody
    public ResponseEntity<AdminListsHolder> adminsGroupings(@RequestHeader("current_user") String currentUser) {
        logger.info("Entered REST adminsGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.adminLists(currentUser));
    }

    /**
     * Create a new admin.
     */
    @PostMapping(value = "/admins/{uid:[\\w-:.]+}")
    public ResponseEntity<GroupingsServiceResult> addNewAdmin(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST addNewAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.addAdmin(currentUser, uid));
    }

    /**
     * Delete an admin.
     */
    @DeleteMapping(value = "/admins/{uid:[\\w-:.]+}")
    public ResponseEntity<GroupingsServiceResult> deleteNewAdmin(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST deleteNewAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteAdmin(currentUser, uid));
    }

    /**
     * Delete a user from multiple groupings.
     */
    @DeleteMapping(value = "/admins/{paths}/{uid}")
    public ResponseEntity<List<GroupingsServiceResult>> removeFromGroups(
            @RequestHeader("current_user") String currentUser,
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
    @DeleteMapping(value = "/groupings/{path}/{include}/{exclude}/resetGroup")
    public ResponseEntity<List<GroupingsServiceResult>> resetGroup(@RequestHeader("current_user") String owner,
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
    public ResponseEntity<Map<String, String>> memberAttributes(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST memberAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getUserAttributes(currentUser, uid));
    }

    /**
     * Get a list of a groupings a user is in and can opt into.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}")
    @ResponseBody
    public ResponseEntity<Grouping> getGrouping(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortString,
            @RequestParam(required = false) Boolean isAscending) {
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
    public ResponseEntity<List<Membership>> membershipResults(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST membershipResults...");
        return ResponseEntity
                .ok()
                .body(membershipService.getMembershipResults(currentUser, uid));
    }

    /**
     * Get a list of all the paths associated with the groupings which uid as the ability top opt into.
     */
    @GetMapping(value = "/groupings/optInGroups/{uid}")
    @ResponseBody
    public ResponseEntity<List<String>> getOptInGroups(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST optInGroups...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService
                        .getOptInGroups(currentUser, uid));
    }

    /**
     * Make a user of uid a member of the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/includeMembers/{uid:[\\w-:.]+}/self")
    public ResponseEntity<List<GroupingsServiceResult>> optIn(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST optIn...");
        return ResponseEntity
                .ok()
                .body(membershipService.optIn(currentUser, path, uid));
    }

    /**
     * Make a user of uid a member of the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/excludeMembers/{uid:[\\w-:.]+}/self")
    public ResponseEntity<List<GroupingsServiceResult>> optOut(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST optOut...");
        return ResponseEntity
                .ok()
                .body(membershipService.optOut(currentUser, path, uid));
    }

    /**
     * Add user with uid as a new include member to grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/includeMembers/{uid:[\\w-:.]+}")
    public ResponseEntity<List<GroupingsServiceResult>> includeMembers(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST includeMembers...");
        path = path + grouperConfiguration.getInclude();
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMember(currentUser, path, uid));
    }

    /**
     * Add multiple users with a list of uids to the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/includeMultipleMembers/{uids}")
    public ResponseEntity<List<GroupingsServiceResult>> includeMultipleMembers(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable List<String> uids) throws IOException, MessagingException {
        logger.info("Entered REST includeMultipleMembers...");
        path = path + grouperConfiguration.getInclude();
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMembers(currentUser, path, uids));
    }

    /**
     * Add user with uid as a new exclude member to grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/excludeMembers/{uid:[\\w-:.]+}")
    public ResponseEntity<List<GroupingsServiceResult>> excludeMembers(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST excludeMembers...");
        path = path + grouperConfiguration.getExclude();
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMember(currentUser, path, uid));
    }

    /**
     * Add multiple users with a list of uids to the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/excludeMultipleMembers/{uids}")
    public ResponseEntity<List<GroupingsServiceResult>> excludeMultipleMembers(
            @RequestHeader("current_user") String currentUser, @PathVariable String path,
            @PathVariable List<String> uids) throws IOException, MessagingException {
        logger.info("Entered REST excludeMultipleMembers...");
        path = path + grouperConfiguration.getExclude();
        return ResponseEntity
                .ok()
                .body(membershipService.addGroupMembers(currentUser, path, uids));
    }

    /**
     * Remove member with uid from include group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/includeMembers/{uid:[\\w-:.]+}")
    public ResponseEntity<GroupingsServiceResult> deleteInclude(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteInclude");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteGroupMember(currentUser, path + grouperConfiguration.getInclude(), uid));
    }

    /**
     * Remove member with uid from exclude group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/excludeMembers/{uid:[\\w-:.]+}")
    public ResponseEntity<GroupingsServiceResult> deleteExclude(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteExclude");
        return ResponseEntity
                .ok()
                .body(membershipService.deleteGroupMember(currentUser, path + grouperConfiguration.getExclude(), uid));
    }

    /**
     * Remove multiple members pertaining to the uids list from exclude grouping of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/excludeMultipleMembers/{uids}")
    public ResponseEntity<List<GroupingsServiceResult>> deleteMultipleExcludeMembers(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable List<String> uids) {
        logger.info("Entered REST deleteExclude");
        return ResponseEntity
                .ok()
                .body(membershipService
                        .deleteGroupMembers(currentUser, path + grouperConfiguration.getExclude(), uids));
    }

    /**
     * Get an owner's owned groupings by username or UH id number.
     */
    @GetMapping("/owners/{uid:[\\w-:.]+}/groupings")
    public ResponseEntity<List<Grouping>> ownerGroupings(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST ownerGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.restGroupingsOwned(currentUser, uid));
    }

    /**
     * Update grouping to add a new owner.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uid:[\\w-:.]+}")
    public ResponseEntity<GroupingsServiceResult> addOwner(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.assignOwnership(path, currentUser, uid));
    }

    /**
     * Delete a grouping owner.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uid:[\\w-:.]+}")
    public ResponseEntity<GroupingsServiceResult> deleteOwner(@RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String uid) {
        logger.info("Entered REST deleteOwner");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.removeOwnership(path, currentUser, uid));
    }

    /**
     * Update grouping description.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingsServiceResult> updateDescription(
            @RequestHeader("current_user") String currentUser,
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
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/syncDests/{syncDestName:[\\w-:.]+}/enable")
    public ResponseEntity<GroupingsServiceResult> enableSyncDest(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String syncDestName) {
        return ResponseEntity
                .ok()
                .body(groupAttributeService.changeGroupAttributeStatus(path, currentUser, syncDestName, true));
    }

    /**
     * Update grouping to disable given preference.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/syncDests/{syncDestName:[\\w-:.]+}/disable")
    public ResponseEntity<GroupingsServiceResult> disableSyncDest(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String syncDestName) {
        return ResponseEntity
                .ok()
                .body(groupAttributeService.changeGroupAttributeStatus(path, currentUser, syncDestName, false));
    }

    /**
     * Get a response which specifies whether uid is an owner or not.
     */
    @GetMapping(value = "/admins/{uid:[\\w-:.]+}")
    @ResponseBody
    public ResponseEntity<GenericServiceResult> getIsAdmin(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST getAllSyncDestinations...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getIsAdmin(currentUser, uid));
    }

    /**
     * Update grouping to enable given preference.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/preferences/{preferenceId:[\\w-:.]+}/enable")
    public ResponseEntity<List<GroupingsServiceResult>> enablePreference(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String preferenceId) {
        logger.info("Entered REST enablePreference");
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (grouperConfiguration.getOptIn().equals(preferenceId))
            results = groupAttributeService.changeOptInStatus(path, currentUser, true);
        else if (grouperConfiguration.getOptOut().equals(preferenceId))
            results = groupAttributeService.changeOptOutStatus(path, currentUser, true);
        else
            throw new UnsupportedOperationException();

        return ResponseEntity
                .ok()
                .body(results);
    }

    /**
     * Update grouping to disable given preference.
     */
    @RequestMapping(value = "/groupings/{path:[\\w-:.]+}/preferences/{preferenceId:[\\w-:.]+}/disable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> disablePreference(
            @RequestHeader("current_user") String currentUser,
            @PathVariable String path,
            @PathVariable String preferenceId) {
        logger.info("Entered REST disablePreference");
        List<GroupingsServiceResult> results = new ArrayList<>();

        if (grouperConfiguration.getOptIn().equals(preferenceId))
            results = groupAttributeService.changeOptInStatus(path, currentUser, false);
        else if (grouperConfiguration.getOptOut().equals(preferenceId))
            results = groupAttributeService.changeOptOutStatus(path, currentUser, false);
        else
            throw new UnsupportedOperationException();
        return ResponseEntity
                .ok()
                .body(results);
    }

    /**
     * Get the list of sync destinations.
     */
    @RequestMapping(value = "/groupings/{path:[\\w-:.]+}/syncDestinations",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<SyncDestination>> getSyncDestinations(@RequestHeader("current_user") String
            currentUser,
            @PathVariable String path) throws Exception {
        logger.info("Entered REST getAllSyncDestinations...");
        return ResponseEntity
                .ok()
                .body(groupAttributeService.getAllSyncDestinations(currentUser, path));
    }

    /**
     * Ger a response which specifies whether uid is an owner or not,
     */
    @RequestMapping(value = "/owners/{uid:[\\w-:.]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<GenericServiceResult> getIsOwner(@RequestHeader("current_user") String currentUser,
            @PathVariable String uid) {
        logger.info("Entered REST getAllSyncDestinations...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getIsOwner(currentUser, uid));
    }

}
