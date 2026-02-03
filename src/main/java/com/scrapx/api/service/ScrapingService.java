package com.scrapx.api.service;

import com.scrapx.api.dto.*;
import com.scrapx.api.scraping.WebScraper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para realizar scraping de múltiples fuentes externas y devolver resultados
 * de manera estructurada a través de DTOs.
 *
 * Esta clase contiene los métodos que interactúan con el scraper para obtener
 * los resultados de tres fuentes:
 * 1) OffShore Leaks
 * 2) World Bank
 * 3) OFAC (Office of Foreign Assets Control)
 */
@Service
public class ScrapingService {
    private final WebScraper scraper;

    /**
     * Constructor que inyecta el scraper para realizar las búsquedas.
     *
     * @param scraper Instancia de WebScraper para interactuar con las fuentes externas.
     */
    public ScrapingService(WebScraper scraper) {
        this.scraper = scraper;
    }

    /**
     * Realiza scraping de las tres fuentes (OffShore, WorldBank, OFAC) y devuelve
     * una respuesta agregada con los resultados obtenidos de cada fuente.
     *
     * @param entity El nombre de la entidad a buscar en las fuentes.
     * @param score  Un parámetro adicional para la búsqueda en OFAC, como el score mínimo.
     * @return ScrapingResponse Con los resultados de las tres fuentes (OffShore, WorldBank, OFAC).
     */
    public ScrapingResponse scrap(String entity, String score) {
        // Obtener resultados de OffShore, WorldBank y OFAC
        List<OffShoreResult> offShore = scraper.searchOffShore(entity);
        List<WorldBankResult> worldBank = scraper.searchWorldBank(entity);
        List<OFACResults> ofac = scraper.searchOFAC(entity, score);

        // Asignar código de respuesta y mensaje para OffShore
        int offShoreCode = offShore.isEmpty() ? 404 : 200;
        String offShoreMessage = offShore.isEmpty() ? "No se pudo establecer la conexión" : "Los resultados se encontraron éxitosamente";

        // Asignar código de respuesta y mensaje para WorldBank
        int worldBankCode = worldBank.isEmpty() ? 404 : 200;
        String worldBankMessage = worldBank.isEmpty() ? "No se pudo establecer la conexión" : "Los resultados se encontraron éxitosamente";

        // Asignar código de respuesta y mensaje para OFAC
        int ofacCode = ofac.isEmpty() ? 404 : 200;
        String ofacMessage = ofac.isEmpty() ? "No se pudo establecer la conexión" : "Los resultados se encontraron éxitosamente";

        // Retornar los resultados estructurados con código, mensaje y resultados de cada fuente
        return new ScrapingResponse(
                new SourceResponse<>(offShoreCode, offShoreMessage, offShore.size(), offShore),
                new SourceResponse<>(worldBankCode, worldBankMessage, worldBank.size(), worldBank),
                new SourceResponse<>(ofacCode, ofacMessage, ofac.size(), ofac)
        );
    }

    /**
     * Realiza scraping solo de la fuente OffShore y devuelve los resultados obtenidos.
     *
     * @param entity El nombre de la entidad a buscar en OffShore.
     * @return SourceResponse Con los resultados de OffShore.
     */
    public SourceResponse<OffShoreResult> scrapOffShore(String entity) {
        // Obtener resultados de OffShore
        List<OffShoreResult> offShore = scraper.searchOffShore(entity);

        // Asignar código de respuesta y mensaje para OffShore
        int offShoreCode = offShore.isEmpty() ? 404 : 200;
        String offShoreMessage = offShore.isEmpty() ? "No se pudo establecer la conexión" : "Los resultados se encontraron éxitosamente";

        // Retornar los resultados de OffShore con el código de respuesta y mensaje
        return new SourceResponse<>(offShoreCode, offShoreMessage, offShore.size(), offShore);
    }

    /**
     * Realiza scraping solo de la fuente WorldBank y devuelve los resultados obtenidos.
     *
     * @param entity El nombre de la entidad a buscar en WorldBank.
     * @return SourceResponse Con los resultados de WorldBank.
     */
    public SourceResponse<WorldBankResult> scrapWorldBank(String entity) {
        // Obtener resultados de WorldBank
        List<WorldBankResult> worldBank = scraper.searchWorldBank(entity);

        // Asignar código de respuesta y mensaje para WorldBank
        int worldBankCode = worldBank.isEmpty() ? 404 : 200;
        String worldBankMessage = worldBank.isEmpty() ? "No se pudo establecer la conexión" : "Los resultados se encontraron éxitosamente";

        // Retornar los resultados de WorldBank con el código de respuesta y mensaje
        return new SourceResponse<>(worldBankCode, worldBankMessage, worldBank.size(), worldBank);
    }

    /**
     * Realiza scraping solo de la fuente OFAC y devuelve los resultados obtenidos.
     *
     * @param entity El nombre de la entidad a buscar en OFAC.
     * @param score  El score mínimo para la búsqueda en OFAC.
     * @return SourceResponse Con los resultados de OFAC.
     */
    public SourceResponse<OFACResults> scrapOFAC(String entity, String score) {
        // Obtener resultados de OFAC
        List<OFACResults> ofac = scraper.searchOFAC(entity, score);

        // Asignar código de respuesta y mensaje para OFAC
        int ofacCode = ofac.isEmpty() ? 404 : 200;
        String ofacMessage = ofac.isEmpty() ? "No se pudo establecer la conexión" : "Los resultados se encontraron éxitosamente";

        // Retornar los resultados de OFAC con el código de respuesta y mensaje
        return new SourceResponse<>(ofacCode, ofacMessage, ofac.size(), ofac);
    }
}
