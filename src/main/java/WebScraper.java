import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class WebScraper {

    private String baseURLOffShore = "https://offshoreleaks.icij.org/";
    private String baseURLWorlBank = "https://projects.worldbank.org/en/projects-operations/procurement/debarred-firms";

    public void searchOffShore(String entity) {
        try {
            // 0. Setear los parámetros de búsqueda
            int from = 0;
            int resultsPerPage = 100;

            String trailParams = "&c=&j=&d=&cat=Entity";
            String searchEntity = entity.trim().replace(" ", "+");

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

    public void searchWorldBank(String entity) {

    }

    public static void main(String[] args) {
        System.out.println("Starting Test...");

        WebScraper ws = new WebScraper();
        String entityTesting = "AERO CONTINENT";
        ws.searchOffShore(entityTesting);

        System.out.println("Test Finished!");
    }
}
