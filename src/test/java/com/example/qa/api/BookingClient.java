package com.example.qa.api;

import com.example.qa.config.TestConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class BookingClient {
    private RequestSpecification request() {
        int timeout = TestConfig.positiveInt("api.timeoutMs");
        return given()
                .baseUri(TestConfig.get("api.baseUrl"))
                .accept("application/json")
                .config(RestAssuredConfig.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", timeout)
                                .setParam("http.socket.timeout", timeout))
                        .logConfig(LogConfig.logConfig().blacklistHeader("Authorization")))
                .log().ifValidationFails();
    }

    public Response create(Booking booking) {
        return request().contentType(ContentType.JSON).body(booking).post("/booking");
    }

    public Response get(int id) {
        return request().pathParam("id", id).get("/booking/{id}");
    }

    public Response delete(int id) {
        return request().auth().preemptive()
                .basic(TestConfig.get("api.username"), TestConfig.get("api.password"))
                .pathParam("id", id).delete("/booking/{id}");
    }
}
