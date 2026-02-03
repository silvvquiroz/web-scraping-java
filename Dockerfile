# Usar Eclipse Temurin como base para Java 17
FROM eclipse-temurin:17-jdk

# Instalar Maven y Chromium en el contenedor
RUN apt-get update && apt-get install -y maven chromium

# Establecer el directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el código del proyecto al contenedor
COPY . .

# Ejecutar Maven para instalar las dependencias y compilar el proyecto
RUN mvn clean install

# Exponer el puerto que se utilizará
EXPOSE 8080

# Ejecutar la aplicación
CMD ["mvn", "spring-boot:run"]
