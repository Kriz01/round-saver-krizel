package com.starling.roundup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starling.roundup.BaseTestSetup;
import com.starling.roundup.exception.ApiException;
import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.Amount;
import com.starling.roundup.model.FeedResponse;
import com.starling.roundup.service.impl.StarlingPublicApiServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StarlingPublicApiServiceImplTest extends BaseTestSetup {

    private MockWebServer mockWebServer;
    private StarlingPublicApiServiceImpl starlingPublicApiService;
    private ObjectMapper objectMapper;

    protected static final String ADD_MONEY_TO_SAVINGS_URI = "/api/v2/account/test-account/savings-goals/test-savings-goal/add-money/";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        objectMapper = new ObjectMapper();
        starlingPublicApiService = new StarlingPublicApiServiceImpl(webClient, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetAllFeedTransactions() throws Exception {
        FeedResponse mockResponse = new FeedResponse(List.of(createTransaction(375)));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(mockResponse)));

        Mono<FeedResponse> result = starlingPublicApiService.getAllFeedTransactions(
                ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER);

        StepVerifier.create(result)
                .expectNext(mockResponse)
                .verifyComplete();
        verifyRequest("GET", "/api/v2/feed/account/test-account/category/test-category?changesSince=2024-01-01T00:00:00.000Z");
    }

    @Test
    void testAddMoneyToSavings() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        AddMoneyRequest addMoneyRequest = new AddMoneyRequest(new Amount("GBP", 3634)); // Populate with test data

        Mono<Void> result = starlingPublicApiService.addMoneyToSavings(
                ACCOUNT_UUID, SAVINGS_GOAL_UUID, addMoneyRequest, AUTHORIZATION_HEADER);

        StepVerifier.create(result)
                .verifyComplete();

        verifyRequest("PUT", ADD_MONEY_TO_SAVINGS_URI);
    }

    @Test
    void testHandleError() throws Exception {
        String errorResponse = "{\"error\":\"Some error\",\"details\":\"Error details\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400) // Simulating a 400 Bad Request error
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(errorResponse));

        AddMoneyRequest addMoneyRequest = new AddMoneyRequest(new Amount("GBP", 3634));

        Mono<Void> result = starlingPublicApiService.addMoneyToSavings(
                ACCOUNT_UUID, SAVINGS_GOAL_UUID, addMoneyRequest, AUTHORIZATION_HEADER);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(ApiException.class, throwable);

                    ApiException apiException = (ApiException) throwable;
                    assertEquals(HttpStatus.BAD_REQUEST, apiException.getStatus());
                    Assertions.assertTrue(apiException.getMessage().contains("Some error"));
                })
                .verify();
        verifyRequest("PUT", ADD_MONEY_TO_SAVINGS_URI);
    }


    private void verifyRequest(String expectedMethod, String expectedPath) throws InterruptedException {
        RecordedRequest recordedRequest = takeRequest();
        assertEquals(expectedMethod, recordedRequest.getMethod());
        Assertions.assertNotNull(recordedRequest.getPath());
        Assertions.assertTrue(recordedRequest.getPath().contains(expectedPath));
        assertEquals(AUTHORIZATION_HEADER, recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    private RecordedRequest takeRequest() throws InterruptedException {
        return mockWebServer.takeRequest();
    }

}