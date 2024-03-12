package edu.hawaii.its.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;

public class FindAttributesResults extends Results {
    private final WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults;

    public FindAttributesResults(WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults) {
        if (wsFindAttributeDefNamesResults == null) {
            this.wsFindAttributeDefNamesResults = new WsFindAttributeDefNamesResults();
        } else {
            this.wsFindAttributeDefNamesResults = wsFindAttributeDefNamesResults;
        }
    }

    @Override public String getResultCode() {
        if (getResults().isEmpty()) {
            return "FAILURE";
        }
        return this.wsFindAttributeDefNamesResults.getResultMetadata().getResultCode();
    }

    public AttributesResult getResult() {
        WsAttributeDefName[] wsAttributeDefNames = this.wsFindAttributeDefNamesResults.getAttributeDefNameResults();
        if (isEmpty(wsAttributeDefNames)) {
            return new AttributesResult();
        }
        return new AttributesResult(wsAttributeDefNames[0]);
    }

    public List<AttributesResult> getResults() {
        WsAttributeDefName[] wsAttributeDefNames = this.wsFindAttributeDefNamesResults.getAttributeDefNameResults();
        List<AttributesResult> attributesResults = new ArrayList<>();
        if (isEmpty(wsAttributeDefNames)) {
            return attributesResults;
        }
        for (WsAttributeDefName wsAttributeDefName : wsAttributeDefNames) {
            attributesResults.add(new AttributesResult(wsAttributeDefName));
        }
        return attributesResults;
    }
}