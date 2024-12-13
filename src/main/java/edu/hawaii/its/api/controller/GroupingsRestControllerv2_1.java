package edu.hawaii.its.api.controller;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.groupings.GroupingAddResult;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingMoveMemberResult;
import edu.hawaii.its.api.groupings.GroupingMoveMembersResult;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.groupings.GroupingRemoveResult;
import edu.hawaii.its.api.groupings.GroupingRemoveResults;
import edu.hawaii.its.api.groupings.GroupingReplaceGroupMembersResult;
import edu.hawaii.its.api.groupings.GroupingSyncDestinations;
import edu.hawaii.its.api.groupings.GroupingUpdateDescriptionResult;
import edu.hawaii.its.api.groupings.GroupingUpdateOptAttributeResult;
import edu.hawaii.its.api.groupings.GroupingUpdateSyncDestResult;
import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.service.AnnouncementsService;
import edu.hawaii.its.api.service.AsyncJobsManager;
import edu.hawaii.its.api.service.GroupingAssignmentService;
import edu.hawaii.its.api.service.GroupingAttributeService;
import edu.hawaii.its.api.service.GroupingOwnerService;
import edu.hawaii.its.api.service.GroupingsService;
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.type.AsyncJobResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;

@RestController
@RequestMapping("/api/groupings/v2.1")
public class GroupingsRestControllerv2_1 {

    private static final Log logger = LogFactory.getLog(GroupingsRestControllerv2_1.class);

    @Value("${app.groupings.controller.uuid}")
    private String uuid;

    private final AsyncJobsManager asyncJobsManager;

    private final GroupingAttributeService groupingAttributeService;

    private final GroupingAssignmentService groupingAssignmentService;

    private final MemberAttributeService memberAttributeService;

    private final MembershipService membershipService;

    private final UpdateMemberService updateMemberService;

    private final MemberService memberService;

    private final GroupingOwnerService groupingOwnerService;

    private final AnnouncementsService announcementsService;

    private final GroupingsService groupingsService;

    final private static String CURRENT_USER_KEY = "current_user";

    public GroupingsRestControllerv2_1(AsyncJobsManager asyncJobsManager,
            GroupingAttributeService groupingAttributeService,
            GroupingAssignmentService groupingAssignmentService,
            MemberAttributeService memberAttributeService,
            MembershipService membershipService,
            UpdateMemberService updateMemberService,
            MemberService memberService,
            GroupingOwnerService groupingOwnerService,
            AnnouncementsService announcementsService,
            GroupingsService groupingsService) {
        this.asyncJobsManager = asyncJobsManager;
        this.groupingAttributeService = groupingAttributeService;
        this.groupingAssignmentService = groupingAssignmentService;
        this.memberAttributeService = memberAttributeService;
        this.membershipService = membershipService;
        this.updateMemberService = updateMemberService;
        this.memberService = memberService;
        this.groupingOwnerService = groupingOwnerService;
        this.announcementsService = announcementsService;
        this.groupingsService = groupingsService;
    }

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
     * Check if a grouping path is valid.
     */
    @GetMapping(value = "/grouping/{path:[\\w-:.]+}/is-valid")
    @ResponseBody
    public ResponseEntity<Boolean> groupingPathIsValid(@PathVariable String path) {
        return ResponseEntity
                .ok()
                .body(groupingAttributeService.isGroupingPath(path));
    }

