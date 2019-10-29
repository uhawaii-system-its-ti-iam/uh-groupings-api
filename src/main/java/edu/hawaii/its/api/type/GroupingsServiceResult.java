package edu.hawaii.its.api.type;

public class GroupingsServiceResult {
  private String action;
  private String resultCode;
  private Person person;

  public GroupingsServiceResult() {
    this.action = "null";
    this.resultCode = "null";
    this.person = null;
  }

  public GroupingsServiceResult(String resultCode, String action, Person person) {
    this.resultCode = resultCode;
    this.action = action;
    this.person = person;
  }

  public GroupingsServiceResult(String resultCode, String action) {
    this.resultCode = resultCode;
    this.action = action;
    this.person = null;
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

  public void setPerson(Person person) {
    this.person = person;
  }

  public Person getPerson() {
    return this.person;
  }

  @Override
  public String toString() {
    return "GroupingsServiceResult [action=" + action
        + ", resultCode=" + resultCode
        + "]";
  }
}

