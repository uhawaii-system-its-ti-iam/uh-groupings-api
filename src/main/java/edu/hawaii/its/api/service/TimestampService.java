package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.util.Dates;
import edu.hawaii.its.api.wrapper.UpdateTimestampCommand;
import edu.hawaii.its.api.wrapper.UpdateTimestampResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("timestampService")
public class TimestampService {
    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_replace_values}")
    private String OPERATION_REPLACE_VALUES;
    private static String FORMAT_STR = "yyyyMMdd'T'HHmm";

    @Autowired
    private ExecutorService executor;

    public static final Log logger = LogFactory.getLog(TimestampService.class);

    public UpdateTimestampResults updateTimestamp(String groupPath) {
        UpdateTimestampResults updateTimestampResults = updateTimestamp(groupPath,LocalDateTime.now());
        return updateTimestampResults;
    }
    public UpdateTimestampResults updateTimestamp(String groupPath, LocalDateTime dateTime) {
        UpdateTimestampResults updateTimestampResults = executor.execute(
                new UpdateTimestampCommand(ASSIGN_TYPE_GROUP, OPERATION_ASSIGN_ATTRIBUTE, groupPath, YYYYMMDDTHHMM,
                        OPERATION_REPLACE_VALUES, dateTime));
        logger.info("TimestampService; updateTimestamp; groupPath: " + groupPath + "; dateTime: " + Dates.formatDate(dateTime, FORMAT_STR) + ";");
        return updateTimestampResults;
    }
}
