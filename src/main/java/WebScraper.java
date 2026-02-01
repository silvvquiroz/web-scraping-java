import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.List;

public class WebScraper {

    /**
     *
     * @param entity
     */
    public void searchOffShore(final String entity) {
        final String baseURLOffShore = "https://offshoreleaks.icij.org/";

        // 0. Setear los parámetros de búsqueda
        int from = 0;
        int resultsPerPage = 100;

        String trailParams = "&c=&j=&d=&cat=Entity";
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

                System.out.println(searchURL);
                // 2. Conectar a la URL y obtener el HTML de la página
                Document doc = Jsoup.connect(searchURL).get();

                // Testing: Imprimir el título de la página
                System.out.println("Título de la página: " + doc.title());

                // 3. Seleccionar las filas de la tabla de resultados
                /*
                Sidenote: El DOM de la página tiene solo 1 tabla;
                por lo tanto, las filas se pueden seleccionar directamente
                */
                Elements tableBody = doc.getElementsByTag("tbody");

                if (tableBody.isEmpty()) {
                    break;
                }

                Elements rows = tableBody.getFirst().getElementsByTag("tr");

                System.out.println("Results: " + rows.size());

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

                    // Testing: Imprimir los datos scrapeados
                    System.out.println("Entity: " + entityName);
                    System.out.println("Jurisdiction: " + jurisdiction);
                    System.out.println("Linked To: " + linkedTo);
                    System.out.println("Data From: " + dataFrom);
                }

                from += resultsPerPage;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param entity
     */
    public void searchWorldBank(final String entity) {
        final String baseURLWorlBank = "https://projects.worldbank.org/en/projects-operations/procurement/debarred-firms";

        // 0. Configurar el web driver (en este caso, ChromeDriver) y los parámetros de búsqueda
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        // Iniciar el WebDriver con ChromeDriver
        WebDriver driver = new ChromeDriver(options);

        String searchEntity = entity.trim();

        try {
            // 1. Conectar a la URL de WorldBank
            driver.get(baseURLWorlBank);

            // 2. Localizar el textbox de búsqueda
            /*
            Sidenote: El nombre de la entidad se ingresa en un elemento de forma:
            tag: <input>
            id: "category"
             */
            WebElement textBox = driver.findElement(By.id("category"));

            // Esperar a que los elementos estén localizados y listos para actualizarse
            Thread.sleep(2000);

            // 3. Ingresar el texto a buscar (nombre de la entidad)
            textBox.sendKeys(searchEntity);

            // 4. Obtener el HTML actualizado y seleccionar las filas de la tabla
            WebElement tableParent = driver.findElement(By.id("k-debarred-firms")).findElement(By.className("k-grid-content"));
            List<WebElement> rows = tableParent.findElements(By.tagName("tr"));

            // Testing: Número de filas encontradas
            System.out.println("Results count: " + rows.size());

            // 5. Iterar por cada fila de resultado para obtener los datos
            for(WebElement row : rows) {

                // Testing: Imprimir el HTML actualizado
                //String html = row.getAttribute("outerHTML");
                //System.out.println(html);

                // Obtener el arreglo de datos
                List<WebElement> data = row.findElements(By.tagName("td"));
                String firmName = data.get(0).getText();
                String address = data.get(2).getText();
                String country = data.get(3).getText();
                String fromDate = data.get(4).getText();
                String toDate = data.get(5).getText();
                String grounds = data.get(6).getText();
                /*
                El elemento en la posición 1 es ignorado, ya que no es mostrado
                en la tabla de la página y tampoco forma parte de los atributos
                solicitados
                 */

                // Testing: Imprimir los datos scrapeados
                System.out.println("Firm Name: " + firmName);
                System.out.println("Address: " + address);
                System.out.println("Country: " + country);
                System.out.println("From Date: " + fromDate);
                System.out.println("To Date: " + toDate);
                System.out.println("Grounds: " + grounds);

            }

        }
        catch (RuntimeException | InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            // x. Cerrar el driver de Chrome después de procesar
            driver.quit();
        }
    }

    /**
     *
     * @param entity
     * @param score
     */
    public void searchOFACAPI(final String entity, final int score) {

    }

    public static void main(String[] args) {
        System.out.println("Starting Test...");

        WebScraper ws = new WebScraper();
        String entityTesting = "aero";
        ws.searchOffShore(entityTesting);
        ws.searchWorldBank(entityTesting);

        System.out.println("Test Finished!");
    }
}
