# ScrapX
## Descripción

Este proyecto consiste en una **API RESTful** desarrollada para realizar **web scraping** en fuentes externas con el objetivo de identificar entidades de alto riesgo, como **sanciones internacionales**, **listas de vigilancia** y otras **bases de datos** relevantes. La API permite realizar búsquedas automatizadas de **entidades** en las siguientes fuentes:

1. **OffShore Leaks Database**: Proporciona información relacionada con entidades vinculadas a cuentas offshore.
2. **The World Bank**: Contiene información sobre firmas inhabilitadas en el contexto de adquisiciones y contratos del Banco Mundial.
3. **OFAC (Office of Foreign Assets Control)**: Contiene información sobre individuos y entidades sancionadas por el gobierno de los Estados Unidos.

La API toma como entrada el **nombre de una entidad** y realiza la búsqueda en las fuentes mencionadas, devolviendo los resultados de la búsqueda junto con información relevante como los **atributos** de cada fuente.

### Características:
- **Web Scraping Automatizado**: Se realiza scraping de las tres fuentes para obtener datos relevantes.
- **Rate-Limiting**: La API limita el número de solicitudes a **20 por minuto** para evitar el uso excesivo de recursos.

## Endpoints

### 1. **/api/scrap**

**Método**: `GET`

Este endpoint realiza scraping en las tres fuentes (OffShore Leaks, World Bank y OFAC) y devuelve los resultados correspondientes en una única respuesta.

#### Parámetros:
- `entity` (string): Nombre de la entidad a buscar en las tres fuentes.
- `score` (string): Parámetro utilizado solo para la búsqueda en **OFAC**.

#### Respuesta:
La respuesta es un objeto que contiene los resultados de las tres fuentes con el siguiente formato:

```json
{
  "offShore": {
    "code": 200,
    "message": "Los resultados se encontraron éxitosamente",
    "numHits": 10,
    "results": [/* resultados de OffShore */]
  },
  "worldBank": {
    "code": 404,
    "message": "No se pudo establecer la conexión",
    "numHits": 0,
    "results": []
  },
  "ofac": {
    "code": 200,
    "message": "Los resultados se encontraron éxitosamente",
    "numHits": 5,
    "results": [/* resultados de OFAC */]
  }
}
```

### 2. **/api/offshore**

**Método**: `GET`

Este endpoint realiza scraping solo en la fuente OffShore Leaks y devuelve los resultados correspondientes.
#### Parámetros:
- `entity` (string): Nombre de la entidad a buscar en **OffShoreLeaks**.

#### Respuesta:

```json
{
    "code": 200,
    "message": "Los resultados se encontraron éxitosamente",
    "numHits": 10,
    "results": [/* resultados de OffShore */]
}
```

### 3. **/api/worldbank**

**Método**: `GET`

Este endpoint realiza scraping solo en la fuente WorldBank y devuelve los resultados correspondientes.
#### Parámetros:
- `entity` (string): Nombre de la entidad a buscar en **WorldBank**.

#### Respuesta:

```json
{
    "code": 200,
    "message": "Los resultados se encontraron éxitosamente",
    "numHits": 10,
    "results": [/* resultados de WorldBank */]
}
```

### 4. **/api/ofac**

**Método**: `GET`

Este endpoint realiza scraping solo en la fuente OFAC y devuelve los resultados correspondientes.
#### Parámetros:
- `entity` (string): Nombre de la entidad a buscar en **OFAC**.
- `score` (string): Parámetro utilizado solo para la búsqueda en **OFAC**.

#### Respuesta:

```json
{
    "code": 200,
    "message": "Los resultados se encontraron éxitosamente",
    "numHits": 10,
    "results": [/* resultados de OFAC */]
}
```

## Rate-Limiting

Para proteger la API contra un uso excesivo, se ha implementado **rate-limiting**. Esto significa que un usuario solo podrá hacer **20 solicitudes por minuto**.

### ¿Cómo funciona el rate-limiting?

- La API usa **Bucket4j**, una librería de rate-limiting, para gestionar las solicitudes.

- Cada usuario tiene un **bucket** con capacidad de **20 tokens** que se recargan automáticamente cada **1 minuto**.

- Por cada solicitud, la API intenta consumir **1 token**. Si el usuario aún tiene tokens disponibles, la solicitud es procesada. Si no hay tokens disponibles, la solicitud será rechazada y se devolverá un código de error **429 - Too Many Requests**.

  Esto ayuda a limitar el abuso de la API y a asegurar un uso equilibrado de los recursos.

### Ejemplo de respuesta cuando se excede el límite:

Si un usuario excede el número máximo de solicitudes permitidas (20 por minuto), la API responderá con el siguiente código de error:

```json
{
  "code": 429,
  "message": "Has superado el límite de solicitudes. Intenta más tarde.",
  "numHits": 0,
  "results": []
}
```
En este caso, el cliente deberá esperar un minuto antes de poder hacer más solicitudes.

## Despliegue (Docker + Azure Container Apps)

Esta API fue empaquetada en un contenedor Docker y desplegada en **Azure Container Apps**. A continuacion se incluyen el contenido del `Dockerfile` y los pasos base para construir la imagen y publicarla en ACA.

### Dockerfile

