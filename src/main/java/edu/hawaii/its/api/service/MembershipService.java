package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.nameGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.onlyGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhIdentifierNotFoundException;
import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.ManageSubjectResult;
import edu.hawaii.its.api.type.MembershipResult;
import edu.hawaii.its.api.wrapper.Group;

@Service("membershipService")
public class MembershipService {

    private static final Log logger = LogFactory.getLog(MembershipService.class);

    private final SubjectService subjectService;

    private final GroupingsService groupingsService;

    private final GroupPathService groupPathService;

    private final MemberService memberService;

    public MembershipService(SubjectService subjectService,
            GroupingsService groupingsService,
            GroupPathService groupPathService,
            MemberService memberService) {
        this.subjectService = subjectService;
        this.groupingsService = groupingsService;
        this.groupPathService = groupPathService;
        this.memberService = memberService;
    }

    /**
     * Get a list of memberships pertaining to the currentUser. Grouper's composite group already calculates
     * (basis + include) - exclude, so we use the composite grouping paths directly.
     */
    public MembershipResults membershipResults(String currentUser) {
        logger.info(String.format("membershipResults; currentUser: %s;", currentUser));

        String uhUuid = subjectService.getValidUhUuid(currentUser);
        if (uhUuid.isEmpty()) {
            throw new UhIdentifierNotFoundException(currentUser);
        }
        // Get all group paths the user is a member of from Grouper
        List<String> allGroupPaths = groupingsService.allGroupPaths(currentUser);
        // Filter to get only composite grouping paths (excluding :basis, :include, :exclude, :owners suffixes)
        // This leverages Grouper's composite group which already calculates (basis + include) - exclude
        List<String> groupingMembershipPaths = groupingsService.filterGroupPaths(allGroupPaths, onlyGroupingPaths());
        // Get all basis and include paths to check the opt-out attribute.
        List<String> basisAndInclude =
                groupingsService.filterGroupPaths(allGroupPaths, pathHasBasis().or(pathHasInclude()));
        // The list of all curated groupings
        List<String> curatedGroupingsPaths = groupingsService.curatedGroupings();
        // Intersect the two lists so curatedGroupingsPaths contains only paths the user is listed in
        curatedGroupingsPaths.retainAll(allGroupPaths);
        // Send all the grouping Membership paths to grouper to obtain grouping descriptions.
        List<Group> membershipGroupings = groupPathService.getValidGroupings(groupingMembershipPaths);
        // Get a list of groupings paths of all basis and include groups that have the opt-out attribute.
        List<String> optOutList = groupingsService.optOutEnabledGroupingPaths(parentGroupingPaths(basisAndInclude));

        List<MembershipResult> memberships =
                createMemberships(membershipGroupings, optOutList, curatedGroupingsPaths);
        return new MembershipResults(memberships);
    }

    private List<MembershipResult> createMemberships(List<Group> membershipGroupings, List<String> optOutList,
            List<String> curatedGroupingsPaths) {
        List<MembershipResult> memberships = new ArrayList<>();
        for (Group grouping : membershipGroupings) {
            MembershipResult membershipResult =
                    new MembershipResult(grouping.getGroupPath(), nameGroupingPath(grouping.getGroupPath()),
                            grouping.getDescription());
            membershipResult.setOptOutEnabled(optOutList.contains(membershipResult.getPath()));
            memberships.add(membershipResult);
        }
        for (String grouping : curatedGroupingsPaths) {
            MembershipResult membershipResult = new MembershipResult(grouping, grouping, "");
            memberships.add(membershipResult);
        }
        return memberships;
    }

    /**
     * Get a list of all groupings pertaining to uid (nonfiltered).
     */
    public ManageSubjectResults manageSubjectResults(String currentUser, String uid) {
        logger.info(String.format("manageSubjectResults; currentUser: %s; uid: %s;", currentUser, uid));
        if (!memberService.isAdmin(currentUser) && !currentUser.equals(uid)) {
            throw new AccessDeniedException();
        }
        ManageSubjectResults manageSubjectResults = new ManageSubjectResults();
        String uhUuid = subjectService.getValidUhUuid(uid);
        if (uhUuid.isEmpty()) {
            return manageSubjectResults;
        }
        List<String> groupPaths;
        try {
            groupPaths = groupingsService.allGroupPaths(uid);
        } catch (Exception e) {
            logger.warn("membershipResults;" + e);
            return manageSubjectResults;
        }

        return createMembershipList(groupPaths);
    }

    /**
     * Helper - membershipResults, manageSubjectResults
     */
    private ManageSubjectResults createMembershipList(List<String> groupPaths) {
        Map<String, List<String>> pathMap = new HashMap<>();

        for (String pathToCheck : groupPaths) {
            if (pathToCheck.endsWith(GroupType.INCLUDE.value())
                    || pathToCheck.endsWith(GroupType.EXCLUDE.value())
                    || pathToCheck.endsWith(GroupType.BASIS.value())
                    || pathToCheck.endsWith(GroupType.OWNERS.value())) {
                String parentPath = parentGroupingPath(pathToCheck);
                if (!pathMap.containsKey(parentPath)) {
                    pathMap.put(parentPath, new ArrayList<>());
                }
                pathMap.get(parentPath).add(pathToCheck);
            }
        }

        List<Group> groupingMemberships = groupPathService.getValidGroupings(new ArrayList<>(pathMap.keySet()));

        List<ManageSubjectResult> results = new ArrayList<>();

        for (Group group : groupingMemberships) {
            String groupingPath = group.getGroupPath();
            List<String> paths = pathMap.get(groupingPath);
            ManageSubjectResult manageSubjectResult = subgroups(paths);
            manageSubjectResult.setPath(groupingPath);
            manageSubjectResult.setName(nameGroupingPath(group.getGroupPath()));
            results.add(manageSubjectResult);
        }
        return new ManageSubjectResults(results);
    }

    /**
     * Helper - membershipResults, manageSubjectResults
     */
    private ManageSubjectResult subgroups(List<String> paths) {
        ManageSubjectResult membership = new ManageSubjectResult();
        for (String path : paths) {
            if (path.endsWith(GroupType.BASIS.value())) {
                membership.setInBasisAndInclude(true);
            }
            if (path.endsWith(GroupType.INCLUDE.value())) {
                membership.setInInclude(true);
            }
            if (path.endsWith(GroupType.EXCLUDE.value())) {
                membership.setInExclude(true);
            }
            if (path.endsWith(GroupType.OWNERS.value())) {
                membership.setInOwner(true);
            }
        }
        return membership;
    }

    /**
     * Get the number of memberships.
     */
    public Integer numberOfMemberships(String currentUser) {
        logger.debug(String.format("numberOfMemberships; currentUser: %s;", currentUser));
        return membershipResults(currentUser).getResults().size();
    }
}
