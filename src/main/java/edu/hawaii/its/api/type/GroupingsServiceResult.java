package edu.hawaii.its.api.type;

public class GroupingsServiceResult {
  private String action = "null";
  private String resultCode = "null";
  private String username = "null";

  public GroupingsServiceResult() {
    // Empty.
  }

  public GroupingsServiceResult(String resultCode, String action) {
    this.resultCode = resultCode;
    this.action = action;
  }

  public String getResultCode() {
    return resultCode;
  }

  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return this.username;
  }

  @Override
  public String toString() {
    return "GroupingsServiceResult [action=" + action
        + ", resultCode=" + resultCode
        + "]";
  }
}

