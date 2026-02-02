package com.scrapx.api.dto;

public record WorldBankResult(
    String firmName,
    String address,
    String country,
    String fromDate,
    String toDate,
    String grounds
) { }
