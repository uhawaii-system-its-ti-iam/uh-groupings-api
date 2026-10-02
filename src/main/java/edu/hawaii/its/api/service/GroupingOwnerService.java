package edu.hawaii.its.api.service;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.GroupingDescription;
import edu.hawaii.its.api.groupings.GroupingGroupMembers;
import edu.hawaii.its.api.groupings.GroupingGroupsMembers;
import edu.hawaii.its.api.groupings.GroupingMembers;
import edu.hawaii.its.api.groupings.GroupingOptAttributes;
import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.groupings.GroupingSyncDestinations;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.SortBy;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.Strings;
import edu.hawaii.its.api.wrapper.AttributesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * GroupingOwnerService contains all the necessary functions to hydrate a selected grouping.
 */
@Service("ownerService")
public class GroupingOwnerService {

    private static final Log log = LogFactory.getLog(GroupingOwnerService.class);

    @Value("${grouper.api.sync.destinations.location}")
    private String SYNC_DESTINATIONS_LOCATION;

    @Value("uh-settings:attributes:for-groups:uh-grouping:destinations:checkboxes")
    private String SYNC_DESTINATIONS_CHECKBOXES;

    private final GrouperService grouperService;

    private final MemberService memberService;

    private final EmailService emailService;

    public GroupingOwnerService(GrouperService grouperService, MemberService memberService, EmailService emailService) {
        this.grouperService = grouperService;
        this.memberService = memberService;
        this.emailService = emailService;
    }

    /**
     * Get the number of grouping members: Basis + Include - Exclude.
     */
    public Integer numberOfGroupingMembers(String currentUser, String groupingPath) {
        log.debug(String.format("numberOfGroupingMembers; currentUser: %s; groupingPath: %s;", currentUser,
                groupingPath));
        GetMembersResult getMembersResult = grouperService.getMembersResult(currentUser, groupingPath);
        return getMembersResult.getSubjects().size();
    }

    /**
     * Get all members listed in the groups in groupsPath. This should be used iteratively from the UI to get all
     * members of a grouping.
     */
    public GroupingGroupsMembers paginatedGrouping(String currentUser, List<String> groupPaths, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        log.debug(String.format(
                "paginatedGrouping; currentUser: %s; groupPaths: %s; pageNumber: %s; pageSize: %s; sortString: %s; isAscending: %b;",
                currentUser, groupPaths, pageNumber, pageSize, sortString, isAscending));
        validatePagination(pageNumber, pageSize);
        GetMembersResults getMembersResults = grouperService.getMembersResults(
                currentUser,
                groupPaths,
                pageNumber,
                pageSize,
                sortString,
                isAscending);
        GroupingGroupsMembers groupingGroupsMembers = new GroupingGroupsMembers(getMembersResults);
        groupingGroupsMembers.setPageNumber(pageNumber);

        return groupingGroupsMembers;
    }

    public GroupingGroupMembers getGroupingMembers(String currentUser, String groupingPath, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending) {
        log.debug(String.format(
                "getGroupingMembers; currentUser: %s; groupingPath: %s; pageNumber: %d; pageSize: %d; sortString: %s; isAscending: %b;",
                currentUser, groupingPath, pageNumber, pageSize, sortString, isAscending));
        validatePagination(pageNumber, pageSize);
        GetMembersResult getMembersResult = grouperService.getMembersResult(
                currentUser,
                groupingPath,
                pageNumber,
                pageSize,
                sortString,
                isAscending);
        return new GroupingGroupMembers(getMembersResult);
    }

