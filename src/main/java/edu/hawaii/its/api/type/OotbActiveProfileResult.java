package edu.hawaii.its.api.type;

public class OotbActiveProfileResult {
    private String resultCode;
    private OotbActiveProfile result;

    public OotbActiveProfileResult(OotbActiveProfile ootbActiveProfile) {
        setResultCode("SUCCESS");
        setResult(ootbActiveProfile);
    }

    public OotbActiveProfileResult() {
        setResultCode("FAILURE");
        setResult(null);
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public OotbActiveProfile getResult() {
        return result;
    }

    public void setResult(OotbActiveProfile result) {
        this.result = result;
    }
}
