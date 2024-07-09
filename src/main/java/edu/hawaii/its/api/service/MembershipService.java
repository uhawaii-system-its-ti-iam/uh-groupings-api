package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.disjoint;
import static edu.hawaii.its.api.service.PathFilter.nameGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasExclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.groupings.MembershipResults;
import edu.hawaii.its.api.groupings.ManageSubjectResults;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.ManageSubjectResult;
import edu.hawaii.its.api.type.MembershipResult;
import edu.hawaii.its.api.wrapper.Group;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

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
     * Get a list of memberships pertaining to uid. A list of memberships is made up from the groups listings of
     * (basis + include) - exclude.
     */
    public MembershipResults membershipResults(String currentUser, String uid) {
        logger.info(String.format("membershipResults; currentUser: %s; uid: %s;", currentUser, uid));

        if (!memberService.isAdmin(currentUser) && !currentUser.equals(uid)) {
            throw new AccessDeniedException();
        }
        String uhUuid = subjectService.getValidUhUuid(uid);
        if (uhUuid.equals("")) {
            throw new UhMemberNotFoundException(uid);
        }
        // Get all basis, include and exclude paths from grouper.
        List<String> basisIncludeExcludePaths =
                groupingsService.groupPaths(uid, pathHasBasis().or(pathHasInclude().or(pathHasExclude())));
        // Get all basis and include paths to check the opt-out attribute.
        List<String> basisAndInclude =
                groupingsService.filterGroupPaths(basisIncludeExcludePaths, pathHasBasis().or(pathHasInclude()));
        // Get all exclude paths for the disjoint.
        List<String> excludePaths = groupingsService.filterGroupPaths(basisIncludeExcludePaths, pathHasExclude());
        // The disjoint of basis plus include and exclude: (Basis + Include) - Exclude
        List<String> groupingMembershipPaths = disjoint(parentGroupingPaths(basisIncludeExcludePaths),
                parentGroupingPaths(excludePaths));
        // Send all the grouping Membership paths to grouper to obtain grouping descriptions.
        List<Group> membershipGroupings = groupPathService.getValidGroupings(groupingMembershipPaths);
        // Get a list of groupings paths of all basis and include groups that have the opt-out attribute.
        List<String> optOutList = groupingsService.optOutEnabledGroupingPaths(parentGroupingPaths(basisAndInclude));

        List<MembershipResult> memberships = createMemberships(membershipGroupings, optOutList);
        return new MembershipResults(memberships);
    }

    private List<MembershipResult> createMemberships(List<Group> membershipGroupings, List<String> optOutList) {
        List<MembershipResult> memberships = new ArrayList<>();
        for (Group grouping : membershipGroupings) {
            MembershipResult membershipResult = new MembershipResult();
            membershipResult.setDescription(grouping.getDescription());
            membershipResult.setPath(grouping.getGroupPath());
            membershipResult.setName(nameGroupingPath(grouping.getGroupPath()));
            membershipResult.setOptOutEnabled(optOutList.contains(grouping.getGroupPath()));
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
        if (uhUuid.equals("")) {
            return manageSubjectResults;
        }
        List<String> groupPaths;
        try {
            groupPaths = groupingsService.allGroupPaths(uid);
        } catch (GcWebServiceError e) {
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
    public Integer numberOfMemberships(String currentUser, String uid) {
        logger.debug(String.format("numberOfMemberships; currentUser: %s; uid: %s;", currentUser, uid));
        return membershipResults(currentUser, uid).getResults().size();
    }
}
