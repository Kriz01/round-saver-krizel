package com.starling.roundup.service;

import com.starling.roundup.BaseTestSetup;
import com.starling.roundup.exception.CurrencyNotFoundException;
import com.starling.roundup.exception.FeedNotFoundException;
import com.starling.roundup.model.Amount;
import com.starling.roundup.model.FeedResponse;
import com.starling.roundup.service.impl.RoundUpFeatureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoundUpFeatureServiceImplTest extends BaseTestSetup {

    @Mock
    private StarlingPublicApiService starlingPublicApiService;

    @InjectMocks
    RoundUpFeatureServiceImpl roundUpFeatureServiceImpl;

    @Test
    void testValidTotalRoundUpCalculation() {
        BigDecimal expectedRoundUp = new BigDecimal("0.25").add(new BigDecimal("0.80")); // £1.05 → 105 minor units

        when(starlingPublicApiService.getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .thenReturn(Mono.just(new FeedResponse(List.of(createTransaction(375), createTransaction(220)))));
        StepVerifier.create(roundUpFeatureServiceImpl.processWeeklyRoundUp(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .expectNext(new Amount("GBP", expectedRoundUp.multiply(BigDecimal.valueOf(100)).intValue()))
                .verifyComplete();
        verify(starlingPublicApiService, times(1)).getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER);
    }

    @Test
    void testProcessWeeklyRoundUpNoTransactions() {
        when(starlingPublicApiService.getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .thenReturn(Mono.just(new FeedResponse(List.of())));

        StepVerifier.create(roundUpFeatureServiceImpl.processWeeklyRoundUp(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .expectError(FeedNotFoundException.class)
                .verify();

        verify(starlingPublicApiService, times(1)).getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER);
    }

    @Test
    void testProcessWeeklyRoundUpNoCurrency() {
        when(starlingPublicApiService.getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .thenReturn(Mono.just(new FeedResponse(List.of(createTransactionWithoutCurrency()))));

        StepVerifier.create(roundUpFeatureServiceImpl.processWeeklyRoundUp(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .expectError(CurrencyNotFoundException.class)
                .verify();

        verify(starlingPublicApiService, times(1)).getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER);
    }

    /**
     * **Parameterized Test for Edge Cases**
     * - Covers:
     * Whole number transactions (No round-up required)
     * Negative transactions (Should not contribute)
     */
    @ParameterizedTest
    @MethodSource("provideEdgeCaseTransactions")
    void testEdgeCaseTransactions(FeedResponse edgeCaseResponse) {
        when(starlingPublicApiService.getAllFeedTransactions(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .thenReturn(Mono.just(edgeCaseResponse));

        StepVerifier.create(roundUpFeatureServiceImpl.processWeeklyRoundUp(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .expectNext(new Amount("GBP", 0)) // Edge cases should not contribute to round-up
                .verifyComplete();
    }

    static Stream<FeedResponse> provideEdgeCaseTransactions() {
        return Stream.of(
                new FeedResponse(List.of(createWholeNumberTransaction())),
                new FeedResponse(List.of(createNegativeTransaction()))
        );
    }

}
