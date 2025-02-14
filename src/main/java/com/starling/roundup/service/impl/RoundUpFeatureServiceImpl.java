package com.starling.roundup.service.impl;

import com.starling.roundup.exception.FeedNotFoundException;
import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.Amount;
import com.starling.roundup.model.FeedResponse;
import com.starling.roundup.service.RoundUpFeatureService;
import com.starling.roundup.service.StarlingPublicApiService;
import com.starling.roundup.util.RoundUpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class RoundUpFeatureServiceImpl implements RoundUpFeatureService {

    private final StarlingPublicApiService starlingPublicApiService;

    @Autowired
    public RoundUpFeatureServiceImpl(StarlingPublicApiService starlingPublicApiService) {
        this.starlingPublicApiService = starlingPublicApiService;
    }

    @Override
    public Mono<Void> transferToSavings(String accountId, String savingsGoalId, AddMoneyRequest addMoneyRequest, String authorizationHeader) {
        return starlingPublicApiService.addMoneyToSavings(accountId, savingsGoalId, addMoneyRequest, authorizationHeader);
    }

    /**
     * Processes the round-up for an account by fetching all feed transactions,
     * calculating the total round-up amount, and returning an Amount object that encapsulates
     * both the currency and the computed round-up amount (expressed in minor units).
     *
     * <p>This method delegates the fetching of transactions to {@code starlingPublicApiService.getAllFeedTransactions}
     * and then processes the resulting {@link FeedResponse} as follows:
     * <ul>
     *   <li>Extracts the currency from the first feed item
     *   <li>Calculates the round-up amount
     *   <li>Converts the round-up amount to minor units (by multiplying by 100) and creates a new {@code Amount} instance.</li>
     * </ul>
     *
     * @param accountId           the unique identifier of the account
     * @param categoryId          the unique identifier of the category associated with the account
     * @param changesSince        an ISO 8601 timestamp indicating the starting point for considering transactions
     * @param authorizationHeader the authorization header (e.g., a Bearer token) required for API authentication
     * @return a {@code Mono<Amount>} that emits an {@code Amount} containing the currency and the total round-up amount (in minor units)
     * which can then be transferred to the savings goal account
     */

    @Override
    public Mono<Amount> processWeeklyRoundUp(String accountId, String categoryId, String changesSince, String authorizationHeader) {
        return starlingPublicApiService
                .getAllFeedTransactions(accountId, categoryId, changesSince, authorizationHeader)
                .flatMap(feedResponse -> {
                    if (feedResponse.feedItems().isEmpty()) {
                        return Mono.error(new FeedNotFoundException("No feed transactions available, round-up not possible"));
                    }
                    String currency = RoundUpUtils.extractCurrency(feedResponse);
                    BigDecimal roundUpAmount = RoundUpUtils.calculateRoundUpAmount(feedResponse);
                    return Mono.just(new Amount(currency, roundUpAmount.multiply(BigDecimal.valueOf(100)).intValue()));
                });
    }
}
