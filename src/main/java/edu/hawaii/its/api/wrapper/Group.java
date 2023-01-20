package edu.hawaii.its.api.wrapper;

import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

public class Group extends Results {
    private final WsGroup wsGroup;

    public Group(WsGroup wsGroup) {
        if (wsGroup == null) {
            this.wsGroup = new WsGroup();
        } else {
            this.wsGroup = wsGroup;
        }
    }

    public Group() {
        this.wsGroup = new WsGroup();
    }

    @Override public String getResultCode() {
        if (getGroupPath().equals("")) {
            return "FAILURE";
        }
        return "SUCCESS";
    }

    public String getGroupPath() {
        String groupPath = wsGroup.getName();
        return (groupPath != null) ? groupPath : "";
    }

    public String getDescription() {
        String description = wsGroup.getDescription();
        return (description != null) ? description : "";
    }

    public String getExtension() {
        String extension = wsGroup.getExtension();
        return (extension != null) ? extension : "";
    }

    public boolean isValidPath() {
        return !getGroupPath().equals("");
    }
}