```dockerfile
# Usar Eclipse Temurin como base para Java 17
FROM eclipse-temurin:17-jdk

# Instalar Maven y Chromium en el contenedor
RUN apt-get update && apt-get install -y maven chromium

# Establecer el directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el codigo del proyecto al contenedor
COPY . .

# Ejecutar Maven para instalar las dependencias y compilar el proyecto
RUN mvn clean install

# Exponer el puerto que se utilizara
EXPOSE 8080

# Ejecutar la aplicacion
CMD ["mvn", "spring-boot:run"]
```

### Build y ejecucion local

```bash
docker build -t scrapx-api:latest .
docker run --rm -p 8080:8080 scrapx-api:latest
```

### Uso directo de la imagen en Docker Hub

Si se desea utilizar la imagen ya publicada en Docker Hub, se puede descargar y ejecutar directamente:

```bash
docker pull silvvquiroz/scrapx:v1
docker run --rm -p 8080:8080 silvvquiroz/scrapx:v1
```

Repositorio en Docker Hub: `https://hub.docker.com/repository/docker/silvvquiroz/scrapx/general`

Arquitectura soportada: `linux/amd64`.

### Despliegue en Azure Container Apps (referencia)

1. Publicar la imagen en Docker Hub:

```bash
docker tag scrapx-api:latest <USERNAME>/scrapx:latest
docker push <USERNAME>/scrapx:latest
```

2. Crear el Container App y apuntar a la imagen:

```bash
az containerapp create \
  -g <RESOURCE_GROUP> \
  -n <APP_NAME> \
  --image <USERNAME>/scrapx:latest \
  --target-port 8080 \
  --ingress external
```

Notas:
- Reemplazar los valores entre `<>` por los del entorno.
- Ver repositorio en Docker Hub: `https://hub.docker.com/repository/docker/silvvquiroz/scrapx/general`
- Considerar que la version de produccion es la `v1` (no `v2`).

## Uso de la API

Esta sección resume el uso de los endpoints principales en producción y en local. Los parámetros ya descritos en la sección de Endpoints se incluyen aquí para mayor claridad.

### Endpoints en producción (Azure Container Apps)

- Offshore (scraping de OffShore Leaks): obtener registros que coincidan con el nombre de la entidad.
  `https://scrapx-app.thankfulocean-d0a214e1.eastus.azurecontainerapps.io/api/offshore?entity={entityName}`

  Parámetros:
  - `entity` (string): nombre de la entidad a buscar.

  Ejemplo:
  `https://scrapx-app.thankfulocean-d0a214e1.eastus.azurecontainerapps.io/api/offshore?entity=AERO`

  Nota:
  La fuente OffShore puede presentar caídas temporales, lo que puede provocar respuestas con error 404 cuando la conexión no se establece correctamente.

- OFAC (scraping de la lista OFAC): obtener registros que coincidan con el nombre de la entidad, filtrando por porcentaje mínimo de coincidencia.
  `https://scrapx-app.thankfulocean-d0a214e1.eastus.azurecontainerapps.io/api/ofac?entity={entityName}&score={score}`

  Parámetros:
  - `entity` (string): nombre de la entidad a buscar.
  - `score` (string): porcentaje mínimo de coincidencia del nombre buscado.

  Ejemplo:
  `https://scrapx-app.thankfulocean-d0a214e1.eastus.azurecontainerapps.io/api/ofac?entity=AERO&score=100`

- WorldBank (deshabilitada en producción): obtener registros de World Bank (debarred firms) que coincidan con el nombre de la entidad.
  `https://scrapx-app.thankfulocean-d0a214e1.eastus.azurecontainerapps.io/api/worldbank?entity={entityName}`

  Parámetros:
  - `entity` (string): nombre de la entidad a buscar.

  Ejemplo:
  `https://scrapx-app.thankfulocean-d0a214e1.eastus.azurecontainerapps.io/api/worldbank?entity=AERO`

### Endpoint WorldBank en local

Debido a limitaciones del despliegue, no es posible ejecutar Chromium dentro del contenedor Docker; por ello, el endpoint de WorldBank se recomienda probarlo de forma local.

`http://localhost:8080/api/worldbank?entity=AERO`

Parámetros:
- `entity` (string): nombre de la entidad a buscar.

Ejemplo:
`http://localhost:8080/api/worldbank?entity=AERO`

### Parámetros principales

- `entity` (string): nombre de la entidad a buscar (requerido en Offshore, WorldBank y OFAC).
- `score` (string): porcentaje mínimo de coincidencia del nombre buscado (requerido solo para OFAC).

### Colección de Postman

Para ejecutar las pruebas desde Postman, usar la colección incluida en el repositorio:
Para ejecutar las pruebas desde Postman, usar la colección incluida en el repositorio:

- Archivo local: [ScrapX API.postman_collection.json](ScrapX API.postman_collection.json)
- Link de la colección: https://go.postman.co/collection/49810004-78bee5f1-dbec-4e5d-b30e-6f8dff3394f3?source=collection_link

Las requests de la colección corresponden a los endpoints de producción y a la variante local de WorldBank, e incluyen ejemplos con los parámetros `entity` y `score`.
La informacion mas detallada sobre el uso de las APIs se encuentra en la colección de Postman.
