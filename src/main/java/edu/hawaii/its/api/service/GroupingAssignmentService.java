package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.hawaii.its.api.groupings.GroupingGroupMember;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingOwnerMembers;
import edu.hawaii.its.api.groupings.GroupingPaths;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Subject;

@Service("groupingAssignmentService")
public class GroupingAssignmentService {

    private static final Log logger = LogFactory.getLog(GroupingAssignmentService.class);
    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;
    @Value("${groupings.api.stale_subject_id}")
    private String STALE_SUBJECT_ID;
    @Value("${groupings.max.owner.limit}")
    private Integer OWNERS_LIMIT;

    private final MemberService memberService;

    private final GrouperService grouperService;

    private final GroupingsService groupingsService;

    public GroupingAssignmentService(MemberService memberService,
            GrouperService grouperService,
            GroupingsService groupingsService) {
        this.memberService = memberService;
        this.grouperService = grouperService;
        this.groupingsService = groupingsService;
    }

    /**
     * A list of grouping paths for all groupings, restricted to admins' use only.
     */
    public GroupingPaths allGroupingPaths(String adminUhIdentifier) {
        logger.info(String.format("allGroupings; adminUhIdentifier: %s;", adminUhIdentifier));
        if (!memberService.isAdmin(adminUhIdentifier)) {
            throw new AccessDeniedException();
        }
        return new GroupingPaths(groupingsService.allGroupAttributeResults());
    }

    /**
     * Returns groupingsAdmins object containing the list of all admins.
     */
    public GroupingGroupMembers groupingAdmins(String adminUhIdentifier) {
        logger.info(String.format("groupingAdmins; adminUhIdentifier: %s;", adminUhIdentifier));
        if (!memberService.isAdmin(adminUhIdentifier)) {
            throw new AccessDeniedException();
        }
        return new GroupingGroupMembers(grouperService.getMembersResult(adminUhIdentifier, GROUPING_ADMINS));
    }

    /**
     * Returns a group from grouper or the database.
     */
    public Map<String, Group> getMembers(String ownerUid, List<String> groupPaths) {
        GetMembersResults getMembersResults =
                grouperService.getMembersResults(
                        ownerUid,
                        groupPaths,
                        null,
                        null,
                        null,
                        false);
        return makeGroups(getMembersResults);
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt out of.
     */
    public List<String> optOutGroupingsPaths(String owner, String optOutUid) {
        logger.info(String.format("optOutGroupingsPaths; owner: %s; optOutUid: %s;", owner, optOutUid));

        List<String> includes = groupingsService.groupPaths(optOutUid, pathHasInclude());
        includes = includes.stream().map(path -> parentGroupingPath(path)).collect(Collectors.toList());
        List<String> optOutPaths = groupingsService.optOutEnabledGroupingPaths();
        optOutPaths.retainAll(includes);
        return new ArrayList<>(new HashSet<>(optOutPaths));
    }

    /**
     * As a group owner, get a list of grouping paths pertaining to the groups which optInUid can opt into.
     */
    public GroupingPaths optInGroupingPaths(String owner, String optInUid) {
        logger.info(String.format("optInGroupingsPaths; owner: %s; optInUid: %s;", owner, optInUid));

        List<String> includes = groupingsService.groupPaths(optInUid, pathHasInclude());
        includes = includes.stream().map(path -> parentGroupingPath(path)).collect(Collectors.toList());

        List<String> optInPaths = groupingsService.optInEnabledGroupingPaths();
        optInPaths.removeAll(includes);
        optInPaths = new ArrayList<>(new HashSet<>(optInPaths));

        return new GroupingPaths(groupingsService.getGroupingPaths(optInPaths));
    }

    /**
     * Get number of immediate owners in a grouping. Owners with "IMMEDIATE" filter.
     * It includes direct owners + owner-groupings.
     */
    public Integer numberOfImmediateOwners(String currentUser, String groupPath, String uhIdentifier) {
        logger.debug(String.format("numberOfImmediateOwners; currentUser: %s; groupPath: %s; uidToCheck: %s;",
                currentUser, groupPath, uhIdentifier));
        if (!memberService.isOwner(groupPath, currentUser)) {
            throw new AccessDeniedException();
        }
        GroupingGroupMembers owners = groupingImmediateOwners(currentUser, groupPath).getOwners();

        return owners.getMembers().size();
    }

    /**
     * Get number of all owners in a grouping. Owners with "ALL" filter.
     * It includes direct owners + owner-groupings + indirect owners.
     */
    public Integer numberOfAllOwners(String currentUser, String groupPath) {
        logger.debug(String.format("numberOfAllOwners; currentUser: %s; groupPath: %s;",
                currentUser, groupPath));
        if (!memberService.isOwner(groupPath, currentUser)) {
            throw new AccessDeniedException();
        }
        GroupingGroupMembers owners = groupingAllOwners(currentUser, groupPath).getOwners();

        return owners.getMembers().size();
    }

    /**
     * Get number of direct owners in a grouping.
     */
    public Integer numberOfDirectOwners(String currentUser, String groupingPath) {
        logger.info(String.format("groupingDirectOwners; currentUser: %s; groupingPath: %s;",
                currentUser, groupingPath));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(groupingPath, currentUser)) {
            throw new AccessDeniedException();
        }
        GroupingOwnerMembers immediateOwners = new GroupingOwnerMembers(grouperService
                .getImmediateMembers(currentUser, groupingPath + GroupType.OWNERS.value()), OWNERS_LIMIT);

        Integer directOwnerCount = 0;
        for (GroupingGroupMember owner : immediateOwners.getOwners().getMembers()) {
            if (!owner.getName().contains(":"))
                directOwnerCount++;
        }
        return directOwnerCount;
    }

