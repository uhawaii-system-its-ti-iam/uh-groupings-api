package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.groupings.GroupingResult;
import edu.hawaii.its.api.groupings.GroupingTimestampResult;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.UpdateTimestampCommand;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResult;

import org.springframework.stereotype.Service;

/**
 * Update a groupings timestamp attribute. A groupings timestamp should only be updated after certain queries are
 * performed with grouper (see https://uhawaii.atlassian.net/wiki/spaces/SITARd/pages/12944638/UH+Groupings+Design+2.x).
 * After the query occurs the result data should be set to a class which implements GroupingsResult, which
 * can be passed to update the timestamp.
 */
@Service("timestampService")
public class UpdateTimestampService {

    public static final Log logger = LogFactory.getLog(MembershipService.class);

    public GroupingTimestampResult update(GroupingResult groupingResult) {
        if (groupingResult.getResultCode().equals("SUCCESS")) {
            return updateLastModifiedTimestamp(groupingResult.getGroupPath());
        }
        return new GroupingTimestampResult();
    }

    private GroupingTimestampResult updateLastModifiedTimestamp(String groupPath) {
        UpdatedTimestampResult updatedTimestampResult = new UpdateTimestampCommand(groupPath).execute();
        GroupingTimestampResult groupingsTimestampResult = new GroupingTimestampResult(updatedTimestampResult);
        logger.debug("GroupingsTimestampResult; + " + JsonUtil.asJson(groupingsTimestampResult));
        return groupingsTimestampResult;
    }

}
