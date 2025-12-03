package railsystem;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DatabaseInitializer {
    private final String databaseUrl;
    private final DBManager dbManager;
    private final Terminal terminal;

    public DatabaseInitializer(String databaseUrl, Terminal terminal) {
        this.databaseUrl = databaseUrl;
        this.dbManager = new DBManager(databaseUrl);
        this.terminal = terminal;
    }

    public boolean initializeDatabase() {
        try (var conn = DriverManager.getConnection(databaseUrl)) {
            if (conn != null) {
                var meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("Database connection established.");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Database connection failed: " + e.getMessage());
            System.out.println("  The application may not function properly without a database connection.");
        }
        return false;
    }

    public boolean loadConnections() {
        try {
            List<Connection> connections;

            if (dbManager.hasConnections()) {
                System.out.println("[INFO] Loading connections from database...");
                connections = dbManager.loadConnections();

                if (connections == null || connections.isEmpty()) {
                    System.out.println("[WARN] Database query returned no connections. Attempting to load from CSV...");
                    connections = loadFromCSV();
                } else {
                    System.out.println(
                            "[INFO] Successfully loaded " + connections.size() + " connections from database.");
                }
            } else {
                System.out.println("[INFO] Database is empty. Loading connections from CSV file...");
                connections = loadFromCSV();
            }

            if (connections == null || connections.isEmpty()) {
                System.out.println(
                        "[ERROR] Failed to load connections. The application cannot function without connection data.");
                return false;
            }

            terminal.setConnectionRepo(new ConnectionRepository(connections));
            terminal.setDbManager(dbManager);

            // Load existing travellers from database into registry
            dbManager.loadTravellers();

            System.out
                    .println("[INFO] Connection repository initialized with " + connections.size() + " connections.\n");
            return true;

        } catch (Exception e) {
            System.out.println("[ERROR] Error loading connections: " + e.getMessage());
            return false;
        }
    }

    private List<Connection> loadFromCSV() {
        try {
            System.out.println("[INFO] Reading CSV file...");
            List<Connection> connections = terminal.getLoader()
                    .loadConnections(Paths.get("demo/src/main/resources/eu_rail_network.csv"));

            if (connections != null && !connections.isEmpty()) {
                System.out.println("[INFO] Parsed " + connections.size() + " connections from CSV.");
                System.out.println("[INFO] Saving connections to database...");
                dbManager.saveConnections(connections);
                System.out.println("[INFO] Connections saved to database successfully.");
            } else {
                System.out.println("[WARN] CSV file was empty or could not be parsed.");
            }

            return connections;
        } catch (Exception e) {
            System.out.println("[ERROR] Error loading CSV file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
