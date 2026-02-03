package com.scrapx.api.dto;

/**
 * Representa un resultado de búsqueda en la fuente OffShore Leaks (ICIJ).
 * Contiene la información relevante sobre una entidad.
 *
 * @param entity      Nombre de la entidad o empresa
 * @param jurisdiction Jurisdicción donde se encuentra registrada la entidad
 * @param linkedTo    Entidad o país al que la entidad está vinculada
 * @param dataFrom    Fuente de los datos
 */
public record OffShoreResult (
        String entity,
        String jurisdiction,
        String linkedTo,
        String dataFrom
) { }
