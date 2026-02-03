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

### 3. **/api/worldbank (actualmente deshabilitada)**

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
