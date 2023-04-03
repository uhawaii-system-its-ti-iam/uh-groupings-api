package edu.hawaii.its.api.wrapper;

import edu.hawaii.its.api.exception.InvalidGroupPathException;

import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;

/**
 * Wrapper for GcGroupSave.
 */
public class GroupSaveCommand extends GrouperCommand implements Command<GroupSaveResults> {

    private final GcGroupSave gcGroupSave;
    private final WsGroupToSave wsGroupToSave;

    public GroupSaveCommand() {
        gcGroupSave = new GcGroupSave();
        wsGroupToSave = new WsGroupToSave();
    }

    @Override
    public GroupSaveResults execute() {
        gcGroupSave.addGroupToSave(wsGroupToSave);
        try {
            WsGroupSaveResults wsGroupSaveResults = gcGroupSave.execute();
            return new GroupSaveResults(wsGroupSaveResults);
        } catch (RuntimeException e) {
            throw new InvalidGroupPathException("");
        }
    }

    public GroupSaveCommand setGroupingPath(String groupingPath) {
        String grouperUuid = new FindGroupsCommand().addPath(groupingPath).execute().getGroup().getGrouperUuid();
        WsGroupLookup wsGroupLookup = new WsGroupLookup(groupingPath, grouperUuid);
        wsGroupToSave.setWsGroupLookup(wsGroupLookup);
        return this;
    }

    public GroupSaveCommand setDescription(String description) {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setDescription(description);
        wsGroupToSave.setWsGroup(wsGroup);
        return this;
    }
}
