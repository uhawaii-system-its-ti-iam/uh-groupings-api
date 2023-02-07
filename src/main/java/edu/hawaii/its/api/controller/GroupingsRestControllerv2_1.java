package edu.hawaii.its.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResults;
import edu.hawaii.its.api.groupings.GroupingsReplaceGroupMembersResult;
import edu.hawaii.its.api.service.GroupAttributeService;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.AdminListsHolder;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.type.SyncDestination;
import edu.hawaii.its.api.type.UIAddMemberResults;
import edu.hawaii.its.api.type.UIRemoveMemberResults;

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

import edu.hawaii.its.api.wrapper.Subject;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/api/groupings/v2.1")
public class GroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(GroupingsRestControllerv2_1.class);

    @Value("${app.groupings.controller.uuid}")
    private String uuid;

    @Autowired
    private GroupAttributeService groupAttributeService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private UpdateMemberService updateMemberService;

    final private static String CURRENT_USER_KEY = "current_user";

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
    public ResponseEntity<AdminListsHolder> adminsGroupings(@RequestHeader(CURRENT_USER_KEY) String currentUser) {
        logger.info("Entered REST adminsGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.adminLists(currentUser));
    }

    /**
     * Create a new admin.
     */
    @PostMapping(value = "/admins/{uhIdentifier:[\\w-:.]+}")
    public ResponseEntity<GroupingsAddResult> addAdmin(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST addAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.addAdmin(currentUser, uhIdentifier));
    }

    /**
     * Remove an admin.
     */
    @DeleteMapping(value = "/admins/{uhIdentifier:[\\w-:.]+}")
    public ResponseEntity<GroupingsRemoveResult> removeAdmin(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST removeAdmin...");
        return ResponseEntity
                .ok()
                .body(membershipService.removeAdmin(currentUser, uhIdentifier));
    }

    /**
     * Delete a user from multiple groupings.
     */
    @DeleteMapping(value = "/admins/{paths}/{uhIdentifier}")
    public ResponseEntity<List<UIRemoveMemberResults>> removeFromGroups(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable List<String> paths,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST removeFromGroups...");
        return ResponseEntity
                .ok()
                .body(membershipService.removeFromGroups(currentUser, uhIdentifier, paths));
    }

    /**
     * Remove all members from basis, include, exclude.
     */
    @DeleteMapping(value = "/groupings/{path}/{include}/{exclude}/reset-group")
    public ResponseEntity<List<UIRemoveMemberResults>> resetGroup(@RequestHeader(CURRENT_USER_KEY) String owner,
            @PathVariable String path,
            @PathVariable List<String> include, @PathVariable List<String> exclude) {
        logger.info("Entered REST resetGroups...");
        return ResponseEntity
                .ok()
                .body(membershipService.resetGroup(owner, path,
                        include, exclude));
    }

    /**
     * Get a list of invalid uhIdentifiers given a list of uhIdentifiers.
     */
    @PostMapping(value = "/members/invalid")
    @ResponseBody
    public ResponseEntity<List<String>> invalidUhIdentifiers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST membersAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.invalidUhIdentifiers(currentUser, uhIdentifiers));
    }

    /**
     * Remove all members from the include group.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/include")
    public ResponseEntity<GroupingsReplaceGroupMembersResult> resetIncludeGroup(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String groupingPath) {
        logger.info("Entered REST resetIncludeGroup...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.resetIncludeGroup(currentUser, groupingPath));
    }

    /**
     * Remove all members from the exclude group.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/exclude")
    public ResponseEntity<GroupingsReplaceGroupMembersResult> resetExcludeGroup(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String groupingPath) {
        logger.info("Entered REST resetExcludeGroup...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.resetExcludeGroup(currentUser, groupingPath));
    }

    /**
     * Get a member's attributes based off username or id number.
     */
    @GetMapping(value = "/members/{uhIdentifier:[\\w-:.<>]+}")
    @ResponseBody
    public ResponseEntity<Person> memberAttributes(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST memberAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getMemberAttributes(currentUser, uhIdentifier));
    }

    /**
     * Get a list of members' attributes based off a list of uhIdentifiers.
     */
    @PostMapping(value = "/members")
    @ResponseBody
    public ResponseEntity<List<Subject>> membersAttributes(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST membersAttributes...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getMembersAttributes(currentUser, uhIdentifiers));
    }

    /**
     * Get a list of a groupings a user is in and can opt into.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}")
    @ResponseBody
    public ResponseEntity<Grouping> getGrouping(@RequestHeader(CURRENT_USER_KEY) String currentUser,
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
     * Get a List of memberships as which uhIdentifier has.
     */
    @GetMapping(value = "/members/{uhIdentifier:[\\w-:.]+}/memberships")
    @ResponseBody
    public ResponseEntity<List<Membership>> membershipResults(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST membershipResults...");
        return ResponseEntity
                .ok()
                .body(membershipService.membershipResults(currentUser, uhIdentifier));
    }

    /**
     * Get a list of all groupings pertaining to uhIdentifier (nonfiltered).
     */
    @GetMapping(value = "/members/{uhIdentifier:[\\w-:.]+}/groupings")
    @ResponseBody
    public ResponseEntity<List<Membership>> managePersonResults(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST managePersonResults...");
        return ResponseEntity
                .ok()
                .body(membershipService.managePersonResults(currentUser, uhIdentifier));
    }

    /**
     * Get a list of all the paths associated with the groupings which uhIdentifier as the ability top opt into.
     */
    @GetMapping(value = "/groupings/members/{uhIdentifier}/opt-in-groups")
    @ResponseBody
    public ResponseEntity<List<GroupingPath>> optInGroupingPaths(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST optInGroups...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService
                        .optInGroupingPaths(currentUser, uhIdentifier));
    }

    /**
     * Make a user of uhIdentifier a member of the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/{uhIdentifier:[\\w-:.]+}/self")
    public ResponseEntity<UIAddMemberResults> optIn(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @PathVariable String uhIdentifier) {
        logger.info("Entered REST optIn...");
        return ResponseEntity
                .ok()
                .body(membershipService.optIn(currentUser, path, uhIdentifier));
    }

    /**
     * Make a user of uhIdentifier a member of the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/{uhIdentifier:[\\w-:.]+}/self")
    public ResponseEntity<UIAddMemberResults> optOut(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @PathVariable String uhIdentifier) {
        logger.info("Entered REST optOut...");
        return ResponseEntity
                .ok()
                .body(membershipService.optOut(currentUser, path, uhIdentifier));
    }

    /**
     * Add a list of users to the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members")
    public ResponseEntity<GroupingsMoveMembersResult> addIncludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addIncludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addIncludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Add a list of users to the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members")
    public ResponseEntity<GroupingsMoveMembersResult> addExcludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addExcludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addExcludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Remove a list of users from the Include group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/include-members")
    public ResponseEntity<GroupingsRemoveResults> removeIncludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser, @PathVariable String path,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST removeIncludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeIncludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Remove a list of users from the Exclude group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members")
    public ResponseEntity<GroupingsRemoveResults> removeExcludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser, @PathVariable String path,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST removeExcludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeExcludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Get an owner's owned groupings by username or UH id number.
     */
    @GetMapping("/owners/{uhIdentifier:[\\w-:.]+}/groupings")
    public ResponseEntity<List<GroupingPath>> ownerGroupings(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST ownerGroupings...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getOwnedGroupings(currentUser, uhIdentifier));
    }

    /**
     * Update grouping to add a new owner.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uhIdentifier}")
    public ResponseEntity<List<UIAddMemberResults>> addOwners(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @PathVariable List<String> uhIdentifier) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(membershipService.addOwnerships(path, currentUser, uhIdentifier));
    }

    /**
     * Delete a grouping owner.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uhIdentifier}")
    public ResponseEntity<List<UIRemoveMemberResults>> removeOwners(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @PathVariable List<String> uhIdentifier) {
        logger.info("Entered REST removeOwners");
        return ResponseEntity
                .ok()
                .body(membershipService.removeOwnerships(path, currentUser, uhIdentifier));
    }

    /**
     * Update grouping description.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingsServiceResult> updateDescription(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
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
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
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
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
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
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @PathVariable("id") OptType optType) {

        return updatePreference(currentUser, path, optType, true);
    }

    @RequestMapping(value = "/groupings/{path:[\\w-:.]+}/preference/{id:[\\w-:.]+}/disable",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GroupingsServiceResult>> disablePreference(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @PathVariable("id") OptType optType) {

        return updatePreference(currentUser, path, optType, false);
    }

    private ResponseEntity<List<GroupingsServiceResult>> updatePreference(
            String currentUser,
            String path,
            OptType optType,
            boolean value) {

        logger.info("Entered REST updatePreference");

        OptRequest optInRequest = new OptRequest.Builder()
                .withUsername(currentUser)
                .withGroupNameRoot(path)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(optType)
                .withOptValue(value)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withUsername(currentUser)
                .withGroupNameRoot(path)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(optType)
                .withOptValue(value)
                .build();

        return ResponseEntity
                .ok()
                .body(groupAttributeService.changeOptStatus(optInRequest, optOutRequest));
    }

    /**
     * Get the list of sync destinations.
     */
    @RequestMapping(value = "/groupings/{path:[\\w-:.]+}/sync-destinations",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<SyncDestination>> getSyncDestinations(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
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
    public ResponseEntity<Boolean> hasOwnerPrivs(@RequestHeader(CURRENT_USER_KEY) String currentUser) {
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
    public ResponseEntity<Boolean> hasAdminPrivs(@RequestHeader(CURRENT_USER_KEY) String currentUser) {
        logger.info("Entered REST hasAdminPrivs...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.isAdmin(currentUser));
    }

    /**
     * Get the number of groupings that the current user owns
     */
    @GetMapping(value = "/owners/{uhIdentifier:[\\w-:.]+}/groupings/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfGroupings(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST getNumberOfGroupings...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.numberOfGroupings(currentUser, uhIdentifier));
    }

    /**
     * Get the number of memberships the current user has
     */
    @GetMapping(value = "/members/{uhIdentifier:[\\w-:.<>]+}/memberships/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfMemberships(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST getNumberOfMemberships...");
        return ResponseEntity
                .ok()
                .body(membershipService.numberOfMemberships(currentUser, uhIdentifier));
    }

    /**
     * Check if the user is a sole owner of a grouping
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uhIdentifier}")
    @ResponseBody
    public ResponseEntity<Boolean> isSoleOwner(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @PathVariable String uhIdentifier) {
        logger.info("Entered REST getGroupingOwners...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.isSoleOwner(currentUser, path, uhIdentifier));
    }
}
