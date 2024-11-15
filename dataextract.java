import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class dataextract {

    public static void main(String[] args) {
        // Path to the JSON file
        String jsonFilePath = "data.json"; // Replace with your actual file path

        // Database credentials
        String url = "jdbc:mysql://localhost:3306/biobiversity"; // Replace with your DB URL
        String user = "root"; // Replace with your DB username
        String password = ""; // Replace with your DB password
        
        // Read JSON and insert data into the database
        try {
            // Parse JSON file
            JsonNode rootNode = readJsonFile(jsonFilePath);

            // Establish database connection
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                // Loop through the JSON array and insert each record
                if (rootNode.isArray()) {
                    for (JsonNode recordNode : rootNode) {
                        String indicatorValue = recordNode.get("indicator").get("value").asText();
                        String countryValue = recordNode.get("country").get("value").asText();
                        String date = recordNode.get("date").asText();
                        Double value = recordNode.get("value").isNull() ? null : recordNode.get("value").asDouble();

                        // Insert data into the database
                        insertDataIntoDatabase(conn, indicatorValue, countryValue, date, value);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to read JSON file
    private static JsonNode readJsonFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(new File(filePath));
    }

    // Method to insert data into the database
    private static void insertDataIntoDatabase(Connection conn, String indicator, String country, String date, Double value) {
        String insertSQL = "INSERT INTO indicators_data (indicator, country, date, value) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, indicator);
            stmt.setString(2, country);
            stmt.setString(3, date);
            if (value != null) {
                stmt.setDouble(4, value);
            } else {
                stmt.setNull(4, java.sql.Types.DOUBLE);
            }
            stmt.executeUpdate();
            System.out.println("Inserted: " + indicator + ", " + country + ", " + date + ", " + value);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
