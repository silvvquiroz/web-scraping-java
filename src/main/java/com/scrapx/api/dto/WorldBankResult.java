package com.scrapx.api.dto;

/**
 * Representa un resultado de búsqueda en la fuente World Bank.
 * Contiene información sobre las firmas inhabilitadas.
 *
 * @param firmName Nombre de la firma inhabilitada
 * @param address Dirección de la firma
 * @param country País donde la firma está registrada
 * @param fromDate Fecha de inicio de la inhabilitación
 * @param toDate   Fecha de fin de la inhabilitación
 * @param grounds  Motivos de la inhabilitación de la firma
 */
public record WorldBankResult(
        String firmName,
        String address,
        String country,
        String fromDate,
        String toDate,
        String grounds
) { }
