package com.starling.roundup.util;

import com.starling.roundup.exception.CurrencyNotFoundException;
import com.starling.roundup.exception.MultipleCurrenciesFoundException;
import com.starling.roundup.model.FeedItem;
import com.starling.roundup.model.FeedResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundUpUtils {

    private static final String OUT = "OUT";
    private static final String INTERNAL_TRANSFER = "INTERNAL_TRANSFER";
    private static final String SETTLED = "SETTLED";

    private RoundUpUtils() {
    }

    /**
     * Extracts the currency code from a {@link FeedResponse} by retrieving the currency from the first feed item.
     *
     * <p>If no feed items are available in the response, this method throws a {@link CurrencyNotFoundException}.
     *
     * @param feedResponse the response containing a list of feed items
     * @return the currency code (as a {@code String}) from the first feed item in the response
     * @throws CurrencyNotFoundException if no feed items are found in the response
     * @throws MultipleCurrenciesFoundException if feed items have multiple currencies
     */
    public static String extractCurrency(FeedResponse feedResponse) {
        return feedResponse.feedItems().stream()
                .map(feedItem -> feedItem.amount().currency())
                .filter(currency -> !currency.isEmpty())
                .distinct()
                .limit(2)
                .reduce((first, second) -> { throw new MultipleCurrenciesFoundException("Multiple currencies found");
                })
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found"));
    }

    /**
     * Calculates the total round-up amount from a {@link FeedResponse}.
     *
     * <p>This method filters for feed items that are eligible for round-up (i.e., those with a direction of "OUT"
     * and a source other than "INTERNAL_TRANSFER"), calculates the rounding remainder for each item, and sums
     * these remainders to produce a total round-up amount.
     *
     * @param feedResponse the response containing the feed items to process
     * @return a {@code BigDecimal} representing the total round-up amount in major currency units
     */
    public static BigDecimal calculateRoundUpAmount(FeedResponse feedResponse) {
        return feedResponse.feedItems().stream()
                .filter(RoundUpUtils::isEligibleForRoundUp)
                .map(RoundUpUtils::calculateRemainder)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Determines if a given {@link FeedItem} is eligible for a round-up calculation.
     *
     * <p>A feed item is eligible if its direction is "OUT" and its source is not "INTERNAL_TRANSFER" and the status is "SETTLED"
     *
     * @return {@code true} if the feed item is eligible for round-up; {@code false} otherwise
     */
    static boolean isEligibleForRoundUp(FeedItem feedItem) {
        return OUT.equals(feedItem.direction()) && !INTERNAL_TRANSFER.equals(feedItem.source()) && SETTLED.equals(feedItem.status());
    }

    /**
     * Calculates the rounding remainder for a single {@link FeedItem}.
     *
     * <p>This method converts the feed item’s amount from minor units to major units, extracts the fractional part,
     * and then determines how much is required to round up to the next whole number.
     * It Ignores any negative minorUnits
     *
     * @param feedItem the feed item from which to calculate the remainder
     * @return a {@code BigDecimal} representing the amount to add to round up to the nearest whole unit;
     * returns {@code BigDecimal.ZERO} if no rounding is needed
     */
    static BigDecimal calculateRemainder(FeedItem feedItem) {
        if (feedItem.amount().minorUnits() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amountInCurrencyUnit = BigDecimal.valueOf(feedItem.amount().minorUnits()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal remainder = amountInCurrencyUnit.remainder(BigDecimal.ONE);
        return remainder.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.ONE.subtract(remainder);
    }
}
