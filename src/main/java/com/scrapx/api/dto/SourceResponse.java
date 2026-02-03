package com.scrapx.api.dto;

import java.util.List;

/**
 * Representa la respuesta genérica para una fuente de datos (OffShore, WorldBank, OFAC).
 * Incluye el código de estado HTTP, un mensaje descriptivo, el número de resultados obtenidos
 * y la lista de resultados específicos para esa fuente.
 *
 * @param <T> Tipo de los resultados que esta respuesta contiene (OffShoreResult`, `WorldBankResult`, `OFACResults`).
 */
public record SourceResponse<T> (
        int code,
        String message,
        int numHits,
        List<T> results
) { }
