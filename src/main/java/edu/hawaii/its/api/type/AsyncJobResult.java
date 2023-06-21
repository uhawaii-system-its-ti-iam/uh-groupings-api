package edu.hawaii.its.api.type;

public class AsyncJobResult {
    private Integer id;
    private String status;
    private Object result;

    public AsyncJobResult() {
    }

    public AsyncJobResult(Integer id, String status) {
        this.id = id;
        this.status = status;
        this.result = "";
    }

    public AsyncJobResult(Integer id, String status, Object result) {
        this.id = id;
        this.status = status;
        this.result = result;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
