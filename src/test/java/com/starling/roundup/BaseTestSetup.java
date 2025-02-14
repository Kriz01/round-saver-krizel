package com.starling.roundup;

import com.starling.roundup.model.Amount;
import com.starling.roundup.model.FeedItem;
import com.starling.roundup.model.SourceAmount;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class BaseTestSetup {

    protected final String ACCOUNT_UUID = "test-account";
    protected final String CATEGORY_UUID = "test-category";
    protected final String SAVINGS_GOAL_UUID = "test-savings-goal";
    protected final String AUTHORIZATION_HEADER = "Bearer test-token";
    protected final String CHANGES_SINCE = "2024-01-01T00:00:00.000Z";

    protected static FeedItem createTransaction(Integer minorUnits) {
        return new FeedItem(
                UUID.fromString("48f4ecec-3c22-491d-b7b4-79414527b6e6"),
                UUID.fromString("48f00630-62f0-4e42-8e6a-0a7523a64c3f"),
                new Amount("GBP", minorUnits),
                new SourceAmount("GBP", minorUnits),
                "OUT",
                "2025-02-09T21:08:37.067",
                "2025-02-09T21:08:37.945",
                "FASTER_PAYMENTS_OUT",
                "EXTERNAL",
                "SETTLED",
                "PAYEE",
                "48f4b2d6-2264-4ad1-94f0-de098344ffe0",
                UUID.fromString("48f00630-62f0-4e42-8e6a-0a7523a64c3f"),
                "48f441c1-caf5-4a64-b4ee-4dd87f6fc43f",
                UUID.fromString("48f00630-62f0-4e42-8e6a-0a7523a64c3f"),
                "204514",
                "00000825",
                "External Payment",
                "GB",
                "PAYMENTS",
                "category",
                false,
                null
        );
    }

    protected static FeedItem createTransaction2(String direction, String status, String source) {
        return new FeedItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Amount("GBP", 220),
                new SourceAmount("GBP", 220),
                direction,
                "2024-02-01T12:00:00.000",
                "2024-02-01T12:01:00.000",
                "FASTER_PAYMENTS_OUT",
                source,
                status,
                "Test Merchant 2",
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                "123456",
                "00000826",
                "External Payment",
                "GB",
                "PAYMENTS",
                "category",
                false,
                null
        );
    }

    protected FeedItem createTransactionWithoutCurrency() {
        return new FeedItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Amount("", 220),
                new SourceAmount("", 220),
                "OUT",
                "2024-02-01T12:00:00.000",
                "2024-02-01T12:01:00.000",
                "FASTER_PAYMENTS_OUT",
                "SETTLED",
                UUID.randomUUID().toString(),
                "Test Merchant 2",
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                "123456",
                "00000826",
                "External Payment",
                "GB",
                "PAYMENTS",
                "category",
                false,
                null
        );
    }

    protected static FeedItem createWholeNumberTransaction() {
        return new FeedItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Amount("GBP", 200),
                new SourceAmount("GBP", 200),
                "OUT",
                "2024-02-01T12:00:00.000",
                "2024-02-01T12:01:00.000",
                "FASTER_PAYMENTS_OUT",
                "SETTLED",
                UUID.randomUUID().toString(),
                "Test Merchant 2",
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                "123456",
                "00000826",
                "External Payment",
                "GB",
                "PAYMENTS",
                "category",
                false,
                null
        );
    }

    protected static FeedItem createNegativeTransaction() {
        return new FeedItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Amount("GBP", -280),
                new SourceAmount("GBP", -280),
                "OUT",
                "2024-02-01T12:00:00.000",
                "2024-02-01T12:01:00.000",
                "FASTER_PAYMENTS_OUT",
                "SETTLED",
                UUID.randomUUID().toString(),
                "Test Merchant 2",
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                "123456",
                "00000826",
                "External Payment",
                "GB",
                "PAYMENTS",
                "category",
                false,
                null
        );
    }

    protected static FeedItem createTransactionWithUSCurrency() {
        return new FeedItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new Amount("USD", 280),
                new SourceAmount("USD", 280),
                "IN",
                "2024-02-01T12:00:00.000",
                "2024-02-01T12:01:00.000",
                "FASTER_PAYMENTS_OUT",
                "INTERNAL TRANSFER",
                UUID.randomUUID().toString(),
                "Test Merchant 2",
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                "123456",
                "00000826",
                "External Payment",
                "GB",
                "PAYMENTS",
                "category",
                false,
                null
        );
    }

    protected Amount getAmount() {
        return new Amount("GBP", 100);
    }

}
