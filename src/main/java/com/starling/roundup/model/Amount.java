package com.starling.roundup.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Amount(
        @JsonProperty("currency") String currency,
        @JsonProperty("minorUnits") Integer minorUnits
) {
}
