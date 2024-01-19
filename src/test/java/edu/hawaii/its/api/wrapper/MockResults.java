package edu.hawaii.its.api.wrapper;

public class MockResults extends Results {
    private String resultCode;

    public MockResults(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }
}
