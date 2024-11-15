import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebScraper {
    static final String DB_URL = "jdbc:mysql://localhost:3306/biodiversity";
    static final String USER = "root";
    static final String PASSWORD = "";
    static final String SCRAPE_URL = "https://www.gbif.org/dataset/c7c1cc3c-7f15-493d-8501-e64b9ad3df6d";
    static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClientRequest(clientSocket);
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        ) {
            // Read the HTTP request line (e.g., "GET /scrape HTTP/1.1")
            String requestLine = in.readLine();
            if (requestLine == null) return;

            // Determine request method and path
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            if (method.equals("GET") && path.equals("/scrape")) {
                // Handle GET /scrape
                String scrapedData = scrapeData();
                sendHttpResponse(out, 200, "application/json", "{\"data\":\"" + scrapedData + "\"}");
            } else if (method.equals("POST") && path.equals("/save")) {
                // Handle POST /save
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    requestBody.append(line);
                }
                // Assuming data is sent as a simple JSON: {"eventID":"...","link":"..."}
                String[] params = requestBody.toString().split("&");
                String eventID = params[0].split("=")[1];
                String link = params[1].split("=")[1];

                insertData(eventID, link);
                sendHttpResponse(out, 200, "application/json", "{\"status\":\"success\",\"message\":\"Data saved.\"}");
            } else {
                // Handle unknown path
                sendHttpResponse(out, 404, "application/json", "{\"error\":\"Not Found\"}");
            }
        } catch (IOException e) {
            System.out.println("Client handling error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Utility to send HTTP response
    private static void sendHttpResponse(BufferedWriter out, int statusCode, String contentType, String body) throws IOException {
        out.write("HTTP/1.1 " + statusCode + " \r\n");
        out.write("Content-Type: " + contentType + "\r\n");
        out.write("Content-Length: " + body.length() + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
    }

    // Method to connect to the database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    // Method to insert data into the database
    public static void insertData(String eventID, String link) {
        String query = "INSERT INTO datasets (heading, link) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, eventID != null ? eventID : "");
            pstmt.setString(2, link != null ? link : "");
            int rowsAffected = pstmt.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted. Event ID: " + eventID + ", Link: " + link);
        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to scrape data from the target URL
    public static String scrapeData() {
        StringBuilder result = new StringBuilder();
        try {
            URL website = new URL(SCRAPE_URL);
            HttpURLConnection connection = (HttpURLConnection) website.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error connecting to the URL: " + e.getMessage());
            e.printStackTrace();
        }
        return result.toString();
    }
}