    /**
     * Compare direct owners and owner-groupings
     * Returns a Map of all owners with multiple ownerships and sources of ownership for those owners.
     * Map Structure: Map<String, Map<String, List<String>>>
     *      Outer Key (String): UH UUID of the owner
     *      Inner Map<String, List<String>> contains 3 key, value pairs:
     *          "name"  -> List<String>: Index 0 contains the owner's name
     *          "uid"   -> List<String>: Index 0 contains the owner's uid
     *          "paths" -> List<String>: Contains sources of ownership.
     */
    public Map<String, Map<String, List<String>>> compareOwnerGroupings(String currentUser, String groupPath) {
        logger.info(String.format("compareOwnerGroupings; currentUser: %s; groupPath: %s;",
                currentUser, groupPath));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(groupPath, currentUser)) {
            throw new AccessDeniedException();
        }
        GroupingGroupMembers immediateOwners = groupingImmediateOwners(currentUser, groupPath).getOwners();
        Map<String, Map<String, List<String>>> existingUhUuids = new HashMap<>();
        Map<String, Map<String, List<String>>> duplicates = new HashMap<>();
        ArrayList<String> ownerGroupings = new ArrayList<>();

        //iterate through immediate owners to log direct owners and owner-groupings
        for (GroupingGroupMember owner : immediateOwners.getMembers()) {
            String uhUuid = owner.getUhUuid();
            String name = owner.getName();
            if (owner.getName().contains(":")) {
                ownerGroupings.add(name);
                continue;
            }
            existingUhUuids.put(uhUuid, new HashMap<>());
            existingUhUuids.get(uhUuid).put("name", new ArrayList<>(List.of(owner.getName())));
            existingUhUuids.get(uhUuid).put("uid", new ArrayList<>(List.of(owner.getUid())));
            existingUhUuids.get(uhUuid).put("paths", new ArrayList<>(List.of("DIRECT")));
        }

        //iterate through each owner-grouping to find duplicate owners
        for (String path : ownerGroupings) {
            GroupingGroupMembers pathOwners = new GroupingGroupMembers(
                    grouperService.getMembersResult(currentUser, path));
            for (GroupingGroupMember owner : pathOwners.getMembers()) {
                String uhUuid = owner.getUhUuid();
                //copy and place it in duplicate map if already exists in existing map
                if (existingUhUuids.containsKey(uhUuid)) {
                    if (!duplicates.containsKey(uhUuid)) {
                        duplicates.put(uhUuid, existingUhUuids.get(uhUuid));
                    }
                    //add the owner-grouping path to the list of sources
                    duplicates.get(uhUuid).get("paths").add(path);
                } else {
                    //place it in existing map if seen first time
                    existingUhUuids.put(uhUuid, new HashMap<>());
                    existingUhUuids.get(uhUuid).put("name", new ArrayList<>(List.of(owner.getName())));
                    existingUhUuids.get(uhUuid).put("uid", new ArrayList<>(List.of(owner.getUid())));
                    existingUhUuids.get(uhUuid).put("paths", new ArrayList<>(List.of(path)));
                }
            }
        }
        return duplicates;
    }

    /**
     * All owners including direct, owner-groupings and indirect owners.
     */
    public GroupingOwnerMembers groupingAllOwners(String currentUser, String groupingPath) {
        logger.info(String.format("groupingAllOwners; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingOwnerMembers(
                grouperService.getAllMembers(currentUser, groupingPath + GroupType.OWNERS.value()), OWNERS_LIMIT);
    }

    /**
     * Direct owners + owner-groupings.
     */
    public GroupingOwnerMembers groupingImmediateOwners(String currentUser, String groupingPath) {
        logger.info(String.format("groupingImmediateOwners; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingOwnerMembers(
                grouperService.getImmediateMembers(currentUser, groupingPath + GroupType.OWNERS.value()), OWNERS_LIMIT);
    }

    public Map<String, Group> makeGroups(GetMembersResults getMembersResults) {
        Map<String, Group> groupMembers = new HashMap<>();
        List<GetMembersResult> membersResults = getMembersResults.getMembersResults();
        for (GetMembersResult membersResult : membersResults) {
            Group group = new Group(membersResult.getGroup().getGroupPath());
            List<Subject> subjects = membersResult.getSubjects();
            for (Subject subject : subjects) {
                if (!subject.hasUHAttributes()) {
                    continue;
                }
                if (group.getPath().endsWith(GroupType.BASIS.value()) && subject.getSourceId() != null
                        && subject.getSourceId()
                        .equals(STALE_SUBJECT_ID)) {
                    subject.setUid("User Not Available.");
                }
                group.addMember(subject);
            }
            groupMembers.put(group.getPath(), group);
        }
        return groupMembers;
    }
}
