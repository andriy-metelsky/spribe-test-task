package com.spribe.clients;

import com.spribe.specs.BaseSpec;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class BaseClient {
    protected static final Logger logger = LogManager.getLogger(BaseClient.class);
    protected final RequestSpecification requestSpec;

    public BaseClient() {
        this.requestSpec = BaseSpec.getBaseRequestSpec();
    }

    protected Response post(String endpoint, Object body) {
        return RestAssured
                .given()
                .spec(requestSpec)
                .body(body)
                .post(endpoint);
    }

    protected Response get(String endpoint) {
        return RestAssured
                .given()
                .spec(requestSpec)
                .get(endpoint);
    }

    protected Response get(String endpoint, Map<String, ?> queryParams) {
        return RestAssured
                .given()
                .spec(requestSpec)
                .queryParams(queryParams)
                .get(endpoint);
    }

    protected Response get(String endpoint, Map<String, ?> pathParams, Map<String, ?> queryParams) {
        return RestAssured
                .given()
                .spec(requestSpec)
                .pathParams(pathParams)
                .queryParams(queryParams)
                .when()
                .get(endpoint);
    }

    protected Response patch(String endpoint, Object body, Map<String, String> pathParams) {
        return RestAssured
                .given()
                .spec(requestSpec)
                .pathParams(pathParams)
                .body(body)
                .patch(endpoint);
    }

    protected Response delete(String endpoint, Object body, String paramName, String paramValue) {
        return RestAssured
                .given()
                .spec(requestSpec)
                .pathParam(paramName, paramValue)
                .body(body)
                .delete(endpoint);
    }
}
