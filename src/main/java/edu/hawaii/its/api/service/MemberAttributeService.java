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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

@Service("memberAttributeService")
public class MemberAttributeService {

    public static final Log logger = LogFactory.getLog(MemberAttributeService.class);

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsService groupingsService;

    /**
     * Get a list of invalid uhIdentifiers given a list of uhIdentifiers.
     * Returns an empty list if all uhIdentifiers are valid.
     */
    public List<String> invalidUhIdentifiers(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("invalidUhIdentifiers; currentUser: %s; uhIdentifiers: %s;", currentUser,
                uhIdentifiers));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }

        return uhIdentifiers.parallelStream()
                .filter(uhIdentifier -> !subjectService.isValidIdentifier(uhIdentifier))
                .collect(Collectors.toList());
    }

    /**
     * Get a list of invalid uhIdentifiers given a list of uhIdentifiers asynchronously.
     * Returns an empty list if all uhIdentifiers are valid.
     */
    @Async
    public CompletableFuture<List<String>> invalidUhIdentifiersAsync(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("invalidUhIdentifiersAsync; currentUser: %s; uhIdentifiers: %s;", currentUser,
                uhIdentifiers));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }

        return CompletableFuture.supplyAsync(() ->
            uhIdentifiers.parallelStream()
                    .filter(uhIdentifier -> !subjectService.isValidIdentifier(uhIdentifier))
                    .collect(Collectors.toList())
        );
    }

    /**
     * Get a mapping of all user attributes (uid, composite name, last name, first name, uhUuid) pertaining to the uid
     * or uhUuid passed through uhIdentifier. Passing an invalid uhIdentifier or current user will return a mapping
     * with null values.
     */
    public Person getMemberAttributes(String currentUser, String uhIdentifier) {
        logger.info(String.format("getMemberAttributes; currentUser: %s; uhIdentifier: %s;", currentUser, uhIdentifier));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }
        return subjectService.getPerson(uhIdentifier);
    }

    /**
     * Get a mapping of user attributes (composite name, uid, uhUuid) pertaining to the list of uid
     * or uhUuid passed through uhIdentifiers. Passing a single invalid uhIdentifier or current user will return an
     * empty array
     */
    public List<Subject> getMembersAttributes(String currentUser, List<String> uhIdentifiers) {
        logger.info(String.format("getMembersAttributes; currentUser: %s; uhIdentifiers: %s;", currentUser, uhIdentifiers));
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }

        SubjectsResults results = grouperApiService.getSubjects(uhIdentifiers);

        if (results.getResultCode().equals(FAILURE)) {
            return new ArrayList<>();
        }

        List<Subject> subjects = results.getSubjects();

        return subjects;
    }

    /**
     * Get a list of GroupPaths the user owns, by username or uhUuid.
     */
    public GroupingPaths getOwnedGroupings(String currentUser, String uhIdentifier) {
        logger.info(String.format("getOwnedGroupings; currentUser: %s; uhIdentifier: %s;", currentUser, uhIdentifier));
        List<String> pathStrings = groupingsService.groupPaths(uhIdentifier, pathHasOwner());

        List<GroupingPath> groupingPaths = new ArrayList<>();
        for (String path : pathStrings) {
            String parentGroupingPath = parentGroupingPath(path);
            groupingPaths.add(new GroupingPath(parentGroupingPath,
                    groupingsService.getGroupingDescription(parentGroupingPath)));
        }
        return new GroupingPaths(groupingPaths);
    }

    /**
     * Get the number of groupings a user owns, by username or uhUuid.
     */
    public Integer numberOfGroupings(String currentUser, String uhIdentifier) {
        logger.debug(String.format("numberOfGroupings; currentUser: %s; uhIdentifier: %s;", currentUser, uhIdentifier));
        return groupingsService.groupPaths(uhIdentifier, pathHasOwner()).size();
    }
}
