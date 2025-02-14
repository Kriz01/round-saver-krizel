package com.starling.roundup.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FeedResponse(@JsonProperty("feedItems") List<FeedItem> feedItems) {
}

