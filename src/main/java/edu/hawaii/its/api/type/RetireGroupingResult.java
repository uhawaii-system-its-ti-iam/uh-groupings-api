package edu.hawaii.its.api.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Result for a grouping retirement request.
 */
public class RetireGroupingResult extends GroupingsServiceResult {

    private String resultMessage;

    private List<String> ownerRecipients;

    public RetireGroupingResult() {
        super("FAILURE", "retire");
        this.resultMessage = "";
        this.ownerRecipients = new ArrayList<>();
    }

    public RetireGroupingResult(String resultCode, String resultMessage, List<String> ownerRecipients) {
        super(resultCode, "retire");
        this.resultMessage = resultMessage;
        this.ownerRecipients = ownerRecipients;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public List<String> getOwnerRecipients() {
        return ownerRecipients;
    }

    public void setOwnerRecipients(List<String> ownerRecipients) {
        this.ownerRecipients = ownerRecipients;
    }

}
