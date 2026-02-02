package com.scrapx.api.dto;

public record OffShoreResult (
    String entity,
    String jurisdiction,
    String linkedTo,
    String dataFrom
) {}