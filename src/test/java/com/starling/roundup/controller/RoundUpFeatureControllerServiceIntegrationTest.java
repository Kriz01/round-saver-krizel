package com.starling.roundup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.starling.roundup.BaseTestSetup;
import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.Amount;
import com.starling.roundup.model.FeedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class RoundUpFeatureControllerServiceIntegrationTest extends BaseTestSetup {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private final String ROUND_UP_API = "/api/starling/account/{accountUid}/category/{categoryUid}/round-up?changesSince={changesSince}";
    private final String TRANSFER_ROUND_UP_API = "/api/starling/account/{accountUid}/savings-goal/{savingsGoalUid}/transfer-round-up";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("starling.api.baseUrl", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
    }


    @Test
    public void testRoundUpTransactions() throws Exception {
        FeedResponse feedResponse = new FeedResponse(List.of(createTransaction(375)));
        mockGetFeedTransactions(feedResponse);

        webTestClient.get()
                .uri(ROUND_UP_API, ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Amount.class)
                .value(amount -> {
                    assert amount.currency().equals("GBP");
                    assert amount.minorUnits() == 25;
                });
    }

    @Test
    public void testRoundUpTransactions_MissingAuthorizationHeader() throws Exception {
        FeedResponse feedResponse = new FeedResponse(List.of(createTransaction(375)));
        mockGetFeedTransactions(feedResponse);

        webTestClient.get()
                .uri(ROUND_UP_API, ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void testTransferRoundUpToSavings() throws Exception {
        AddMoneyRequest addMoneyRequest = new AddMoneyRequest(new Amount("GBP", 1000));
        mockAddMoneyToSavings(addMoneyRequest);

        webTestClient.put()
                .uri(TRANSFER_ROUND_UP_API, ACCOUNT_UUID, SAVINGS_GOAL_UUID)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
                .bodyValue(addMoneyRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testTransferRoundUpToSavings_MissingRequestBody() {
        webTestClient.put()
                .uri(TRANSFER_ROUND_UP_API, ACCOUNT_UUID, SAVINGS_GOAL_UUID)
                .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue("")
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * Helper method to mock the GET feed transactions API
     */
    private void mockGetFeedTransactions(FeedResponse feedResponse) throws Exception {
        wireMock.stubFor(
                WireMock.get(urlPathMatching("/api/v2/feed/account/.*/category/.*"))
                        .withQueryParam("changesSince", equalTo(CHANGES_SINCE))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody(objectMapper.writeValueAsString(feedResponse)))
        );
    }

    /**
     * Helper method to mock the PUT add money to savings API
     **/
    private void mockAddMoneyToSavings(AddMoneyRequest addMoneyRequest) throws Exception {
        wireMock.stubFor(
                WireMock.put(urlPathMatching("/api/v2/account/.*/savings-goals/.*/add-money/.*"))
                        .withRequestBody(equalToJson(objectMapper.writeValueAsString(addMoneyRequest)))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        );
    }
}