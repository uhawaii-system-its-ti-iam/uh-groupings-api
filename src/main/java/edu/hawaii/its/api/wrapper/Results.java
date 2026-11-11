package edu.hawaii.its.api.wrapper;

import java.util.List;

import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

public abstract class Results implements Resultable {
    protected boolean isEmpty(Object[] o) {
        return o == null || o.length == 0;
    }

    protected boolean getBoolean(String str) {
        String result = (str != null) ? str : "F";
        return result.equals("T");
    }

    /**
     * The result metadata of a result merged from several batch results: the first unsuccessful batch's, so the
     * merged result never reports success if any batch did not succeed, otherwise the first batch's.
     */
    protected static WsResultMeta mergedResultMetadata(List<WsResultMeta> batchResultMetadata) {
        for (WsResultMeta resultMetadata : batchResultMetadata) {
            String resultCode = resultMetadata != null ? resultMetadata.getResultCode() : null;
            if (resultCode == null || !resultCode.startsWith("SUCCESS")) {
                return resultMetadata;
            }
        }
        return batchResultMetadata.get(0);
    }
}
