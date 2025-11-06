package railsystem;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles database initialization and connection loading.
 */
public class DatabaseInitializer {
    private final String databaseUrl;
    private final DBManager dbManager;
    private final Terminal terminal;

    public DatabaseInitializer(String databaseUrl, Terminal terminal) {
        this.databaseUrl = databaseUrl;
        this.dbManager = new DBManager(databaseUrl);
        this.terminal = terminal;
    }

    /**
     * Initializes the database connection and verifies it's working.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public boolean initializeDatabase() {
        try (var conn = DriverManager.getConnection(databaseUrl)) {
            if (conn != null) {
                var meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Database connection failed: " + e.getMessage());
            System.out.println("  The application may not function properly without a database connection.");
        }
        return false;
    }

    /**
     * Loads connections either from database or CSV file.
     * 
     * @return true if connections were loaded successfully, false otherwise
     */
    public boolean loadConnections() {
        try {
            List<Connection> connections;

            if (dbManager.hasConnections()) {
                System.out.println("Loading connections from database...");
                connections = dbManager.loadConnections();

                if (connections == null || connections.isEmpty()) {
                    System.out.println("[WARN] No connections found in database. Attempting to load from CSV...");
                    connections = loadFromCSV();
                }
            } else {
                connections = loadFromCSV();
            }

            if (connections == null || connections.isEmpty()) {
                System.out.println(
                        "[ERROR] Failed to load connections. The application cannot function without connection data.");
                return false;
            }

            terminal.setConnectionRepo(new ConnectionRepository(connections));
            System.out.println("[INFO] Loaded " + connections.size() + " connections.\n");
            return true;

        } catch (Exception e) {
            System.out.println("[ERROR] Error loading connections: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to load connections from CSV file.
     */
    private List<Connection> loadFromCSV() {
        try {
            System.out.println("Loading connections from CSV file...");
            List<Connection> connections = terminal.getLoader()
                    .loadConnections(Paths.get("demo/src/main/resources/eu_rail_network.csv"));

            if (connections != null && !connections.isEmpty()) {
                System.out.println("Saving connections to database...");
                dbManager.saveConnections(connections);
            }

            return connections;
        } catch (Exception e) {
            System.out.println("[ERROR] Error loading CSV file: " + e.getMessage());
            return null;
        }
    }
}
