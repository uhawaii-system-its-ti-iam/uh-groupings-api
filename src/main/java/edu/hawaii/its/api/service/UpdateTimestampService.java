package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.groupings.GroupingResult;
import edu.hawaii.its.api.groupings.GroupingTimestampResults;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.UpdateTimestampCommand;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResults;

/**
 * Update a groupings timestamp attribute. A groupings timestamp should only be updated after certain queries are
 * performed with grouper (see https://uhawaii.atlassian.net/wiki/spaces/SITARd/pages/12944638/UH+Groupings+Design+2.x).
 * After the query occurs the result data should be set to a class which implements GroupingsResult, which
 * can be passed to update the timestamp.
 */
@Service("timestampService")
public class UpdateTimestampService {
    private static final Log logger = LogFactory.getLog(MembershipService.class);

    private final GroupPathService groupPathService;

    private final ExecutorService exec;

    public UpdateTimestampService(GroupPathService groupPathService, ExecutorService exec) {
        this.groupPathService = groupPathService;
        this.exec = exec;
    }

    public GroupingTimestampResults update(GroupingResult groupingResult) {
        if (groupingResult.getResultCode().equals("SUCCESS")) {
            List<String> groupPaths = getGroupingGroupPaths(groupingResult.getGroupPath());
            return updateLastModifiedTimestamp(groupPaths);
        }
        return new GroupingTimestampResults();
    }

    private GroupingTimestampResults updateLastModifiedTimestamp(List<String> groupPaths) {
        UpdatedTimestampResults updatedTimestampResults = exec.execute(new UpdateTimestampCommand()
                .addGroupPaths(groupPaths)
                .setRetry(true));
        GroupingTimestampResults groupingsTimestampResults = new GroupingTimestampResults(updatedTimestampResults);
        logger.debug("GroupingsTimestampResult; + " + JsonUtil.asJson(groupingsTimestampResults));
        return groupingsTimestampResults;
    }

    private List<String> getGroupingGroupPaths(String groupPath) {
        List<String> groupPaths = new ArrayList<>();
        if (groupPathService.isOwnersGroupPath(groupPath)) {
            groupPaths.add(groupPathService.getGroupingPath(groupPath));
        }
        groupPaths.add(groupPath);
        return groupPaths;
    }

}
