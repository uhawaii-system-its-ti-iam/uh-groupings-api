package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.groupings.GroupingsResult;
import edu.hawaii.its.api.groupings.GroupingsTimestampResult;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.UpdateTimestampCommand;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResult;

import org.springframework.stereotype.Service;

@Service("timestampService")
public class UpdateTimestampService {

    public static final Log logger = LogFactory.getLog(MembershipService.class);

    public GroupingsTimestampResult update(GroupingsResult groupingsResult) {
        if (groupingsResult.getResultCode().equals("SUCCESS")) {
            return updateLastModifiedTimestamp(groupingsResult.getGroupPath());
        }
        return new GroupingsTimestampResult();

    }

    private GroupingsTimestampResult updateLastModifiedTimestamp(String groupPath) {
        UpdatedTimestampResult updatedTimestampResult = new UpdateTimestampCommand(groupPath).execute();
        GroupingsTimestampResult groupingsTimestampResult = new GroupingsTimestampResult(updatedTimestampResult);
        logger.info("GroupingsTimestampResult; + " + JsonUtil.asJson(groupingsTimestampResult));
        return groupingsTimestampResult;
    }

}
