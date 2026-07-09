package edu.hawaii.its.api.type;

/**
 * Result for a grouping retirement request.
 */
public class RetireGroupingResult extends GroupingsServiceResult {

    private String resultMessage;

    public RetireGroupingResult() {
        super("FAILURE", "retire");
        this.resultMessage = "";
    }

    public RetireGroupingResult(String resultCode, String resultMessage) {
        super(resultCode, "retire");
        this.resultMessage = resultMessage;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}
