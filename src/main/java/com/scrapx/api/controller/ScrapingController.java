package com.scrapx.api.controller;

import com.scrapx.api.dto.*;
import com.scrapx.api.service.ScrapingService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;

/**
 * Controlador que maneja las solicitudes de scraping de las tres fuentes externas:
 * 1) OffShore Leaks
 * 2) World Bank
 * 3) OFAC (Office of Foreign Assets Control)
 *
 * Proporciona endpoints para realizar las búsquedas de una entidad en cada fuente
 * y devuelve los resultados correspondientes, junto con códigos de respuesta y
 * mensajes descriptivos.
 */
@RestController
@RequestMapping("/api")
public class ScrapingController {

    private final ScrapingService service;
    private final Bucket bucket;

    /**
     * Constructor que inyecta el servicio de scraping.
     *
     * @param service El servicio que gestiona las operaciones de scraping.
     */
    public ScrapingController(ScrapingService service) {
        this.service = service;
        // Configuración del Bucket: 20 solicitudes, recarga de 20 solicitudes cada minuto
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    /**
     * Endpoint que maneja la solicitud de scraping en las tres fuentes externas:
     * OffShore Leaks, World Bank y OFAC.
     *
     * Realiza el scraping de las tres fuentes, devolviendo los resultados estructurados
     * con un código de estado (200 o 404) y un mensaje explicativo.
     *
     * @param entity Nombre de la entidad a buscar en las fuentes.
     * @param score  El score mínimo para la búsqueda en OFAC.
     * @return ScrapingResponse Con los resultados de las tres fuentes.
     */
    @GetMapping("/scrap")
    public ScrapingResponse scrap(@RequestParam String entity, @RequestParam String score) {
        if (bucket.tryConsume(1)) {
            return service.scrap(entity, score);
        }
        else {
            // Si no hay tokens disponibles, responde con un error 429
            return new ScrapingResponse(
                    new SourceResponse<>(429, "Has superado el límite de solicitudes. Intenta más tarde.", 0, new ArrayList<>()),
                    new SourceResponse<>(429, "Has superado el límite de solicitudes. Intenta más tarde.", 0, new ArrayList<>()),
                    new SourceResponse<>(429, "Has superado el límite de solicitudes. Intenta más tarde.", 0, new ArrayList<>())
            );
        }
    }

    /**
     * Endpoint que maneja la solicitud de scraping solo en OffShore Leaks.
     *
     * Realiza el scraping de la fuente OffShore Leaks, devolviendo los resultados
     * con un código de estado (200 o 404) y un mensaje explicativo.
     *
     * @param entity Nombre de la entidad a buscar en OffShore Leaks.
     * @return SourceResponse Con los resultados de OffShore Leaks.
     */
    @GetMapping("/offshore")
    public SourceResponse<OffShoreResult> offshore(@RequestParam String entity) {
        if (bucket.tryConsume(1)) {
            return service.scrapOffShore(entity);
        }
        else {
            return new SourceResponse<>(429, "Has superado el límite de solicitudes. Intenta más tarde.", 0, new ArrayList<>());
        }
    }

    /**
     * Endpoint que maneja la solicitud de scraping solo en World Bank.
     *
     * Realiza el scraping de la fuente World Bank, devolviendo los resultados
     * con un código de estado (200 o 404) y un mensaje explicativo.
     *
     * @param entity Nombre de la entidad a buscar en World Bank.
     * @return SourceResponse Con los resultados de World Bank.
     */
    @GetMapping("/worldbank")
    public SourceResponse<WorldBankResult> worldbank(@RequestParam String entity) {
        if (bucket.tryConsume(1)) {
            return service.scrapWorldBank(entity);
        }
        else {
            return new SourceResponse<>(429, "Has superado el límite de solicitudes. Intenta más tarde.", 0, new ArrayList<>());
        }
    }

    /**
     * Endpoint que maneja la solicitud de scraping solo en OFAC.
     *
     * Realiza el scraping de la fuente OFAC, devolviendo los resultados
     * con un código de estado (200 o 404) y un mensaje explicativo.
     *
     * @param entity Nombre de la entidad a buscar en OFAC.
     * @param score  El score mínimo para la búsqueda en OFAC.
     * @return SourceResponse Con los resultados de OFAC.
     */
    @GetMapping("/ofac")
    public SourceResponse<OFACResults> ofac(@RequestParam String entity, @RequestParam String score) {
        if (bucket.tryConsume(1)) {
            return service.scrapOFAC(entity,score);
        }
        else {
            return new SourceResponse<>(429, "Has superado el límite de solicitudes. Intenta más tarde.", 0, new ArrayList<>());
        }
    }
}
