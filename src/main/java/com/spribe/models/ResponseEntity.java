package com.spribe.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseEntity {

    @JsonProperty("body")
    private Object body;

    @JsonProperty("statusCode")
    private String statusCode;

    @JsonProperty("statusCodeValue")
    private Integer statusCodeValue;

    public ResponseEntity() {
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getStatusCodeValue() {
        return statusCodeValue;
    }

    public void setStatusCodeValue(Integer statusCodeValue) {
        this.statusCodeValue = statusCodeValue;
    }
}
