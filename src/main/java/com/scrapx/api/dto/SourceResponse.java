package com.scrapx.api.dto;

import java.util.List;

public record SourceResponse<T> (
        int numHits,
        List<T> results
) { }
