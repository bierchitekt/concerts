package com.bierchitekt.concerts.venues;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventRequestBody(
        @JsonProperty("limit_count") int limitCount,
        @JsonProperty("offset_count") int offsetCount
) {}