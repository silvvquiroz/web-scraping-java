package com.scrapx.api.dto;

/**
 * Representa un resultado de búsqueda en la fuente OFAC (Office of Foreign Assets Control).
 * Contiene información sobre entidades o individuos que han sido sancionados.
 *
 * @param name    Nombre de la entidad o individuo sancionado
 * @param address Dirección de la entidad sancionada
 * @param type    Tipo de entidad/individuo
 * @param program Programa o legislación bajo la cual la entidad fue sancionada
 * @param list    Tipo de lista en la que la entidad está incluida
 * @param score   Porcentaje de coincidencia en el nombre
 */
public record OFACResults (
        String name,
        String address,
        String type,
        String program,
        String list,
        String score
) { }
