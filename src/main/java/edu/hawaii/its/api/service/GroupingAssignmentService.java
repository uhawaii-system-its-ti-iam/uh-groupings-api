package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     */
    public Integer numberOfImmediateOwners(String currentUser, String groupPath, String uhIdentifier) {
        logger.debug(String.format("isSoleOwner; currentUser: %s; groupPath: %s; uidToCheck: %s;",
                currentUser, groupPath, uhIdentifier));
        if (!memberService.isOwner(uhIdentifier)) {
            throw new AccessDeniedException();
        }
        GroupingGroupMembers owners = groupingImmediateOwners(currentUser, groupPath).getOwners();

        return owners.getMembers().size();
    }

    /**
     * Get number of all owners in a grouping. Owners with "ALL" filter.
     */
    public Integer numberOfAllOwners(String currentUser, String groupPath) {
        logger.debug(String.format("isSoleOwner; currentUser: %s; groupPath: %s;",
                currentUser, groupPath));
        if (!memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }
        GroupingGroupMembers owners = groupingAllOwners(currentUser, groupPath).getOwners();

        return owners.getMembers().size();
    }

    public GroupingOwnerMembers groupingAllOwners(String currentUser, String groupingPath) {
        logger.info(String.format("groupingImmediateOwners; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingOwnerMembers(
                grouperService.getAllMembers(currentUser, groupingPath + GroupType.OWNERS.value()), OWNERS_LIMIT);
    }

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
