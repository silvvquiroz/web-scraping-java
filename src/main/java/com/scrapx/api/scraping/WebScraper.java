package com.scrapx.api.scraping;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.scrapx.api.dto.OFACResults;
import com.scrapx.api.dto.OffShoreResult;
import com.scrapx.api.dto.WorldBankResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Componente central de web scraping con funcionalidad implementada para
 * 3 fuentes externas:
 * 
 * 1) OffShore Leaks -> scraping por HTML estático usando Jsoup
 * 2) World Bank -> scraping dinámico usando Selenium
 * 3) OFAC -> scraping por simulación de formulario ASP.NET
 * 
 * Sidenote: Este componente está pensado para integrarse a endpoints API,
 * por lo que devuelve DTOs listos.
 */
@Component
public class WebScraper {

    /** URL base para la página OffShore */
    private final String baseURLOffShore = "https://offshoreleaks.icij.org/";
    
    /** URL base para la página WorldBank */
    private final String baseURLWorldBank = "https://projects.worldbank.org/en/projects-operations/procurement/debarred-firms";
    
    /** URL base para la página OFAC */
    private final String baseOFACURL = "https://sanctionssearch.ofac.treas.gov/Default.aspx";
    
    /** User-Agent para peticiones HTTP (usado en la página OFAC) */
    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";

