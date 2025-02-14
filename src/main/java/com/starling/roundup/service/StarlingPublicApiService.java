package com.starling.roundup.service;

import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.FeedResponse;
import reactor.core.publisher.Mono;

public interface StarlingPublicApiService {

    Mono<FeedResponse> getAllFeedTransactions(String accountUid, String categoryUid, String changesSince, String authorizationHeader);

    Mono<Void> addMoneyToSavings(String accountId, String savingsGoalId, AddMoneyRequest addMoneyRequest, String authorizationHeader);
}