    /**
     * Get a list of all admins.
     */
    @GetMapping(value = "/groupings/admins")
    @ResponseBody
    public ResponseEntity<GroupingGroupMembers> groupingAdmins(@RequestHeader(CURRENT_USER_KEY) String currentUser) {
        logger.info("Entered REST groupingAdmins...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.groupingAdmins(currentUser));
    }

    /**
     * Get a list of all groupings.
     */
    @GetMapping(value = "/groupings")
    @ResponseBody
    public ResponseEntity<GroupingPaths> allGroupings(@RequestHeader(CURRENT_USER_KEY) String currentUser) {
        logger.info("Entered REST allGroupings...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.allGroupingPaths(currentUser));
    }

    /**
     * Create a new admin.
     */
    @PostMapping(value = "/admins/{uhIdentifier:[\\w-:.]+}")
    public ResponseEntity<GroupingAddResult> addAdmin(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                      @PathVariable String uhIdentifier) {
        logger.info("Entered REST addAdmin...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addAdminMember(currentUser, uhIdentifier));
    }

    /**
     * Remove an admin.
     */
    @DeleteMapping(value = "/admins/{uhIdentifier:[\\w-:.]+}")
    public ResponseEntity<GroupingRemoveResult> removeAdmin(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                            @PathVariable String uhIdentifier) {
        logger.info("Entered REST removeAdmin...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeAdminMember(currentUser, uhIdentifier));
    }

    /**
     * Delete a user from multiple groupings.
     */
    @DeleteMapping(value = "/admins/{paths}/{uhIdentifier}")
    public ResponseEntity<GroupingRemoveResults> removeFromGroups(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable List<String> paths,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST removeFromGroups...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeFromGroups(currentUser, uhIdentifier, paths));
    }

    /**
     * Remove all members from the include group.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/include")
    public ResponseEntity<GroupingReplaceGroupMembersResult> resetIncludeGroup(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String groupingPath) {
        logger.info("Entered REST resetIncludeGroup...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.resetIncludeGroup(currentUser, groupingPath));
    }

    /**
     * Remove all members from the include group asynchronously.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/include/async")
    public ResponseEntity<Integer> resetIncludeGroupAsync(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String groupingPath) {
        logger.info("Entered REST resetIncludeGroupAsync...");
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.resetIncludeGroupAsync(currentUser, groupingPath)));
    }

    /**
     * Remove all members from the exclude group.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/exclude")
    public ResponseEntity<GroupingReplaceGroupMembersResult> resetExcludeGroup(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String groupingPath) {
        logger.info("Entered REST resetExcludeGroup...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.resetExcludeGroup(currentUser, groupingPath));
    }

    /**
     * Remove all members from the exclude group asynchronously.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/exclude/async")
    public ResponseEntity<Integer> resetExcludeGroupAsync(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String groupingPath) {
        logger.info("Entered REST resetExcludeGroupAsync...");
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.resetExcludeGroupAsync(currentUser, groupingPath)));
    }

    /**
     * Get a list of members' attributes based off a list of uhIdentifiers.
     */
    @PostMapping(value = "/members")
    @ResponseBody
    public ResponseEntity<MemberAttributeResults> memberAttributeResults(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST memberAttributeResults...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getMemberAttributeResults(currentUser, uhIdentifiers));
    }

    /**
     * Get a list of members' attributes based off a list of uhIdentifiers asynchronously.
     */
    @PostMapping(value = "/members/async")
    @ResponseBody
    public ResponseEntity<Integer> memberAttributeResultsAsync(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST memberAttributeResultsAsync...");
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(memberAttributeService.getMemberAttributeResultsAsync(currentUser, uhIdentifiers)));
    }

    /**
     * Get all the members of an owned grouping through paginated calls.
     */
    @PostMapping(value = "/groupings/group")
    @ResponseBody
    public ResponseEntity<GroupingGroupsMembers> ownedGrouping(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                               @RequestBody List<String> groupPaths,
                                                               @RequestParam Integer page,
                                                               @RequestParam Integer size,
                                                               @RequestParam String sortString,
                                                               @RequestParam Boolean isAscending) {
        logger.info("Entered REST ownedGrouping...");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService
                        .paginatedGrouping(currentUser, groupPaths, page, size, sortString, isAscending));
    }

