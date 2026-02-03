package com.scrapx.api.dto;

import java.util.List;

/**
 * Representa la respuesta completa de scraping que contiene los resultados
 * de las tres fuentes (OffShore, WorldBank, OFAC).
 *
 * Cada fuente se devuelve como un `SourceResponse` que incluye el código de estado,
 * mensaje, el número de resultados encontrados y la lista de resultados.
 */
public record ScrapingResponse (
        SourceResponse<OffShoreResult> offShore,
        SourceResponse<WorldBankResult> worldBank,
        SourceResponse<OFACResults> ofac
) { }

