package com.starling.roundup.util;

import com.starling.roundup.BaseTestSetup;
import com.starling.roundup.exception.CurrencyNotFoundException;
import com.starling.roundup.exception.MultipleCurrenciesFoundException;
import com.starling.roundup.model.FeedItem;
import com.starling.roundup.model.FeedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundUpUtilsTest extends BaseTestSetup {

    @ParameterizedTest
    @MethodSource("provideRoundUpTestData")
    void testCalculateRoundUpAmount(List<FeedItem> feedItems, BigDecimal expectedRoundUp) {
        FeedResponse feedResponse = new FeedResponse(feedItems);
        BigDecimal result = RoundUpUtils.calculateRoundUpAmount(feedResponse);
        assertEquals(expectedRoundUp, result);
    }

    @Test
    void testExtractCurrencySuccess() {
        FeedResponse feedResponse = new FeedResponse(List.of(createTransaction(225)));
        String currency = RoundUpUtils.extractCurrency(feedResponse);
        assertEquals("GBP", currency);
    }

    @Test
    void testCurrencyNotFoundException() {
        FeedResponse feedResponse = new FeedResponse(List.of());
        assertThrows(CurrencyNotFoundException.class, () -> RoundUpUtils.extractCurrency(feedResponse));
    }

    @Test
    void testMultipleCurrenciesFoundException() {
        FeedResponse feedResponse = new FeedResponse(List.of(createTransactionWithUSCurrency(),createTransaction(234)));
        assertThrows(MultipleCurrenciesFoundException.class, () -> RoundUpUtils.extractCurrency(feedResponse));
    }

    @ParameterizedTest
    @MethodSource("provideEligibilityTestData")
    void testIsEligibleForRoundUp(FeedItem feedItem, boolean expectedEligibility) {
        var result = RoundUpUtils.isEligibleForRoundUp(feedItem);
        assertEquals(expectedEligibility, result);
    }

    @ParameterizedTest
    @MethodSource("provideRemainderTestData")
    void testCalculateRemainder(FeedItem feedItem, BigDecimal expectedRemainder) {
        BigDecimal result = RoundUpUtils.calculateRemainder(feedItem);
        assertEquals(expectedRemainder.setScale(2, RoundingMode.HALF_UP),
                result.setScale(2, RoundingMode.HALF_UP));

    }

    static Stream<Arguments> provideEligibilityTestData() {
        return Stream.of(
                Arguments.of(createTransaction2("OUT", "SETTLED", "OTHER"), true),
                Arguments.of(createTransaction2("OUT", "DECLINED", "OTHER"), false),
                Arguments.of(createTransaction2("IN", "SETTLED", "OTHER"), false),
                Arguments.of(createTransaction2("OUT", "SETTLED", "INTERNAL_TRANSFER"), false)
        );
    }

    static Stream<Arguments> provideRemainderTestData() {

        return Stream.of(
                Arguments.of(createTransaction(250), BigDecimal.valueOf(0.50)),  // 2.50 -> Needs 0.50 to round up
                Arguments.of(createTransaction(175), BigDecimal.valueOf(0.25)),  // 1.75 -> Needs 0.25 to round up
                Arguments.of(createTransaction(400), BigDecimal.ZERO),           // 4.00 -> Already rounded
                Arguments.of(createTransaction(95), BigDecimal.valueOf(0.05)),   // 0.95 -> Needs 0.05 to round up
                Arguments.of(createTransaction(-200), BigDecimal.ZERO)
        );
    }

    static Stream<Arguments> provideRoundUpTestData() {
        return Stream.of(
                Arguments.of(List.of(createTransaction(220),
                        createTransaction(375)), BigDecimal.valueOf(1.05)),

                Arguments.of(List.of(
                        createTransaction(220),
                        createTransaction(195)), BigDecimal.valueOf(0.85)),

                Arguments.of(List.of(
                        createTransaction(600),
                        createTransaction2("IN","SETTLED","OTHER"),
                        createTransaction(-800)), BigDecimal.ZERO)
        );
    }

}
