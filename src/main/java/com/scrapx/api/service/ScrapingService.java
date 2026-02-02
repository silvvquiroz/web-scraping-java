package com.scrapx.api.service;

import com.scrapx.api.dto.*;
import com.scrapx.api.dto.*;
import com.scrapx.api.scraping.WebScraper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrapingService {
    private final WebScraper scraper;

    public ScrapingService(WebScraper scraper) {
        this.scraper = scraper;
    }

    public ScrapingResponse scrap(String entity, String score) {
        List<OffShoreResult> offShore = scraper.searchOffShore(entity);
        List<WorldBankResult> worldBank = scraper.searchWorldBank(entity);
        List<OFACResults> ofac = scraper.searchOFAC(entity, score);

        return new ScrapingResponse(
                new SourceResponse<>(offShore.size(), offShore),
                new SourceResponse<>(worldBank.size(), worldBank),
                new SourceResponse<>(ofac.size(), ofac)
        );
    }

    public SourceResponse<OffShoreResult> scrapOffShore(String entity) {
        List<OffShoreResult> offShore = scraper.searchOffShore(entity);

        return new SourceResponse<>(offShore.size(), offShore);
    }

    public SourceResponse<WorldBankResult> scrapWorldBank(String entity) {
        List<WorldBankResult> worldBank = scraper.searchWorldBank(entity);

        return new SourceResponse<>(worldBank.size(), worldBank);
    }

    public SourceResponse<OFACResults> scrapOFAC(String entity, String score) {
        List<OFACResults> ofac = scraper.searchOFAC(entity, score);

        return new SourceResponse<>(ofac.size(), ofac);
    }
}
