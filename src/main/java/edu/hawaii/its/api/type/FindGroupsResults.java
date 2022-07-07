package edu.hawaii.its.api.type;

import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;

public class FindGroupsResults extends Results {

    private WsFindGroupsResults wsFindGroupsResults;
    private final boolean isEmpty;
    private static final String NO_DESCRIPTION_TEXT = "No description given for this Grouping.";

    // Constructor (temporary; don't depend on it).
    public FindGroupsResults(WsFindGroupsResults wsFindGroupsResults) {
        this.wsFindGroupsResults = wsFindGroupsResults;
        if (this.wsFindGroupsResults == null) {
            this.wsFindGroupsResults = new WsFindGroupsResults();
        }
        isEmpty = isEmpty(this.wsFindGroupsResults.getGroupResults());
    }

    public String getDescription() {
        if (isEmpty) {
            return NO_DESCRIPTION_TEXT;
        }

        String description = wsFindGroupsResults.getGroupResults()[0].getDescription();
        if (description == null || description.length() == 0) {
            return NO_DESCRIPTION_TEXT;
        }

        return description;
    }

    @Override
    public String getResultCode() {
        return wsFindGroupsResults.getResultMetadata().getResultCode();
    }

}