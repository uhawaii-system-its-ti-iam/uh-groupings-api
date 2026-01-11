package edu.hawaii.its.api.controller;

import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.api.groupings.GroupingAddResult;
import edu.hawaii.its.api.groupings.GroupingAddResults;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingOwnerMembers;
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
import edu.hawaii.its.api.service.MemberAttributeService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.service.MembershipService;
import edu.hawaii.its.api.service.UpdateMemberService;
import edu.hawaii.its.api.type.Announcements;
import edu.hawaii.its.api.type.AsyncJobResult;
import edu.hawaii.its.api.type.OptRequest;
import edu.hawaii.its.api.type.OptType;
import edu.hawaii.its.api.type.PrivilegeType;
import edu.hawaii.its.api.type.SortBy;

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

    public GroupingsRestControllerv2_1(AsyncJobsManager asyncJobsManager,
            GroupingAttributeService groupingAttributeService,
            GroupingAssignmentService groupingAssignmentService,
            MemberAttributeService memberAttributeService,
            MembershipService membershipService,
            UpdateMemberService updateMemberService,
            MemberService memberService,
            GroupingOwnerService groupingOwnerService,
            AnnouncementsService announcementsService) {
        this.asyncJobsManager = asyncJobsManager;
        this.groupingAttributeService = groupingAttributeService;
        this.groupingAssignmentService = groupingAssignmentService;
        this.memberAttributeService = memberAttributeService;
        this.membershipService = membershipService;
        this.updateMemberService = updateMemberService;
        this.memberService = memberService;
        this.groupingOwnerService = groupingOwnerService;
        this.announcementsService = announcementsService;
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
    public ResponseEntity<GroupingGroupMembers> groupingAdmins() {
        logger.info("Entered REST groupingAdmins...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.groupingAdmins(currentUser));
    }

    /**
     * Get a list of all groupings.
     */
    @GetMapping(value = "/groupings")
    @ResponseBody
    public ResponseEntity<GroupingPaths> allGroupings() {
        logger.info("Entered REST allGroupings...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.allGroupingPaths(currentUser));
    }

    /**
     * Create a new admin.
     */
    @PostMapping(value = "/admins/{uhIdentifier:[\\w-:.]+}")
    public ResponseEntity<GroupingAddResult> addAdmin(@PathVariable String uhIdentifier) {
        logger.info("Entered REST addAdmin...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.addAdminMember(currentUser, uhIdentifier));
    }

    /**
     * Remove an admin.
     */
    @DeleteMapping(value = "/admins/{uhIdentifier:[\\w-:.]+}")
    public ResponseEntity<GroupingRemoveResult> removeAdmin(@PathVariable String uhIdentifier) {
        logger.info("Entered REST removeAdmin...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeAdminMember(currentUser, uhIdentifier));
    }

    /**
     * Delete a user from multiple groupings.
     */
    @DeleteMapping(value = "/admins/{paths}/{uhIdentifier}")
    public ResponseEntity<GroupingRemoveResults> removeFromGroups(@PathVariable List<String> paths,
                                                                  @PathVariable String uhIdentifier) {
        logger.info("Entered REST removeFromGroups...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeFromGroups(currentUser, uhIdentifier, paths));
    }

    /**
     * Remove all members from the include group.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/include")
    public ResponseEntity<GroupingReplaceGroupMembersResult> resetIncludeGroup(@PathVariable String groupingPath) {
        logger.info("Entered REST resetIncludeGroup...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.resetIncludeGroup(currentUser, groupingPath));
    }

    /**
     * Remove all members from the include group asynchronously.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/include/async")
    public ResponseEntity<Integer> resetIncludeGroupAsync(@PathVariable String groupingPath) {
        logger.info("Entered REST resetIncludeGroupAsync...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.resetIncludeGroupAsync(currentUser, groupingPath)));
    }

    /**
     * Remove all members from the exclude group.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/exclude")
    public ResponseEntity<GroupingReplaceGroupMembersResult> resetExcludeGroup(@PathVariable String groupingPath) {
        logger.info("Entered REST resetExcludeGroup...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.resetExcludeGroup(currentUser, groupingPath));
    }

    /**
     * Remove all members from the exclude group asynchronously.
     */
    @DeleteMapping(value = "/groupings/{groupingPath}/exclude/async")
    public ResponseEntity<Integer> resetExcludeGroupAsync(@PathVariable String groupingPath) {
        logger.info("Entered REST resetExcludeGroupAsync...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.resetExcludeGroupAsync(currentUser, groupingPath)));
    }

    /**
     * Get a list of members' attributes based off a list of uhIdentifiers.
     */
    @PostMapping(value = "/members")
    @ResponseBody
    public ResponseEntity<MemberAttributeResults> memberAttributeResults(@RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST memberAttributeResults...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getMemberAttributeResults(currentUser, uhIdentifiers));
    }

    /**
     * Get a list of members' attributes based off a list of uhIdentifiers asynchronously.
     */
    @PostMapping(value = "/members/async")
    @ResponseBody
    public ResponseEntity<Integer> memberAttributeResultsAsync(@RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST memberAttributeResultsAsync...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(memberAttributeService.getMemberAttributeResultsAsync(currentUser, uhIdentifiers)));
    }
    @PostMapping(value = "/groupings/group")
    @ResponseBody
    public ResponseEntity<GroupingGroupsMembers> ownedGrouping(@RequestBody List<String> groupPaths,
                                                               @RequestParam Integer page,
                                                               @RequestParam Integer size,
                                                               @RequestParam SortBy sortBy,
                                                               @RequestParam Boolean isAscending) {
        logger.info("Entered REST ownedGrouping...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService
                        .paginatedGrouping(currentUser, groupPaths, page, size, sortBy.sortString(), isAscending));
    }

    /**
     * Get paginated members by a grouping path.
     */
    @GetMapping(value = "/groupings/{groupingPath}")
    @ResponseBody
    public ResponseEntity<GroupingGroupMembers> getGroupingMembers(@PathVariable String groupingPath,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer size,
                                                                   @RequestParam(required = true) SortBy sortBy,
                                                                   @RequestParam Boolean isAscending,
                                                                   @RequestParam(required = false) String searchString) {
        logger.info("Entered REST getGroupingMembers...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService
                        .getGroupingMembers(currentUser, groupingPath, page, size, sortBy.sortString(), isAscending, searchString));
    }

    /**
     * Get where listed information for members of a grouping path.
     */
    @PostMapping(value = "/groupings/{groupingPath}/where-listed")
    @ResponseBody
    public ResponseEntity<GroupingMembers> getGroupingMembersWhereListed(@PathVariable String groupingPath,
                                                                         @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST getGroupingMembersWhereListed...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.getGroupingMembersWhereListed(currentUser, groupingPath, uhIdentifiers));
    }

    /**
     * Get is basis information for members of a grouping path.
     */
    @PostMapping(value = "/groupings/{groupingPath}/is-basis")
    @ResponseBody
    public ResponseEntity<GroupingMembers> getGroupingMembersIsBasis(@PathVariable String groupingPath,
                                                                     @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST getGroupingMembersIsBasis...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.getGroupingMembersIsBasis(currentUser, groupingPath, uhIdentifiers));
    }

    /**
     * Get a List of memberships as which uhIdentifier has.
     */
    @GetMapping(value = "/members/memberships")
    @ResponseBody
    public ResponseEntity<MembershipResults> membershipResults() {
        logger.info("Entered REST membershipResults...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(membershipService.membershipResults(currentUser));
    }

    /**
     * Get a list of all groupings pertaining to uhIdentifier (nonfiltered).
     */
    @GetMapping(value = "/members/{uhIdentifier:[\\w-:.]+}/groupings")
    @ResponseBody
    public ResponseEntity<ManageSubjectResults> manageSubjectResults(@PathVariable String uhIdentifier) {
        logger.info("Entered REST manageSubjectResults...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(membershipService.manageSubjectResults(currentUser, uhIdentifier));
    }

    /**
     * Get a list of all the paths associated with the groupings which uhIdentifier has the ability to opt into.
     */
    @GetMapping(value = "/groupings/members/{uhIdentifier}/opt-in-groups")
    @ResponseBody
    public ResponseEntity<GroupingPaths> optInGroupingPaths(@PathVariable String uhIdentifier) {
        logger.info("Entered REST optInGroups...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.optInGroupingPaths(currentUser, uhIdentifier));
    }

    /**
     * Make a user of uhIdentifier a member of the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/{uhIdentifier:[\\w-:.]+}/self")
    public ResponseEntity<GroupingMoveMemberResult> optIn(@PathVariable String path,
                                                          @PathVariable String uhIdentifier) {
        logger.info("Entered REST optIn...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.optIn(currentUser, path, uhIdentifier));
    }

    /**
     * Make a user of uhIdentifier a member of the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/{uhIdentifier:[\\w-:.]+}/self")
    public ResponseEntity<GroupingMoveMemberResult> optOut(@PathVariable String path,
                                                           @PathVariable String uhIdentifier) {
        logger.info("Entered REST optOut...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.optOut(currentUser, path, uhIdentifier));
    }

    /**
     * Add a list of uhIdentifiers to the include group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members")
    public ResponseEntity<GroupingMoveMembersResult> addIncludeMembers(@PathVariable String path,
                                                                       @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addIncludeMembers...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.addIncludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Add a list of uhIdentifiers to the include group of grouping at path asynchronously.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/include-members/async")
    public ResponseEntity<Integer> addIncludeMembersAsync(@PathVariable String path,
                                                          @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addIncludeMembersAsync...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.addIncludeMembersAsync(currentUser, path, uhIdentifiers)));
    }

    /**
     * Add a list of users to the exclude group of grouping at path.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members")
    public ResponseEntity<GroupingMoveMembersResult> addExcludeMembers(@PathVariable String path,
                                                                       @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addExcludeMembers...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.addExcludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Add a list of users to the exclude group of grouping at path asynchronously.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members/async")
    public ResponseEntity<Integer> addExcludeMembersAsync(@PathVariable String path,
                                                          @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST addExcludeMembersAsync...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .accepted()
                .body(asyncJobsManager.putJob(updateMemberService.addExcludeMembersAsync(currentUser, path, uhIdentifiers)));
    }

    /**
     * Remove a list of users from the Include group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/include-members")
    public ResponseEntity<GroupingRemoveResults> removeIncludeMembers(@PathVariable String path,
                                                                      @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST removeIncludeMembers...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeIncludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Remove a list of users from the Exclude group of grouping at path.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/exclude-members")
    public ResponseEntity<GroupingRemoveResults> removeExcludeMembers(@PathVariable String path,
                                                                      @RequestBody List<String> uhIdentifiers) {
        logger.info("Entered REST removeExcludeMembers...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeExcludeMembers(currentUser, path, uhIdentifiers));
    }

    /**
     * Get a current user's owned groupings
     */
    @GetMapping("/owners/groupings")
    public ResponseEntity<GroupingPaths> ownerGroupings() {
        logger.info("Entered REST ownerGroupings...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(memberAttributeService.getOwnedGroupings(currentUser));
    }

    /**
     * Update grouping to add a new owner.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uhIdentifier}")
    public ResponseEntity<GroupingAddResults> addOwners(@PathVariable String path,
                                                        @PathVariable List<String> uhIdentifier) {
        logger.info("Entered REST addOwners...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.addOwnerships(currentUser, path, uhIdentifier));
    }

    /**
     * Update grouping to add new owner-groupings.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/owners/owner-groupings/{ownerGroupings}")
    public ResponseEntity<GroupingAddResults> addOwnerGroupings(@PathVariable String path,
                                                                @PathVariable List<String> ownerGroupings) {
        logger.info("Entered REST addOwnerGroupings...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.addOwnerGroupingOwnerships(currentUser, path, ownerGroupings));
    }


    /**
     * Delete a grouping owner(s).
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/{uhIdentifier}")
    public ResponseEntity<GroupingRemoveResults> removeOwners(@PathVariable String path,
                                                              @PathVariable List<String> uhIdentifier) {
        logger.info("Entered REST removeOwners");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeOwnerships(currentUser, path, uhIdentifier));
    }


    /**
     * Delete grouping owner-groupings.
     */
    @DeleteMapping(value = "/groupings/{path:[\\w-:.]+}/owners/owner-groupings/{ownerGroupings}")
    public ResponseEntity<GroupingRemoveResults> removeOwnerGroupings(@PathVariable String path,
                                                                      @PathVariable List<String> ownerGroupings) {
        logger.info("Entered REST removeOwnerGroupings");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(updateMemberService.removeOwnerGroupingOwnerships(currentUser, path, ownerGroupings));
    }

    /**
     * Get the opt attributes of a selected grouping.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/opt-attributes")
    public ResponseEntity<GroupingOptAttributes> groupingOptAttributes(@PathVariable String path) {
        logger.info("Entered REST groupingOptAttributes...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingOptAttributes(currentUser, path));
    }

    /**
     * Get a list of sync-destinations for a selected grouping.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/groupings-sync-destinations")
    public ResponseEntity<GroupingSyncDestinations> groupingsSyncDestinations(@PathVariable String path) {
        logger.info("Entered REST groupingSyncDestinations...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingsSyncDestinations(currentUser, path));
    }

    /**
     * Get the description of a selected grouping.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingDescription> groupingDescription(@PathVariable String path) {
        logger.info("Entered REST getGroupingDescription");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.groupingsDescription(currentUser, path));
    }

    /**
     * Update grouping description.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/description")
    public ResponseEntity<GroupingUpdateDescriptionResult> updateDescription(
            @PathVariable String path,
            @RequestBody(required = false) String dtoString) {
        logger.info("Entered REST updateDescription");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAttributeService.updateDescription(path, currentUser, dtoString));
    }

    /**
     * Update grouping to enable/disable a sync destination.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/sync-destination/{id:[\\w-:.]+}/{status}")
    public ResponseEntity<GroupingUpdateSyncDestResult> updateSyncDest(@PathVariable String path,
                                                                       @PathVariable String id,
                                                                       @PathVariable boolean status) {
        logger.info("Entered REST updateSyncDest");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAttributeService.updateGroupingSyncDest(path, currentUser, id, status));
    }

    /**
     * Update grouping to enable/disable an opt attribute.
     */
    @PutMapping(value = "/groupings/{path:[\\w-:.]+}/opt-attribute/{id:[\\w-:.]+}/{status}")
    public ResponseEntity<GroupingUpdateOptAttributeResult> updateOptAttribute(
            @PathVariable String path,
            @PathVariable("id") OptType preferenceId,
            @PathVariable boolean status) {
        logger.info("Entered REST updateOptAttribute");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

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
    @GetMapping(value = "/members/{uhIdentifier}/is-owner")
    @ResponseBody
    public ResponseEntity<Boolean> hasOwnerPrivs(@PathVariable String uhIdentifier) {
        logger.info("Entered REST hasOwnerPrivs...");
        return ResponseEntity
                .ok()
                .body(memberService.isOwner(uhIdentifier));
    }

    /**
     * True if user is an owner of the grouping.
     */
    @GetMapping(value = "/members/{path:[\\w-:.]+}/{uhIdentifier}/is-owner")
    @ResponseBody
    public ResponseEntity<Boolean> hasGroupingOwnerPrivs(@PathVariable String path,
                                                         @PathVariable String uhIdentifier) {
        logger.info("Entered REST hasGroupingOwnerPrivs...");
        return ResponseEntity
                .ok()
                .body(memberService.isOwner(path, uhIdentifier));
    }

    /**
     * True if currentUser is an admin.
     */
    @GetMapping(value = "/members/{uhIdentifier}/is-admin")
    @ResponseBody
    public ResponseEntity<Boolean> hasAdminPrivs(@PathVariable String uhIdentifier) {
        logger.info("Entered REST hasAdminPrivs...");
        return ResponseEntity
                .ok()
                .body(memberService.isAdmin(uhIdentifier));
    }

    /**
     * Get the number of groupings that the current user owns
     */
    @GetMapping(value = "/owners/groupings/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfGroupings() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Entered REST getNumberOfGroupings...");
        return ResponseEntity
                .ok()
                .body(memberAttributeService.numberOfGroupings(currentUser));
    }

    /**
     * Get the number of memberships the current user has
     */
    @GetMapping(value = "/members/memberships/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfMemberships() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Entered REST getNumberOfMemberships...");
        return ResponseEntity
                .ok()
                .body(membershipService.numberOfMemberships(currentUser));
    }

    /**
     * Get number of grouping members
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfGroupingMembers(@PathVariable String path) {
        logger.info("Enter REST getNumberOfGroupingMembers...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingOwnerService.numberOfGroupingMembers(currentUser, path));
    }

    /**
     * Used to check if the user is a sole owner of a grouping.
     */
    @GetMapping(value = "/members/{path:[\\w-:.]+}/owners/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfOwners(@PathVariable String path) {
        logger.info("Entered REST getNumberOfOwners...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.numberOfDirectOwners(currentUser, path));
    }

    /**
     * Get number of all owners (direct + indirect) in a grouping.
     * Owners with "ALL" filter.
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/owners/count")
    @ResponseBody
    public ResponseEntity<Integer> getNumberOfAllOwners(@PathVariable String path) {
        
        logger.info("Entered REST getNumberOfAllOwners...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.numberOfAllOwners(currentUser, path));
    }

    /**
     * Returns all duplicated owners in a grouping with their sources of ownership.
     * (Either both a direct owner and indirect owner, or multiple indirect owners via different owner-groupings.)
     */
    @GetMapping(value = "/groupings/{path:[\\w-:.]+}/owners/compare")
    public ResponseEntity<Map<String, Map<String, List<String>>>> compareOwnerGroupings(@PathVariable String path) {
        logger.info("Entered REST compareOwnerGroupings...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.compareOwnerGroupings(currentUser, path));
    }

    /**
     * A list of immediate owners listed in a grouping.
     * Owners with "IMMEDIATE" filter.
     */
    @GetMapping(value = "/grouping/{path:[\\w-:.]+}/owners")
    public ResponseEntity<GroupingOwnerMembers> groupingOwners(@PathVariable String path) {
        logger.info("Entered REST groupingOwners...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .ok()
                .body(groupingAssignmentService.groupingImmediateOwners(currentUser, path));
    }

    /**
     * Get result of async job.
     */
    @GetMapping(value = "/jobs/{jobId}")
    public ResponseEntity<AsyncJobResult> getAsyncJobResult(@PathVariable Integer jobId) {
        logger.debug("Entered REST getAsyncJobResult...");
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
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