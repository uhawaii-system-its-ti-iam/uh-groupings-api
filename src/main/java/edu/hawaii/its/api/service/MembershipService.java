package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.wrapper.Group;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.hawaii.its.api.service.PathFilter.disjoint;
import static edu.hawaii.its.api.service.PathFilter.nameGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.parentGroupingPaths;
import static edu.hawaii.its.api.service.PathFilter.pathHasBasis;
import static edu.hawaii.its.api.service.PathFilter.pathHasExclude;
import static edu.hawaii.its.api.service.PathFilter.pathHasInclude;

@Service("membershipService")
public class MembershipService {

    public static final Log logger = LogFactory.getLog(MembershipService.class);

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private GroupingsService groupingsService;

    @Autowired
    private GroupPathService groupPathService;

    @Autowired
    private MemberService memberService;

    /**
     * Get a list of memberships pertaining to uid. A list of memberships is made up from the groups listings of
     * (basis + include) - exclude.
     */
    public List<Membership> membershipResults(String currentUser, String uid) {
        String action = "membershipResults; currentUser: " + currentUser + "; uid: " + uid + ";";
        logger.info(action);

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
        System.out.println("dumb"+ membershipGroupings);

        // Get a list of groupings paths of all basis and include groups that have the opt-out attribute.
        List<String> optOutList = groupingsService.optOutEnabledGroupingPaths(parentGroupingPaths(basisAndInclude));
        return createMemberships(membershipGroupings, optOutList);
    }

    private List<Membership> createMemberships(List<Group> membershipGroupings, List<String> optOutList) {
        List<Membership> memberships = new ArrayList<>();
        for (Group grouping : membershipGroupings) {
            Membership membership = new Membership();
            membership.setDescription(grouping.getDescription());
            membership.setPath(grouping.getGroupPath());
            membership.setName(nameGroupingPath(grouping.getGroupPath()));
            membership.setOptOutEnabled(optOutList.contains(grouping.getGroupPath()));
            memberships.add(membership);
        }
        return memberships;
    }

    /**
     * Get a list of all groupings pertaining to uid (nonfiltered).
     */
    public List<Membership> managePersonResults(String currentUser, String uid) {
        String action = "managePersonResults; currentUser: " + currentUser + "; uid: " + uid + ";";
        logger.info(action);
        if (!memberService.isAdmin(currentUser) && !currentUser.equals(uid)) {
            throw new AccessDeniedException();
        }
        List<Membership> memberships = new ArrayList<>();
        String uhUuid = subjectService.getValidUhUuid(uid);
        if (uhUuid.equals("")) {
            return memberships;
        }
        List<String> groupPaths;
        List<String> optOutList;
        try {
            groupPaths = groupingsService.allGroupPaths(uid);
            optOutList = groupingsService.optOutEnabledGroupingPaths();
        } catch (GcWebServiceError e) {
            logger.warn("membershipResults;" + e);
            return memberships;
        }

        return createMembershipList(groupPaths, optOutList, memberships);
    }

    /**
     * Helper - membershipResults, managePersonResults
     */
    private List<Membership> createMembershipList(List<String> groupPaths, List<String> optOutList,
            List<Membership> memberships) {
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

        for (Group group : groupingMemberships) {
            String groupingPath = group.getGroupPath();
            List<String> paths = pathMap.get(groupingPath);
            Membership membership = subgroups(paths);
            membership.setPath(groupingPath);
            membership.setOptOutEnabled(optOutList.contains(groupingPath));
            membership.setName(nameGroupingPath(groupingPath));
            membership.setDescription(group.getDescription());
            memberships.add(membership);
        }
        return memberships;
    }

    /**
     * Helper - membershipResults, managePersonResults
     */
    private Membership subgroups(List<String> paths) {
        Membership membership = new Membership();
        for (String path : paths) {
            if (path.endsWith(GroupType.BASIS.value())) {
                membership.setInBasis(true);
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
        return membershipResults(currentUser, uid).size();
    }
}
