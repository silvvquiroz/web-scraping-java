package com.scrapx.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase principal que arranca la aplicación Spring Boot.
 *
 * Esta clase se encarga de inicializar el contexto de Spring Boot y ejecutar
 * la aplicación, lo que permite que todos los componentes de la aplicación
 * estén disponibles para ser utilizados.
 */
@SpringBootApplication
public class ScrapX implements WebMvcConfigurer {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * @param args Argumentos de línea de comando
     */
    public static void main (String[] args) {
        // Ejecutar la aplicación Spring Boot
        SpringApplication.run(ScrapX.class, args);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Permitir solicitudes desde localhost y Vercel
        registry.addMapping("/**") // Configura todas las rutas para permitir CORS
                .allowedOrigins("http://localhost:5173", "https://supplier-hub-front.vercel.app") // Los orígenes permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Métodos permitidos
                .allowedHeaders("*") // Permitir todos los encabezados
                .allowCredentials(true); // Permitir credenciales si es necesario
    }
}
