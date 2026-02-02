package com.scrapx.api.controller;

import com.scrapx.api.dto.*;
import com.scrapx.api.service.ScrapingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ScrapingController {
    private final ScrapingService service;

    public ScrapingController(ScrapingService service) {
        this.service = service;
    }

//    @GetMapping("/scrap")
//    public ScrapingResponse scrap(@RequestParam String entity, @RequestParam String score) {
//        return service.scrap(entity, score);
//    }

    @GetMapping("/offshore")
    public SourceResponse<OffShoreResult> offshore(@RequestParam String entity) {
        return service.scrapOffShore(entity);
    }

//    @GetMapping("/worldbank")
//    public SourceResponse<WorldBankResult> worldbank(@RequestParam String entity) {
//        return service.scrapWorldBank(entity);
//    }

    @GetMapping("/ofac")
    public SourceResponse<OFACResults> ofac(@RequestParam String entity, @RequestParam String score) {
        return service.scrapOFAC(entity, score);
    }

}
