package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import edu.hawaii.its.api.groupings.GroupingPaths;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@Service("memberAttributeService")
public class MemberAttributeService {

    private static final Log logger = LogFactory.getLog(MemberAttributeService.class);

    @Value("${groupings.api.failure}")
    private String FAILURE;

    private final GrouperService grouperService;

    private final SubjectService subjectService;

    private final MemberService memberService;

    private final GroupingsService groupingsService;

    public MemberAttributeService(GrouperService grouperService,
            SubjectService subjectService,
            MemberService memberService,
            GroupingsService groupingsService) {
        this.grouperService = grouperService;
        this.subjectService = subjectService;
        this.memberService = memberService;
        this.groupingsService = groupingsService;
    }

    /**
     * Get a mapping of user attributes (composite name, uid, uhUuid) pertaining to the list of uid
     * or uhUuid passed through uhIdentifiers. Passing a single invalid uhIdentifier or current user will return an
     * empty array
     */
    public MemberAttributeResults getMemberAttributeResults(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("getMemberAttributeResults; currentUser: %s; uhIdentifiers: %s;", currentUser, uhIdentifiers));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }
        List<String> invalidUhIdentifiers = uhIdentifiers.parallelStream()
                .filter(uhIdentifier -> !subjectService.isValidIdentifier(uhIdentifier))
                .collect(Collectors.toList());
        if (!invalidUhIdentifiers.isEmpty()) {
            return new MemberAttributeResults(invalidUhIdentifiers);
        }
        SubjectsResults results = grouperService.getSubjects(uhIdentifiers);
        return new MemberAttributeResults(results);
    }

    /**
     * Get a mapping of user attributes (composite name, uid, uhUuid) pertaining to the list of uid
     * or uhUuid passed through uhIdentifiers asynchronously. Passing a single invalid uhIdentifier or current user will return an
     * empty array
     */
    @Async
    public CompletableFuture<MemberAttributeResults> getMemberAttributeResultsAsync(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("getMemberAttributeResultsAsync; currentUser: %s; uhIdentifiers: %s;", currentUser,
                uhIdentifiers));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }
        List<String> invalid = uhIdentifiers.parallelStream()
                .filter(uhIdentifier -> !subjectService.isValidIdentifier(uhIdentifier))
                .collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            return CompletableFuture.completedFuture(new MemberAttributeResults(invalid));
        }
        SubjectsResults results = grouperService.getSubjects(uhIdentifiers);
        return CompletableFuture.completedFuture(new MemberAttributeResults(results));
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
