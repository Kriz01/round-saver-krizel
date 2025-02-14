package com.starling.roundup.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starling.roundup.exception.ApiException;
import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.FeedResponse;
import com.starling.roundup.service.StarlingPublicApiService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class StarlingPublicApiServiceImpl implements StarlingPublicApiService {

    private static final String FEED_TRANSACTIONS_URI = "/api/v2/feed/account/{accountUid}/category/{categoryUid}";
    private static final String ADD_MONEY_TO_SAVINGS_URI = "/api/v2/account/{accountId}/savings-goals/{savingsGoalId}/add-money/{transferId}";
    private static final String CHANGES_SINCE = "changesSince";
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public StarlingPublicApiServiceImpl(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<FeedResponse> getAllFeedTransactions(String accountUid, String categoryUid, String changesSince, String authorizationHeader) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(FEED_TRANSACTIONS_URI)
                        .queryParam(CHANGES_SINCE, changesSince)
                        .build(accountUid, categoryUid))
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(FeedResponse.class);
    }

    @Override
    public Mono<Void> addMoneyToSavings(String accountId, String savingsGoalId, AddMoneyRequest amount, String authorizationHeader) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ADD_MONEY_TO_SAVINGS_URI)
                        .build(accountId, savingsGoalId, UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .bodyValue(amount)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Void.class);

    }

    private <T> Mono<T> handleError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    Map<String, Object> errorMap;
                    try {
                        errorMap = objectMapper.readValue(errorBody, new TypeReference<>() {
                        });
                    } catch (JsonProcessingException e) {
                        errorMap = Map.of("error", "Unknown error format", "details", errorBody);
                    }
                    return Mono.error(new ApiException(errorMap.toString(), clientResponse.statusCode()));
                });
    }

}
