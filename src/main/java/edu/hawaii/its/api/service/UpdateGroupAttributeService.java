package edu.hawaii.its.api.service;

import edu.hawaii.its.api.wrapper.UpdateGroupAttributeCommand;
import edu.hawaii.its.api.wrapper.UpdateGroupAttributeResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("updateGroupAttributeService")
public class UpdateGroupAttributeService {

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.operation_assign_attribute}")
    private String OPERATION_ASSIGN_ATTRIBUTE;

    @Value("${groupings.api.operation_remove_attribute}")
    private String OPERATION_REMOVE_ATTRIBUTE;

    @Autowired
    private ExecutorService executor;

    public UpdateGroupAttributeResults updateAttribute(String groupingPath, String operation, String attributeName) {
        return executor.execute(
                new UpdateGroupAttributeCommand(ASSIGN_TYPE_GROUP, operation, attributeName, groupingPath));

    }

}
