import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WorldBankAPI {

    private static final String API_URL = "https://api.worldbank.org/v2/country/all/indicator/ER.MRN.PTMR.ZS?format=json";

    public static void main(String[] args) {
        String data = fetchWorldBankData();
        System.out.println(data);
    }

    public static String fetchWorldBankData() {
        String result = "";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_URL))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                result = response.body();
            } else {
                System.out.println("Error: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
