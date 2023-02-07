package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsAddResults;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResults;
import edu.hawaii.its.api.groupings.GroupingsReplaceGroupMembersResult;
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("updateTimestampService")
public class UpdateTimestampService {

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;
    @Autowired
    GrouperApiService grouperApiService;

    public static final Log logger = LogFactory.getLog(MembershipService.class);
    private static final String SUCCESS = "SUCCESS";

    public UpdateTimestampResult addResults(GroupingsAddResults addResults) {
        if (addResults.getResultCode().equals(SUCCESS)) {
            return updateLastModified(addResults.getGroupPath());
        }
        return null;
    }

    public UpdateTimestampResult removeResults(GroupingsRemoveResults removeResults) {
        if (removeResults.getResultCode().equals(SUCCESS)) {
            return updateLastModified(removeResults.getGroupPath());
        }
        return null;
    }

    public UpdateTimestampResult addResult(GroupingsAddResult addResult) {
        if (addResult.getResultCode().equals(SUCCESS)) {
            return updateLastModified(addResult.getGroupPath());
        }
        return null;
    }

    public UpdateTimestampResult removeResult(GroupingsRemoveResult removeResult) {
        if (removeResult.getResultCode().equals(SUCCESS)) {
            return updateLastModified(removeResult.getGroupPath());
        }
        return null;
    }

    public UpdateTimestampResult replaceGroupMembersResult(
            GroupingsReplaceGroupMembersResult groupingsReplaceGroupMembersResult) {
        if (groupingsReplaceGroupMembersResult.getResultCode().equals(SUCCESS)) {
            return updateLastModified(groupingsReplaceGroupMembersResult.getGroupPath());
        }
        return null;
    }

    private UpdateTimestampResult updateLastModified(String groupPath) {
        logger.info("updateLastModified; group: " + groupPath + ";");
        String dateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        return updateLastModifiedTimestamp(dateTime, groupPath);
    }

    /**
     * Update the last modified attribute of a group to dateTime.
     */
    private UpdateTimestampResult updateLastModifiedTimestamp(String dateTime, String groupPath) {
        WsAttributeAssignValue wsAttributeAssignValue = grouperApiService.assignAttributeValue(dateTime);
        return new UpdateTimestampResult(grouperApiService.assignAttributesResults(
                ASSIGN_TYPE_GROUP,
                OPERATION_ASSIGN_ATTRIBUTE,
                groupPath,
                YYYYMMDDTHHMM,
                OPERATION_REPLACE_VALUES,
                wsAttributeAssignValue));
    }

}
