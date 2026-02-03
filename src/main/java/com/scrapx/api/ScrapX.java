package com.scrapx.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal que arranca la aplicación Spring Boot.
 *
 * Esta clase se encarga de inicializar el contexto de Spring Boot y ejecutar
 * la aplicación, lo que permite que todos los componentes de la aplicación
 * estén disponibles para ser utilizados.
 */
@SpringBootApplication
public class ScrapX {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * @param args Argumentos de línea de comando
     */
    public static void main (String[] args) {
        // Ejecutar la aplicación Spring Boot
        SpringApplication.run(ScrapX.class, args);
    }
}