    /**
     * Get paginated members by a grouping path.
     */
    @GetMapping(value = "/groupings/{groupingPath}")
    @ResponseBody
    public ResponseEntity<GroupingGroupMembers> getGroupingMembers(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                                   @PathVariable String groupingPath,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer size,
                                                                   @RequestParam String sortString,
                                                                   @RequestParam Boolean isAscending) {
        logger.info("Entered REST getGroupingMembers...");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService
                        .getGroupingMembers(currentUser, groupingPath, page, size, sortString, isAscending));
    }

    /**
     * Get grouping members of a selected grouping path by search string.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/search/{search}")
    public ResponseEntity<GroupingGroupMembers> searchGroupingMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @PathVariable String search) {
        logger.info("Entered REST searchGroupingMembers");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingMembersBySearchString(currentUser, path, search));
    }

    /**
     * Get where listed information for members of a grouping path.
     */
    @PostMapping(value = "/groupings/{groupingPath}/where-listed")
    @ResponseBody
    public ResponseEntity<GroupingMembers> getGroupingMembersWhereListed(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                                         @PathVariable String groupingPath,
                                                                         @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST getGroupingMembersWhereListed...");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.getGroupingMembersWhereListed(currentUser, groupingPath, uhIdentifiers));
    }

    /**
     * Get is basis information for members of a grouping path.
     */
    @PostMapping(value = "/groupings/{groupingPath}/is-basis")
    @ResponseBody
    public ResponseEntity<GroupingMembers> getGroupingMembersIsBasis(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                                     @PathVariable String groupingPath,
                                                                     @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST getGroupingMembersIsBasis...");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.getGroupingMembersIsBasis(currentUser, groupingPath, uhIdentifiers));
    }

    /**
     * Get a List of memberships as which uhIdentifier has.
     */
    @GetMapping(value = "/members/{uhIdentifier:[\\w-:.]+}/memberships")
    @ResponseBody
    public ResponseEntity<MembershipResults> membershipResults(@RequestHeader(CURRENT_USER_KEY) String currentUser,
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
    public ResponseEntity<ManageSubjectResults> manageSubjectResults(@RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String uhIdentifier) {
        logger.info("Entered REST manageSubjectResults...");
        return ResponseEntity
                .ok()
                .body(membershipService.manageSubjectResults(currentUser, uhIdentifier));
    }

    /**
     * Get a list of all the paths associated with the groupings which uhIdentifier has the ability to opt into.
     */
    @GetMapping(value = "/groupings/members/{uhIdentifier}/opt-in-groups")
    @ResponseBody
    public ResponseEntity<GroupingPaths> optInGroupingPaths(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                                 @PathVariable String uhIdentifier) {
        logger.info("Entered REST optInGroups...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.optInGroupingPaths(currentUser, uhIdentifier));
    }

    /**
     * Make a user of uhIdentifier a member of the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/{uhIdentifier:[\\w-:.]+}/self")
    public ResponseEntity<GroupingMoveMemberResult> optIn(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                          @PathVariable String path, @PathVariable String uhIdentifier) {
        logger.info("Entered REST optIn...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.optIn(currentUser, path, uhIdentifier));
    }

    /**
     * Make a user of uhIdentifier a member of the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/{uhIdentifier:[\\w-:.]+}/self")
    public ResponseEntity<GroupingMoveMemberResult> optOut(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                           @PathVariable String path, @PathVariable String uhIdentifier) {
        logger.info("Entered REST optOut...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.optOut(currentUser, path, uhIdentifier));
    }

    /**
     * Add a list of uhIdentifiers to the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members")
    public ResponseEntity<GroupingMoveMembersResult> addIncludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addIncludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addIncludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Add a list of uhIdentifiers to the include group of grouping at path asynchronously.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/async")
    public ResponseEntity<Integer> addIncludeMembersAsync(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addIncludeMembersAsync...");
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.addIncludeMembersAsync(currentUser, path, uhIdentifiers)));
    }

    /**
     * Add a list of users to the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members")
    public ResponseEntity<GroupingMoveMembersResult> addExcludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addExcludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addExcludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Add a list of users to the exclude group of grouping at path asynchronously.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/async")
    public ResponseEntity<Integer> addExcludeMembersAsync(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path, @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addExcludeMembersAsync...");
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.addExcludeMembersAsync(currentUser, path, uhIdentifiers)));
    }

    /**
     * Remove a list of users from the Include group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/include-members")
    public ResponseEntity<GroupingRemoveResults> removeIncludeMembers(
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
    public ResponseEntity<GroupingRemoveResults> removeExcludeMembers(
            @RequestHeader(CURRENT_USER_KEY) String currentUser, @PathVariable String path,
            @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST removeExcludeMembers...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeExcludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Get an owner's owned groupings by uid or uhUuid.
     */
    @GetMapping("/owners/{uhIdentifier:[\\w-:.]+}/groupings")
    public ResponseEntity<GroupingPaths> ownerGroupings(@RequestHeader(CURRENT_USER_KEY) String currentUser,
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
    public ResponseEntity<GroupingAddResults> addOwners(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                        @PathVariable String path,
                                                        @PathVariable List<String> uhIdentifier) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addOwnerships(currentUser, path, uhIdentifier));
    }

    /**
     * Update grouping to add new path owners.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/owners/path-owner/{pathOwners}")
    public ResponseEntity<GroupingAddResults> addGroupPathOwner(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                        @PathVariable String path,
                                                        @PathVariable List<String> pathOwners) {
        logger.info("Entered REST addOwner...");
        return ResponseEntity
                .ok()
                .body(updateMemberService.addGroupPathOwnership(currentUser, path, pathOwners));
    }


    /**
     * Delete a grouping owner.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uhIdentifier}")
    public ResponseEntity<GroupingRemoveResults> removeOwners(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                              @PathVariable String path,
                                                              @PathVariable List<String> uhIdentifier) {
        logger.info("Entered REST removeOwners");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeOwnerships(currentUser, path, uhIdentifier));
    }


    /**
     * Delete grouping group path owners.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/path-owner/{pathOwners}")
    public ResponseEntity<GroupingRemoveResults> removeGroupPathOwners(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                              @PathVariable String path,
                                                              @PathVariable List<String> pathOwners) {
        logger.info("Entered REST removeGroupPathOwners");
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeGroupPathOwnerships(currentUser, path, pathOwners));
    }

    /**
     * Get the opt attributes of a selected grouping.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/opt-attributes")
    public ResponseEntity<GroupingOptAttributes> groupingOptAttributes(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path) {
        logger.info("Entered REST groupingOptAttributes...");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingOptAttributes(currentUser, path));
    }

    /**
     * Get a list of sync-destinations for a selected grouping.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/groupings-sync-destinations")
    public ResponseEntity<GroupingSyncDestinations> groupingsSyncDestinations(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path) {
        logger.info("Entered REST groupingSyncDestinations...");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingsSyncDestinations(currentUser, path));
    }

    /**
     * Get the description of a selected grouping.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingDescription> groupingDescription(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path) {
        logger.info("Entered REST getGroupingDescription");
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingsDescription(currentUser, path));
    }

    /**
     * Update grouping description.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingUpdateDescriptionResult> updateDescription(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @RequestBody(required = false) String dtoString) {
        logger.info("Entered REST updateDescription");
        return ResponseEntity
                .ok()
                .body(groupingAttributeService.updateDescription(path, currentUser, dtoString));
    }

    /**
     * Update grouping to enable/disable a sync destination.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/sync-destination/{id:[\\w-:.]+}/{status}")
    public ResponseEntity<GroupingUpdateSyncDestResult> updateSyncDest(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @PathVariable String id,
            @PathVariable boolean status) {
        logger.info("Entered REST updateSyncDest");
        return ResponseEntity
                .ok()
                .body(groupingAttributeService.updateGroupingSyncDest(path, currentUser, id, status));
    }

    /**
     * Update grouping to enable/disable an opt attribute.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/opt-attribute/{id:[\\w-:.]+}/{status}")
    public ResponseEntity<GroupingUpdateOptAttributeResult> updateOptAttribute(
            @RequestHeader(CURRENT_USER_KEY) String currentUser,
            @PathVariable String path,
            @PathVariable("id") OptType preferenceId,
            @PathVariable boolean status) {
        logger.info("Entered REST updateOptAttribute");

        OptRequest optInRequest = new OptRequest.Builder()
                .withUid(currentUser)
                .withGroupNameRoot(path)
                .withPrivilegeType(PrivilegeType.IN)
                .withOptType(preferenceId)
                .withOptValue(status)
                .build();

        OptRequest optOutRequest = new OptRequest.Builder()
                .withUid(currentUser)
                .withGroupNameRoot(path)
                .withPrivilegeType(PrivilegeType.OUT)
                .withOptType(preferenceId)
                .withOptValue(status)
                .build();

        return ResponseEntity
                .ok()
                .body(groupingAttributeService.updateOptAttribute(optInRequest, optOutRequest));
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
                .body(memberService.isOwner(currentUser));
    }

    /**
     * True if currentUser is an admin.
     */
    @GetMapping(value = "/members/is-admin")
    @ResponseBody
    public ResponseEntity<Boolean> hasAdminPrivs(@RequestHeader(CURRENT_USER_KEY) String currentUser) {
        logger.info("Entered REST hasAdminPrivs...");
        return ResponseEntity
                .ok()
                .body(memberService.isAdmin(currentUser));
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
     * Get number of grouping members
     */
    @GetMapping(value = "/groupings/{path}/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfGroupingMembers(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                              @PathVariable String path) {
        logger.info("Enter REST getNumberOfGroupingMembers...");
        return ResponseEntity
                .ok()
                .body(groupingsService.numberOfGroupingMembers(currentUser, path));
    }

    /**
     * Check if the user is a sole owner of a grouping
     */
    @GetMapping(value = "/members/{path:[\\w-:.]+}/owners/{uhIdentifier}/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfOwners(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                                     @PathVariable String path, @PathVariable String uhIdentifier) {
        logger.info("Entered REST getNumberOfOwners...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.numberOfOwners(currentUser, path, uhIdentifier));
    }

    /**
     * A list of all owners listed in grouping.
     */
    @GetMapping(value = "/grouping/{path:[\\w-:.]+}/owners")
    public ResponseEntity<GroupingGroupMembers> groupingOwners(@RequestHeader(CURRENT_USER_KEY) String
                                                                       currentUser,
                                                               @PathVariable String path) {
        logger.info("Entered REST groupingOwners...");
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.groupingOwners(currentUser, path));
    }

    /**
     * Get result of async job.
     */
    @GetMapping(value = "/jobs/{jobId}")
    public ResponseEntity<AsyncJobResult> getAsyncJobResult(@RequestHeader(CURRENT_USER_KEY) String currentUser,
                                            @PathVariable Integer jobId) {
        logger.debug("Entered REST getAsyncJobResult...");
        return ResponseEntity
                .ok()
                .body(asyncJobsManager.getJobResult(currentUser, jobId));
    }

    /**
     * Get the list of active announcements to display.
     */
    @GetMapping(value = "/announcements")
    public ResponseEntity<Announcements> getAnnouncements() {
        logger.info("Entered REST activeAnnouncements...");
        return ResponseEntity
                .ok()
                .body(announcementsService.getAnnouncements());
    }
    
}