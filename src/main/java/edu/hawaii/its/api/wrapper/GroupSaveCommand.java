package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;

/**
 * Wrapper for GcGroupSave.
 */
public class GroupSaveCommand extends GrouperCommand<GroupSaveCommand> implements Command<GroupSaveResults> {

    private final GcGroupSave gcGroupSave;
    private final WsGroupToSave wsGroupToSave;

    public GroupSaveCommand() {
        this.gcGroupSave = new GcGroupSave();
        this.gcGroupSave.assignContentType("text/x-json"); // Remove after upgrading to Grouper 4
        this.wsGroupToSave = new WsGroupToSave();
    }

    @Override
    public GroupSaveResults execute() {
        gcGroupSave.addGroupToSave(wsGroupToSave);
        WsGroupSaveResults wsGroupSaveResults = gcGroupSave.execute();
        return new GroupSaveResults(wsGroupSaveResults);
    }

    @Override
    protected GroupSaveCommand self() {
        return this;
    }

    public GroupSaveCommand setGroupingPath(String groupingPath) {
        wsGroupToSave.setWsGroupLookup(groupLookup(groupingPath));
        return this;
    }

    public GroupSaveCommand setDescription(String description) {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setDescription(description);
        wsGroupToSave.setWsGroup(wsGroup);
        return this;
    }
}
