package com.starling.roundup.controller;

import com.starling.roundup.BaseTestSetup;
import com.starling.roundup.model.AddMoneyRequest;
import com.starling.roundup.model.Amount;
import com.starling.roundup.service.RoundUpFeatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoundUpFeatureControllerTest extends BaseTestSetup {

    @Mock
    private RoundUpFeatureService roundUpFeatureService;

    @InjectMocks
    private RoundUpFeatureController roundUpFeatureController;

    private Amount amount;

    @BeforeEach
    void setup() {
        amount = getAmount();
    }

    @Test
    void testRoundUpTransactions() {

        when(roundUpFeatureService.processWeeklyRoundUp(ACCOUNT_UUID, CATEGORY_UUID, CHANGES_SINCE, AUTHORIZATION_HEADER))
                .thenReturn(Mono.just(amount));
        var actualResult = roundUpFeatureController.roundUpTransactions(AUTHORIZATION_HEADER, CHANGES_SINCE, ACCOUNT_UUID, CATEGORY_UUID);

        StepVerifier.create(actualResult)
                .expectNext(amount)
                .expectComplete()
                .verify();
    }

    @Test
    void testTransferRoundUpToSavings() {
        var addMoneyRequest = new AddMoneyRequest(amount);
        when(roundUpFeatureService.transferToSavings(ACCOUNT_UUID, SAVINGS_GOAL_UUID, addMoneyRequest, AUTHORIZATION_HEADER))
                .thenReturn(Mono.empty());

        var actualResult = roundUpFeatureController.transferRoundUpToSavings(AUTHORIZATION_HEADER, ACCOUNT_UUID, SAVINGS_GOAL_UUID, addMoneyRequest);

        StepVerifier.create(actualResult)
                .expectComplete()
                .verify();
    }


}
