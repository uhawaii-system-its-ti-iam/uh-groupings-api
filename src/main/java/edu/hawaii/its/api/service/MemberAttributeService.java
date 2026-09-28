package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.hawaii.its.api.groupings.GroupingPaths;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.UhIdentifierValidationResult;

@Service("memberAttributeService")
public class MemberAttributeService {

    private static final Log logger = LogFactory.getLog(MemberAttributeService.class);

    @Value("${groupings.api.failure}")
    private String FAILURE;

    private final SubjectService subjectService;

    private final MemberService memberService;

    private final GroupingsService groupingsService;

    public MemberAttributeService(SubjectService subjectService,
            MemberService memberService,
            GroupingsService groupingsService) {
        this.subjectService = subjectService;
        this.memberService = memberService;
        this.groupingsService = groupingsService;
    }

    /**
     * Get a mapping of user attributes (composite name, uid, uhUuid) pertaining to the list of uid
     * or uhUuid passed through uhIdentifiers. If any uhIdentifier is invalid (malformed, or unknown to Grouper),
     * only the invalid uhIdentifiers are returned, in full, and no attributes are.
     */
    public MemberAttributeResults getMemberAttributeResults(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("getMemberAttributeResults; currentUser: %s; uhIdentifiers: %s;", currentUser, uhIdentifiers));
        // Use JWT for general role checks instead of querying Grouper
        if (!memberService.isCurrentUserAdmin() && !memberService.isCurrentUserOwner()) {
            throw new AccessDeniedException();
        }
        return resolveMemberAttributeResults(currentUser, uhIdentifiers);
    }

    /**
     * Get a mapping of user attributes (composite name, uid, uhUuid) pertaining to the list of uid
     * or uhUuid passed through uhIdentifiers asynchronously. If any uhIdentifier is invalid (malformed, or unknown
     * to Grouper), only the invalid uhIdentifiers are returned, in full, and no attributes are.
     */
    @Async
    public CompletableFuture<MemberAttributeResults> getMemberAttributeResultsAsync(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("getMemberAttributeResultsAsync; currentUser: %s; uhIdentifiers: %s;", currentUser,
                uhIdentifiers));
        // Use JWT for general role checks instead of querying Grouper
        if (!memberService.isCurrentUserAdmin() && !memberService.isCurrentUserOwner()) {
            throw new AccessDeniedException();
        }
        return CompletableFuture.completedFuture(resolveMemberAttributeResults(currentUser, uhIdentifiers));
    }

    /**
     * Malformed identifiers are reported as invalid, like unknown ones, instead of failing the whole request:
     * a bulk import can contain any number of them (e.g. "12-345-678") and needs the full list of invalid
     * identifiers back to report. All identifiers are checked with a single bulk Grouper lookup: the subjects
     * that lookup already resolved are reused for the attribute results below rather than looked up again.
     */
    private MemberAttributeResults resolveMemberAttributeResults(String currentUser, List<String> uhIdentifiers) {
        UhIdentifierValidationResult validationResult = subjectService.validateUhIdentifiers(currentUser, uhIdentifiers);
        if (!validationResult.getInvalidIdentifiers().isEmpty()) {
            return new MemberAttributeResults(validationResult.getInvalidIdentifiers());
        }
        return MemberAttributeResults.forValidSubjects(validationResult.getValidSubjects());
    }

    /**
     * Get a list of GroupPaths the user owns, by uid or uhUuid.
     */
    public GroupingPaths getOwnedGroupings(String currentUser) {
        logger.info(String.format("getOwnedGroupings; currentUser: %s;", currentUser));
        List<String> pathStrings = groupingsService.groupPaths(currentUser, pathHasOwner());
        List<GroupingPath> groupingPaths = new ArrayList<>();
        for (String path : pathStrings) {
            String parentGroupingPath = parentGroupingPath(path);
            groupingPaths.add(new GroupingPath(parentGroupingPath,
                    groupingsService.getGroupingDescription(parentGroupingPath)));
        }

        return new GroupingPaths(groupingPaths);
    }

    /**
     * Get the number of groupings a user owns, by uid or uhUuid.
     */
    public Integer numberOfGroupings(String uhIdentifier) {
        logger.debug(String.format("numberOfGroupings; uhIdentifier: %s;", uhIdentifier));
        return groupingsService.groupPaths(uhIdentifier, pathHasOwner()).size();
    }

    public Integer numberOfGroupings(String uhIdentifier, String groupPath) {
        logger.debug(String.format("numberOfGroupings; uhIdentifier: %s;", uhIdentifier));
        return groupingsService.groupPaths(uhIdentifier, pathHasOwner()).size();
    }
}