    public GroupingGroupMembers getGroupingMembers(String currentUser, String groupingPath, Integer pageNumber,
            Integer pageSize, String sortString, Boolean isAscending, String searchString) {
        log.debug(String.format(
                "getGroupingMembers; currentUser: %s; groupingPath: %s; pageNumber: %d; pageSize: %d; sortString: %s; isAscending: %b; searchString: %s;",
                currentUser, groupingPath, pageNumber, pageSize, sortString, isAscending, searchString));

        // groupingPath may be a composite path (e.g. "grouping:owners"), so use the parent
        // grouping path for the ownership check to avoid appending ":owners" twice.
        if (!memberService.isCurrentUserAdmin()
                && !memberService.isOwner(parentGroupingPath(groupingPath), currentUser)) {
            throw new AccessDeniedException();
        }

        validatePagination(pageNumber, pageSize);

        if (Strings.isEmpty(searchString)) {
            return getGroupingMembers(currentUser, groupingPath, pageNumber, pageSize, sortString, isAscending);
        }

        SubjectsResults subjectsResults = grouperService.getSubjects(groupingPath, searchString);
        SortBy sortBy = SortBy.find(sortString);

        return new GroupingGroupMembers(subjectsResults).sort(sortBy, isAscending).paginate(pageNumber, pageSize);
    }