    /**
     * Scrapea resultados de OffShore Leaks para una entidad dada.
     * 
     * Sidenote: La página renderiza resultados en una tabla con paginación
     * indicada en el parámetro "from". Cada bloque suele retornar 100 filas.
     * @param entity Nombre de la entidad a buscar
     * @return Lista de resultados con atributos: entityName, jurisdiction, linkedTo, dataFrom
     */
    public List<OffShoreResult> searchOffShore(final String entity) {
        System.out.println("======= Scraping: OffShore Leaks =======");

        List<OffShoreResult> results = new ArrayList<>();

        // 0. Setear los parámetros de búsqueda
        int from = 0;
        int resultsPerPage = 100;
        String trailParams = "&c=&j=&d=&cat=Entity";

        // Normalizar el parámetro de entrada
        String searchEntity = entity.trim().replace(" ", "+");

        try {
            /*
            Sidenote: La página de Off Shore muestra los resultados en una
            tabla que va cargando bloques de 100 filas. Por ello,
            se modifica la URL para obtener el HTML de cada bloque de 100 en iteraciones
             */
            while (true) {
                // 1. Construir la URL con parámetros de búsqueda
                String searchURL = baseURLOffShore + "search?q=" + searchEntity + trailParams + "&from=" + from;

                System.out.println("URL: " + searchURL);

                // 2. Conectar a la URL y obtener el HTML de la página
                Document doc = Jsoup.connect(searchURL).get();

                // 3. Seleccionar las filas de la tabla de resultados
                /*
                Sidenote: El DOM de la página tiene solo 1 tabla;
                por lo tanto, las filas se pueden seleccionar directamente
                */
                Elements tableBody = doc.getElementsByTag("tbody");
                if (tableBody.isEmpty()) {
                    // No hay más resultados
                    break;
                }

                Elements rows = tableBody.get(0).getElementsByTag("tr");

                System.out.println("Results count: " + rows.size());

                // 4. Iterar sobre las filas de la tabla y extraer los datos de la entidad
                for (Element row: rows) {
                    /*
                    Sidenote: Para seleccionar correctamente cada dato,
                    se inspeccionó el HTML de la página y se obtuvo el tag y las clases
                    que contienen a cada dato recuperado
                     */

                    // Extraer el nombre (tag <a> y clase "font-weight-bold text-dark")
                    String entityName = row.select("td a.font-weight-bold").text();

                    // Extraer la jurisdicción (tag <td> y clase "jurisdiction")
                    String jurisdiction = row.select("td.jurisdiction").text();

                    // Extraer el vínculo (tag <td> y clase "country")
                    String linkedTo = row.select("td.country").text();

                    // Extraer la fuente de data (tag <td> y clase "source", tag <a> y atributo "title")
                    String dataFrom = row.select("td.source a").attr("title");

                    results.add(new OffShoreResult(entityName, jurisdiction, linkedTo, dataFrom));
                }

                // 5. Siguiente bloque
                from += resultsPerPage;

            }

        }
        catch (IOException e) {
            System.err.println("Error durante el scraping de OffShore: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();

        }
        return results;
    }

    /**
     * Scrapea resultados de WorldBank para una entidad dada.
     * 
     * Sidenote: Esta página depende de JS (tabla tipo Kendo/Grid). Por ello
     * se usa Selenium para (1) cargar la página, (2) escribir en el filtro,
     * y (3) esperar a que cambie el número de filas.
     * 
     * @param entity Nombre de la entidad a buscar
     * @return Lista de resultados con atributos: firmName, address, country, fromDate, toDate, grounds
     */
    public List<WorldBankResult> searchWorldBank(final String entity) {
        System.out.println("======= Scraping: WorldBanks =======");

        List<WorldBankResult> results = new ArrayList<>();

        // 0. Configurar el web driver (en este caso, ChromeDriver) y los parámetros de búsqueda
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        // Flags recomendadas para contenedores
        options.addArguments("--no-sandbox"); 
        options.addArguments("--disable-dev-shm-usage");
        
        // Ruta de chromium-browser en el contenedor desplegado
        options.setBinary("/usr/bin/chromium-browser");

        // Normalizar el parámetro de entrada
        String searchEntity = entity.trim();

        // 1. Iniciar el WebDriver con ChromeDriver
        WebDriver driver = new ChromeDriver(options);

        try {
            // 2. Conectar a la URL de WorldBank
            driver.get(baseURLWorldBank);

            // 3. Localizar el textbox de búsqueda
            /*
            Sidenote: El nombre de la entidad se ingresa en un elemento de forma:
            tag: <input>
            id: "category"
             */
            WebElement textBox = driver.findElement(By.id("category"));

            // Esperar a que la tabla esté renderizada y lista para actualizarse
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("k-grid-content")));  // Esperamos que la tabla esté presente

            // Obtener el conteo inicial de filas (antes del filtrado)
            List<WebElement> initialRows = driver.findElements(By.cssSelector(".k-grid-content tr"));
            int initialRowCount = initialRows.size();

            // 3. Ingresar el texto a buscar (nombre de la entidad)
            textBox.sendKeys(searchEntity);

            // Esperar a que el número de filas cambie progresivamente (más específico)
            WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait2.until(ExpectedConditions.not(
                    ExpectedConditions.numberOfElementsToBe(By.cssSelector(".k-grid-content tr"), initialRowCount)
            ));

            // 4. Obtener el HTML actualizado y seleccionar las filas de la tabla
            WebElement tableParent = driver.findElement(By.id("k-debarred-firms")).findElement(By.className("k-grid-content"));
            List<WebElement> rows = tableParent.findElements(By.tagName("tr"));

            // Testing: Número de filas encontradas
            System.out.println("Results count: " + rows.size());

            // 5. Iterar por cada fila de resultado para obtener los datos
            for(WebElement row : rows) {

                // 6. Obtener el arreglo de datos
                List<WebElement> data = row.findElements(By.tagName("td"));

                /*
                Sidenote: El elemento en la posición 1 es ignorado, ya que no es mostrado
                en la tabla de la página y tampoco forma parte de los atributos
                solicitados
                 */
                String firmName = data.get(0).getText();
                String address = data.get(2).getText();
                String country = data.get(3).getText();
                String fromDate = data.get(4).getText();
                String toDate = data.get(5).getText();
                String grounds = data.get(6).getText();

                results.add(new WorldBankResult(firmName, address, country, fromDate, toDate, grounds));

            }

        }
        catch (RuntimeException e) {
            System.err.println("Error durante el scraping de OffShore: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
        finally {
            // 7. Cerrar el driver de Chrome después de procesar
            driver.quit();
        }

        return results;
    }

    /**
     * Scrapea resultados de WorldBank para una entidad dada.
     * 
     * Sidenote: Esta página usa formularios de ASP.NET, por lo que se 
     * usa un flujo con 2 peticiones HTTP:
     * 1) GET inicial para obtener cookies de sesión + hidden fields (__VIEWSTATE, etc.)
     * 2) Sobreescribir inputs relevantes (entity, type, score, submit)
     * 3) POST con application/x-www-form-urlencoded
     * 4) Parsear la tabla de resultados gvSearchResults
     * 
     * @param entity Nombre de la entidad a buscar
     * @param score Nivel mínimo de coincidencia de nombre
     * @return Lista de resultados con atributos: name, address, type, program, list, score
     */
    public List<OFACResults> searchOFAC(final String entity, final String score) {
        System.out.println("======= Scraping: WorldBanks =======");

        List<OFACResults> results = new ArrayList<>();

        // 0. Setear los inputs y valores por defecto para las peticiones
        final String inputName = "ctl00$MainContent$txtLastName";
        final String inputType = "ctl00$MainContent$ddlType";
        final String inputScore = "ctl00$MainContent$Slider1";
        final String inputSliderBound = "ctl00$MainContent$Slider1_Boundcontrol";
        final String inputSearch = "ctl00$MainContent$btnSearch";
        final String valueType = "Entity";
        final String valueSearch = "Search";

        // Normalizar el parámetro de entrada
        String searchEntity = entity.trim();

        try {
            // 1. Configurar las cookies para las request
            CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            HttpClient client = HttpClient.newBuilder()
                    .cookieHandler(cm)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            // 2. Hacer una petición GET inicial para obtener la sesión y hidden fields
            String initialHTML = getHtml(client, baseOFACURL);
            if (initialHTML == null || initialHTML.isEmpty()) {
                return new ArrayList<>();
            }

            Document initialDoc = Jsoup.parse(initialHTML, baseOFACURL);

            // 3. Extraer todos los inputs del forms
            Map<String, String> form = extractAllInputs(initialDoc);
            if (form.isEmpty()) {
                return new ArrayList<>();
            }

            // 4. Sobreescribir los inputs necesarios (nombre de la entidad, tipo, score y submit del botón)
            form.put(inputName, searchEntity);
            form.put(inputType, valueType);
            form.put(inputScore, score);
            form.put(inputSliderBound, score);
            form.put(inputSearch, valueSearch);

            // 5. Obtener la URL destino del form
            Element formElement = initialDoc.selectFirst("form#aspnetForm");
            String postURL = (formElement != null) ? formElement.absUrl("action") : baseOFACURL;

            // 6. Hacer una petición POST para enviar el formulario
            String resultHtml = postForm(client, postURL, form);
            Document resultDoc = Jsoup.parse(resultHtml, postURL);

            // 7. Obtener la tabla de resultados
            Element table = resultDoc.getElementById("gvSearchResults");
            if (table == null) {
                System.out.println("No se encontró la tabla de resultados");
                return new ArrayList<>();
            }

            // 8. Iterar filas y mapear las columnas con el DTO
            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements data = row.select("td");

                // Validación mínima: se esperan al menos 6 columnas visibles
                if (data.size() < 6) {
                    continue;
                }

                String nameResult = data.get(0).text();
                String addressResult = data.get(1).text();
                String typeResult = data.get(2).text();
                String programResult = data.get(3).text();
                String listResult = data.get(4).text();
                String scoreResult = data.get(5).text();

                results.add(new OFACResults(nameResult, addressResult, typeResult, programResult, listResult, scoreResult));
            }

        }
        catch (Exception e) {
            System.err.println("Error durante el scraping de OffShore: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return results;
    }

    /**
     * Ejecuta una petición GET y retorna el HTML de la respuesta
     * 
     * @param client Cliente HTTP configurado
     * @param url URl destino
     * @return HTML en texto plano (UTF-8) o null si ocurre un error
     */
    private String getHtml(HttpClient client, String url) {
        try {
            // 1. Construir la petición HTTP
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .header("User-Agent", userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip")
                    .build();

            // 2. Enviar la petición y capturar la respuesta
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            // 3. Decodificar la respuesta HTTP
            return decodeBody(resp);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decodifica el body de una respuesta HTTP, soporta gzip si aplica
     *
     * @param resp Respuesta HTTP en bytes
     * @return Texto decodificado en UTF-8 o null si falla la lectura
     */
    private  String decodeBody(HttpResponse<byte[]> resp) {
        try {
            // 1. Obtener el valor del encabezado Content-Encoding
            String encoding = resp.headers().firstValue("Content-Encoding").orElse("").toLowerCase();

            // 2. Obtener la respuesta HTTP en bytes
            byte[] bytes = resp.body();
            InputStream input = new ByteArrayInputStream(bytes);

            // 3. Descomprimir el contenido si ha sido codificado como gzip
            if (encoding.contains("gzip")) {
                input = new GZIPInputStream(input);
            }

            // 4. Retornar el contenido en texto plano
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrae todos los inputs del form principal
     *
     * @param htmlDoc Documento HTML parseado con Jsoup
     * @return Mapa de inputs
     */
    private Map<String, String> extractAllInputs (Document htmlDoc) {
        Map<String, String> inputsMap = new LinkedHashMap<>();

        // 1. Iterar sobre todos los objetos del form (input y select, para cubrir textbox y dropdown respectivamente)
        for (Element el : htmlDoc.select("form#aspnetForm input[name], form#aspnetForm select[name]")) {
            String name = el.attr("name");
            String value = "";

            // Quitar parámetros innecesarios para el POST request
            if ("ctl00$MainContent$btnReset".equals(name) || name.contains("Image")) continue;

            // 2. Obtener el valor directamente si se trata de un elemento <input>
            if ("input".equals(el.tagName())) {
                value = el.hasAttr("value") ? el.attr("value") : "";
            }

            // 3. Obtener el valor de la opción seleccionada si se trata de un elemento <select>
            else if ("select".equals(el.tagName())) {
                Element selected = el.selectFirst("option[selected]");
                if (selected == null) {
                    selected = el.selectFirst("option");
                }
                value = selected != null ? selected.attr("value") : "";
            }

            inputsMap.put(name, value);
        }
        return inputsMap;
    }

    /**
     * Envía un POST usando el mapa de inputs como payload
     *
     * @param client Cliente HTTP configurado
     * @param url Endpoint destino (action del form)
     * @param form Campos del formulario
     * @return HTML resultante o null si falla
     */
    private String postForm(HttpClient client, String url, Map<String, String> form) {
        try {
            // 1. Serializar el mapa como querystring form-urlencoded
            String body = form.entrySet().stream()
                    .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                    .collect(Collectors.joining("&"));

            // 2. Construir el POST request
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("User-Agent", userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Origin", "https://sanctionssearch.ofac.treas.gov")
                    .header("Referer", url)
                    .build();

            // 3. Enviar el request
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            // 4. Decodificar body
            return decodeBody(resp);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encode helper
     * @param s Texto a codificar
     * @return Texto codificado
     */
    private String encode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    /**
     * Método de prueba local
     *
     * @param args
     */
    public static void main (String[] args) {
        WebScraper ws = new WebScraper();
        ws.searchOffShore("aero");
        ws.searchWorldBank("aero");
        ws.searchOFAC("aero", "100");
    }

}

