package com.scrapx.api.dto;

public record ScrapingResponse (
        SourceResponse<OffShoreResult> offShore,
        SourceResponse<WorldBankResult> worldBank,
        SourceResponse<OFACResults> ofac
) {}
