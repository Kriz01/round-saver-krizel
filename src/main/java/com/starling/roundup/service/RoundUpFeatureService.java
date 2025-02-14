package com.starling.roundup.service;

import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.Amount;
import reactor.core.publisher.Mono;

public interface RoundUpFeatureService {
    Mono<Void> transferToSavings(String accountId, String savingsGoalId, AddMoneyRequest addMoneyRequest, String authorizationHeader);

    Mono<Amount> processWeeklyRoundUp(String accountId, String categoryId, String changesSince, String authorizationHeader);
}