    private void validatePagination(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) {
            throw new IllegalArgumentException("pageNumber must be provided");
        }
        if (pageSize == null) {
            throw new IllegalArgumentException("pageSize must be provided");
        }
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
    }

    public GroupingMembers getGroupingMembersWhereListed(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        HasMembersResults hasMembersResultsBasis = grouperService.hasMembersResults(currentUser,
                groupingPath + GroupType.BASIS.value(), uhIdentifiers);
        HasMembersResults hasMembersResultsInclude = grouperService.hasMembersResults(currentUser,
                groupingPath + GroupType.INCLUDE.value(), uhIdentifiers);

        return new GroupingMembers(hasMembersResultsBasis, hasMembersResultsInclude);
    }

    public GroupingMembers getGroupingMembersIsBasis(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(currentUser,
                groupingPath + GroupType.BASIS.value(), uhIdentifiers);
        return new GroupingMembers(hasMembersResults);
    }

    public GroupingMembers getMembersExistInInclude(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(
                currentUser,
                groupingPath + GroupType.INCLUDE.value(),
                uhIdentifiers);

        List<HasMemberResult> filteredList = hasMembersResults.getExistingMembers();

        return GroupingMembers.fromFilteredResults(filteredList);

    }

    public GroupingMembers getMembersExistInExclude(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(
                currentUser,
                groupingPath + GroupType.EXCLUDE.value(),
                uhIdentifiers);

        List<HasMemberResult> filteredList = hasMembersResults.getExistingMembers();

        return GroupingMembers.fromFilteredResults(filteredList);
    }

    public GroupingMembers getMembersExistInOwners(String currentUser, String groupingPath,
            List<String> uhIdentifiers) {
        HasMembersResults hasMembersResults = grouperService.hasMembersResults(
                currentUser,
                groupingPath + GroupType.OWNERS.value(),
                uhIdentifiers);

        List<HasMemberResult> filteredList = hasMembersResults.getExistingMembers();

        return GroupingMembers.fromFilteredResults(filteredList);
    }

    /**
     * Get the opt attributes of a selected grouping.
     */
    public GroupingOptAttributes groupingOptAttributes(String currentUser, String groupingPath) {
        log.debug(
                String.format("groupingOptAttributes; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingOptAttributes(grouperService.groupAttributeResult(currentUser, groupingPath));
    }

    /**
     * Get the description of a selected grouping.
     */
    public GroupingDescription groupingsDescription(String currentUser, String groupingPath) {
        log.debug(String.format("groupingsDescription; currentUser: %s; groupingPath: %s;", currentUser, groupingPath));
        return new GroupingDescription(grouperService.findGroupsResults(currentUser, groupingPath).getGroup());
    }

    /**
     * Get a list of sync-destinations for a selected grouping.
     */
    public GroupingSyncDestinations groupingsSyncDestinations(String currentUser, String groupingPath) {
        log.debug(String.format("groupingsSyncDestinations; currentUser: %s; groupingPath: %s;", currentUser,
                groupingPath));
        GroupAttributeResults groupAttributeResults;
        try {
            FindAttributesResults findAttributesResults = grouperService.findAttributesResults(
                    currentUser,
                    SYNC_DESTINATIONS_CHECKBOXES,
                    SYNC_DESTINATIONS_LOCATION);

            groupAttributeResults = grouperService.groupAttributeResults(
                    currentUser,
                    findAttributesResults.getResults().stream().map(AttributesResult::getName)
                            .collect(Collectors.toList()),
                    groupingPath);

            List<GroupingSyncDestination> syncDestinationList =
                    createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults, groupingPath);

            return new GroupingSyncDestinations(findAttributesResults, groupAttributeResults, syncDestinationList);
        } catch (Exception e) {
            log.error(String.format("groupingsSyncDestinations; currentUser: %s; groupingPath: %s; error: %s;",
                    currentUser, groupingPath, e.getMessage()), e);
            sendSyncDestinationErrorEmail(e);
            return new GroupingSyncDestinations();
        }
    }

    /**
     * Create a list of groupingSyncDestination with findAttributesResults and groupAttributeResults
     */
    public List<GroupingSyncDestination> createGroupingSyncDestinationList(FindAttributesResults
            findAttributesResults, GroupAttributeResults groupAttributeResults) {
        return createGroupingSyncDestinationList(findAttributesResults, groupAttributeResults, null);
    }

    public List<GroupingSyncDestination> createGroupingSyncDestinationList(FindAttributesResults
            findAttributesResults, GroupAttributeResults groupAttributeResults, String groupingPath) {
        List<AttributesResult> attributesResults = findAttributesResults.getResults();
        List<GroupingSyncDestination> syncDestinationList = new ArrayList<>();
        List<Exception> syncDestinationErrors = new ArrayList<>();
        List<String> syncDestinationErrorMessages = new ArrayList<>();
        String groupPath = groupAttributeResults.getGroups().stream()
                .findFirst()
                .map(Group::getGroupPath)
                .filter(path -> path != null && !path.isBlank())
                .orElse(groupingPath != null ? groupingPath : "");
        String groupName = groupAttributeResults.getGroups().stream()
                .findFirst()
                .map(Group::getExtension)
                .filter(extension -> extension != null && !extension.isBlank())
                .orElseGet(() -> {
                    if (groupPath == null || groupPath.isBlank()) {
                        return "";
                    }
                    int lastColon = groupPath.lastIndexOf(':');
                    return lastColon >= 0 ? groupPath.substring(lastColon + 1) : groupPath;
                });
        for (AttributesResult attributesResult : attributesResults) {
            String name = attributesResult.getName();
            try {
                String rawDescription = attributesResult.getDescription();
                if (rawDescription == null || rawDescription.isBlank()) {
                    throw new IllegalArgumentException(
                            "description field is null or blank — cannot deserialize GroupingSyncDestination");
                }

                GroupingSyncDestination groupingSyncDestination =
                        JsonUtil.asObject(rawDescription, GroupingSyncDestination.class);

                if (groupingSyncDestination == null) {
                    throw new IllegalStateException(
                            "JsonUtil.asObject returned null — description may contain the literal string \"null\"");
                }

                groupingSyncDestination.setName(name);

                String destinationDescription = groupingSyncDestination.getDescription();
                if (destinationDescription == null) {
                    throw new IllegalStateException(
                            "deserialized GroupingSyncDestination has a null description field — "
                                    + "JSON is missing the \"description\" property");
                }
                String resolvedDescription = destinationDescription
                        .replace("${srhfgs}", groupName)
                        .replace("#${srhfgs}", "#" + groupName)
                        .replace("#uh-iam-group", "#" + groupName)
                        .replace("uh-iam-group", groupName);
                groupingSyncDestination.setDescription(resolvedDescription);

                if (groupingSyncDestination.getTooltip() != null) {
                    groupingSyncDestination.setTooltip(groupingSyncDestination.getTooltip()
                            .replace("${srhfgs}", groupName)
                            .replace("#${srhfgs}", "#" + groupName)
                            .replace("#uh-iam-group", "#" + groupName)
                            .replace("uh-iam-group", groupName));
                }
                boolean referencesGrouping = groupName.isBlank()
                        || resolvedDescription.contains(groupName)
                        || (!groupPath.isBlank() && resolvedDescription.contains(groupPath.substring(
                        Math.max(groupPath.lastIndexOf(':') + 1, 0))));
                boolean shouldFilterByGrouping = groupingPath != null && !groupingPath.isBlank();

                if (!shouldFilterByGrouping) {
                    referencesGrouping = true;
                }

                if (!name.contains("uhReleasedGrouping") && !referencesGrouping) {
                    log.info(String.format("Skipping sync destination '%s' because it does not reference grouping '%s'",
                            name, groupName));
                    continue;
                }
                groupingSyncDestination.setSynced(groupAttributeResults.getGroupAttributes().stream()
                        .anyMatch(groupAttribute -> groupAttribute.getAttributeName()
                                .equals(attributesResult.getName())));
                syncDestinationList.add(groupingSyncDestination);

            } catch (Exception e) {
                log.error(String.format("createGroupingSyncDestinationList; skipping sync destination '%s': %s",
                        name, e.getMessage()), e);
                syncDestinationErrors.add(e);
                syncDestinationErrorMessages.add(String.format("'%s': %s", name, e.getMessage()));
            }
        }
        if (!syncDestinationErrors.isEmpty()) {
            sendSyncDestinationErrorEmail(
                    createSyncDestinationError(syncDestinationErrors, syncDestinationErrorMessages));
        }
        if (groupingPath != null && !groupingPath.isBlank() && !groupName.isBlank()) {
            syncDestinationList = syncDestinationList.stream()
                    .filter(destination -> {
                        String destinationName = destination.getName();
                        if (destinationName != null && destinationName.contains("uhReleasedGrouping")) {
                            return true;
                        }
                        String destinationDescription =
                                destination.getDescription() == null ? "" : destination.getDescription();
                        String destinationTooltip =
                                destination.getTooltip() == null ? "" : destination.getTooltip();
                        String destinationText = destinationDescription + " " + destinationTooltip;
                        boolean referencesGrouping = destinationText.contains(groupName)
                                || (!groupPath.isBlank() && destinationText.contains(groupPath.substring(
                                Math.max(groupPath.lastIndexOf(':') + 1, 0))));
                        return referencesGrouping;
                    })
                    .collect(Collectors.toList());
        }

        if (findAttributesResults != null && findAttributesResults.getResults() != null) {
            List<String> validNames = syncDestinationList.stream()
                    .map(GroupingSyncDestination::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<AttributesResult> filteredResults = new ArrayList<>(findAttributesResults.getResults())
                    .stream()
                    .filter(r -> r != null && r.getName() != null && validNames.contains(r.getName()))
                    .collect(Collectors.toList());






        }
        syncDestinationList.sort(Comparator.comparing(GroupingSyncDestination::getDescription));
        return syncDestinationList;
    }

    private Exception createSyncDestinationError(List<Exception> syncDestinationErrors,
            List<String> syncDestinationErrorMessages) {
        Exception syncDestinationError = new RuntimeException(String.format(
                "Skipped %d malformed sync destination(s): %s",
                syncDestinationErrors.size(),
                String.join("; ", syncDestinationErrorMessages)));
        syncDestinationErrors.forEach(syncDestinationError::addSuppressed);
        return syncDestinationError;
    }

    private void sendSyncDestinationErrorEmail(Exception e) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String path = attributes != null && attributes.getRequest() != null
                    ? attributes.getRequest().getRequestURI()
                    : "unknown";
            emailService.sendWithStack(e, "Sync Destination Error", path);
        } catch (Exception emailException) {
            log.error(String.format("sendSyncDestinationErrorEmail; failed to send error email: %s",
                    emailException.getMessage()), emailException);
        }
    }
}
