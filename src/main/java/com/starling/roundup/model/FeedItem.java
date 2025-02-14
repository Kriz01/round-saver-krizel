package com.starling.roundup.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeedItem(
        @JsonProperty("feedItemUid") UUID feedItemUid,
        @JsonProperty("categoryUid") UUID categoryUid,
        @JsonProperty("amount") Amount amount,
        @JsonProperty("sourceAmount") SourceAmount sourceAmount,
        @JsonProperty("direction") String direction,
        @JsonProperty("updatedAt") String updatedAt,
        @JsonProperty("transactionTime") String transactionTime,
        @JsonProperty("settlementTime") String settlementTime,
        @JsonProperty("source") String source,
        @JsonProperty("status") String status,
        @JsonProperty("transactingApplicationUserUid") String transactingApplicationUserUid,
        @JsonProperty("counterPartyType") String counterPartyType,
        @JsonProperty("counterPartyUid") UUID counterPartyUid,
        @JsonProperty("counterPartyName") String counterPartyName,
        @JsonProperty("counterPartySubEntityUid") UUID counterPartySubEntityUid,
        @JsonProperty("counterPartySubEntityName") String counterPartySubEntityName,
        @JsonProperty("counterPartySubEntityIdentifier") String counterPartySubEntityIdentifier,
        @JsonProperty("counterPartySubEntitySubIdentifier") String counterPartySubEntitySubIdentifier,
        @JsonProperty("reference") String reference,
        @JsonProperty("country") String country,
        @JsonProperty("spendingCategory") String spendingCategory,
        @JsonProperty("hasAttachment") Boolean hasAttachment,
        @JsonProperty("hasReceipt") Boolean hasReceipt
) {
}
