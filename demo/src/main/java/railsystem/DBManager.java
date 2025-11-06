package railsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;

public class DBManager {
    private final String url;

    public DBManager(String url) {
        this.url = url;
        createTables();
    }

    private void createTables() {
        String createConnectionsTable = """
            CREATE TABLE IF NOT EXISTS connections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                route_id TEXT NOT NULL,
                train_type TEXT NOT NULL,
                operating_days TEXT NOT NULL,
                first_class_rate DECIMAL(10,2) NOT NULL,
                second_class_rate DECIMAL(10,2) NOT NULL,
                departure_city TEXT NOT NULL,
                departure_time TEXT NOT NULL,
                arrival_city TEXT NOT NULL,
                arrival_time TEXT NOT NULL,
                next_day BOOLEAN NOT NULL
            )
            """;

        try (java.sql.Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createConnectionsTable);
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    public void saveConnections(List<railsystem.Connection> connections) {
        String insertConnection = """
            INSERT INTO connections (
                route_id, train_type, operating_days, first_class_rate,
                second_class_rate, departure_city, departure_time,
                arrival_city, arrival_time, next_day
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (java.sql.Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertConnection)) {
            for (railsystem.Connection connData : connections) {
                pstmt.setString(1, connData.getRouteId());
                pstmt.setString(2, connData.getTrain().getTrainType());
                pstmt.setString(3, connData.getSchedule().getOperatingDays().toString());
                pstmt.setBigDecimal(4, connData.getTicketRates().getFirstClass());
                pstmt.setBigDecimal(5, connData.getTicketRates().getSecondClass());
                pstmt.setString(6, connData.getDepartureStop().toString());
                pstmt.setString(7, connData.getDepartureStop().getScheduledStop().toString());
                pstmt.setString(8, connData.getArrivalStop().toString());
                pstmt.setString(9, connData.getArrivalStop().getScheduledStop().toString());
                pstmt.setBoolean(10, connData.getArrivalStop().isNextDay());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Error saving connections: " + e.getMessage());
        }
    }

    public List<railsystem.Connection> loadConnections() {
        List<railsystem.Connection> connections = new ArrayList<>();
        String selectConnections = """
            SELECT id, route_id, train_type, operating_days, first_class_rate,
                   second_class_rate, departure_city, departure_time,
                   arrival_city, arrival_time, next_day
            FROM connections
            """;
 
        try (java.sql.Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(selectConnections)) {

                while (rs.next()) {
                    // Parse operating days back from string
                    String operatingDaysStr = rs.getString("operating_days");
                    // You'll need to implement parseOperatingDays method in Loader
                    
                    railsystem.Connection connection = railsystem.Connection.of(
                        rs.getString("route_id"),
                        rs.getString("train_type"),
                        parseOperatingDaysFromString(operatingDaysStr), // You need to implement this
                        rs.getBigDecimal("first_class_rate"),
                        rs.getBigDecimal("second_class_rate"),
                        rs.getString("departure_city"),
                        java.time.LocalTime.parse(rs.getString("departure_time")),
                        rs.getString("arrival_city"),
                        java.time.LocalTime.parse(rs.getString("arrival_time")),
                        rs.getBoolean("next_day")
                    );
                    connections.add(connection);
                }
        } catch (SQLException e) {
            System.out.println("Error loading connections: " + e.getMessage());
        }
        return connections;
    }

    private Set<DayOfWeek> parseOperatingDaysFromString(String operatingDaysStr) {

        String cleaned = operatingDaysStr.replaceAll("[\\[\\]]", "");
        return Arrays.stream(cleaned.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(DayOfWeek::valueOf)
            .collect(Collectors.toSet());
} 

    public boolean hasConnections() {
        String countQuery = "SELECT COUNT(*) AS total FROM connections";
        try (java.sql.Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(countQuery)) {
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking connections: " + e.getMessage());
        }
        return false;
    }
}

