package railsystem;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
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
        ensureDatabaseDirectoryExists();
        createTables();
    }

    private void ensureDatabaseDirectoryExists() {
        if (url.startsWith("jdbc:sqlite:")) {
            String dbPath = url.substring("jdbc:sqlite:".length());
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Created database directory: " + parentDir.getPath());
                } else {
                    System.out.println("Warning: Could not create database directory: " + parentDir.getPath());
                }
            }
        }
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
        String createTripsTable = """
                CREATE TABLE IF NOT EXISTS trips (
                    trip_id INTEGER PRIMARY KEY,
                    total_first_class_rate DECIMAL(10,2) NOT NULL,
                    total_second_class_rate DECIMAL(10,2) NOT NULL,
                    total_duration_minutes INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createTripConnectionsTable = """
                CREATE TABLE IF NOT EXISTS trip_connections (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    connection_id INTEGER NOT NULL,
                    sequence_order INTEGER NOT NULL,
                    FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
                    FOREIGN KEY (connection_id) REFERENCES connections(id)
                )
                """;

        String createTravellersTable = """
                CREATE TABLE IF NOT EXISTS travellers (
                    id TEXT PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createBookingsTable = """
                CREATE TABLE IF NOT EXISTS bookings (
                    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createTicketsTable = """
                CREATE TABLE IF NOT EXISTS tickets (
                    ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id INTEGER NOT NULL,
                    trip_id INTEGER NOT NULL,
                    traveller_id TEXT NOT NULL,
                    class_type TEXT NOT NULL,
                    ticket_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
                    FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
                    FOREIGN KEY (traveller_id) REFERENCES travellers(id)
                )
                """;

        try (java.sql.Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            stmt.execute(createConnectionsTable);
            stmt.execute(createTripsTable);
            stmt.execute(createTripConnectionsTable);
            stmt.execute(createTravellersTable);
            stmt.execute(createBookingsTable);
            stmt.execute(createTicketsTable);
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
                pstmt.setString(6, connData.getDepartureStop().getCity().getName());
                pstmt.setString(7, connData.getDepartureStop().getScheduledStop().toString());
                pstmt.setString(8, connData.getArrivalStop().getCity().getName());
                pstmt.setString(9, connData.getArrivalStop().getScheduledStop().toString());
                pstmt.setBoolean(10, connData.getArrivalStop().isNextDay());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Error saving connections: " + e.getMessage());
        }
    }

    public void saveTrip(Trip trip) {
        String insertTrip = """
                INSERT INTO trips (trip_id, total_first_class_rate, total_second_class_rate, total_duration_minutes)
                VALUES (?, ?, ?, ?)
                """;

        String insertTripConnection = """
                INSERT INTO trip_connections (trip_id, connection_id, sequence_order)
                VALUES (?, ?, ?)
                """;

        try (java.sql.Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            try (PreparedStatement tripStmt = conn.prepareStatement(insertTrip);
                    PreparedStatement connStmt = conn.prepareStatement(insertTripConnection)) {

                // Save trip
                tripStmt.setLong(1, trip.getId());
                tripStmt.setBigDecimal(2, trip.getTotalFirstClassRate());
                tripStmt.setBigDecimal(3, trip.getTotalSecondClassRate());
                tripStmt.setLong(4, trip.getTotalDuration().toMinutes());
                tripStmt.executeUpdate();

                // Save connections in order
                int order = 1;
                for (Connection connection : trip.getConnections()) {
                    int connectionId = getConnectionId(connection.getRouteId());
                    connStmt.setLong(1, trip.getId());
                    connStmt.setInt(2, connectionId);
                    connStmt.setInt(3, order++);
                    connStmt.addBatch();
                }
                connStmt.executeBatch();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error saving trip: " + e.getMessage());
        }
    }

    public void saveTraveller(String travellerId, String firstName, String lastName) {
        String insertTraveller = """
                INSERT OR IGNORE INTO travellers (id, first_name, last_name)
                VALUES (?, ?, ?)
                """;

        try (java.sql.Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(insertTraveller)) {
            pstmt.setString(1, travellerId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving traveller: " + e.getMessage());
        }
    }

    public void saveBooking(Booking booking) {
        String insertBooking = "INSERT INTO bookings DEFAULT VALUES";
        String insertTraveller = """
                INSERT OR IGNORE INTO travellers (id, first_name, last_name)
                VALUES (?, ?, ?)
                """;
        String insertTrip = """
                INSERT OR IGNORE INTO trips (trip_id, total_first_class_rate, total_second_class_rate, total_duration_minutes)
                VALUES (?, ?, ?, ?)
                """;
        String insertTripConnection = """
                INSERT OR IGNORE INTO trip_connections (trip_id, connection_id, sequence_order)
                VALUES (?, ?, ?)
                """;
        String insertTicket = """
                INSERT INTO tickets (booking_id, trip_id, traveller_id, class_type)
                VALUES (?, ?, ?, ?)
                """;

        try (java.sql.Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);

            try (PreparedStatement bookingStmt = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement travellerStmt = conn.prepareStatement(insertTraveller);
                    PreparedStatement tripStmt = conn.prepareStatement(insertTrip);
                    PreparedStatement connStmt = conn.prepareStatement(insertTripConnection);
                    PreparedStatement ticketStmt = conn.prepareStatement(insertTicket)) {

                // Create booking
                bookingStmt.executeUpdate();
                var keys = bookingStmt.getGeneratedKeys();
                if (!keys.next()) {
                    throw new SQLException("Failed to get booking ID");
                }
                int bookingId = keys.getInt(1);

                // Save each ticket and related data
                for (Ticket ticket : booking.getTickets()) {
                    Traveller traveller = ticket.getTraveller();
                    Trip trip = ticket.getTrip();

                    // Save traveller (INSERT OR IGNORE prevents duplicates)
                    travellerStmt.setString(1, traveller.getId());
                    travellerStmt.setString(2, traveller.getFirstName());
                    travellerStmt.setString(3, traveller.getLastName());
                    travellerStmt.executeUpdate();

                    // Save trip if not already exists (INSERT OR IGNORE)
                    tripStmt.setLong(1, trip.getId());
                    tripStmt.setBigDecimal(2, trip.getTotalFirstClassRate());
                    tripStmt.setBigDecimal(3, trip.getTotalSecondClassRate());
                    tripStmt.setLong(4, trip.getTotalDuration().toMinutes());
                    tripStmt.executeUpdate();

                    // Save trip connections
                    int order = 1;
                    for (Connection connection : trip.getConnections()) {
                        int connectionId = getConnectionId(connection.getRouteId());
                        connStmt.setLong(1, trip.getId());
                        connStmt.setInt(2, connectionId);
                        connStmt.setInt(3, order++);
                        connStmt.addBatch();
                    }
                    connStmt.executeBatch();
                    connStmt.clearBatch();

                    // Save ticket
                    ticketStmt.setInt(1, bookingId);
                    ticketStmt.setLong(2, trip.getId());
                    ticketStmt.setString(3, traveller.getId());
                    ticketStmt.setString(4, ticket.getClassRate());
                    ticketStmt.addBatch();
                }
                ticketStmt.executeBatch();

                conn.commit();
                System.out.println("✓ Booking saved successfully!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error saving booking: " + e.getMessage());
            e.printStackTrace();
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
                String operatingDaysStr = rs.getString("operating_days");

                railsystem.Connection connection = railsystem.Connection.of(
                        rs.getString("route_id"),
                        rs.getString("train_type"),
                        parseOperatingDaysFromString(operatingDaysStr),
                        rs.getBigDecimal("first_class_rate"),
                        rs.getBigDecimal("second_class_rate"),
                        rs.getString("departure_city"),
                        java.time.LocalTime.parse(rs.getString("departure_time")),
                        rs.getString("arrival_city"),
                        java.time.LocalTime.parse(rs.getString("arrival_time")),
                        rs.getBoolean("next_day"));
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

    public boolean travellerExists(String travellerId) {
        String query = "SELECT COUNT(*) AS total FROM travellers WHERE id = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, travellerId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking traveller existence: " + e.getMessage());
        }
        return false;
    }

    private List<Connection> getTripConnections(long tripId) throws SQLException {
        List<Connection> connections = new ArrayList<>();

        String query = """
                SELECT
                    c.route_id,
                    c.train_type,
                    c.operating_days,
                    c.first_class_rate,
                    c.second_class_rate,
                    c.departure_city,
                    c.departure_time,
                    c.arrival_city,
                    c.arrival_time,
                    c.next_day,
                    tc.sequence_order
                FROM trip_connections tc
                JOIN connections c ON tc.connection_id = c.id
                WHERE tc.trip_id = ?
                ORDER BY tc.sequence_order
                """;

        try (java.sql.Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, tripId);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                Connection connection = Connection.of(
                        rs.getString("route_id"),
                        rs.getString("train_type"),
                        parseOperatingDaysFromString(rs.getString("operating_days")),
                        rs.getBigDecimal("first_class_rate"),
                        rs.getBigDecimal("second_class_rate"),
                        rs.getString("departure_city"),
                        java.time.LocalTime.parse(rs.getString("departure_time")),
                        rs.getString("arrival_city"),
                        java.time.LocalTime.parse(rs.getString("arrival_time")),
                        rs.getBoolean("next_day"));
                connections.add(connection);
            }
        }

        return connections;
    }

    public List<Ticket> getTravellerBookings(String travellerId) {
        List<Ticket> tickets = new ArrayList<>();

        String query = """
                SELECT
                    t.ticket_id,
                    t.class_type,
                    t.ticket_date,
                    tr.trip_id,
                    tr.total_first_class_rate,
                    tr.total_second_class_rate,
                    tr.total_duration_minutes,
                    tv.id as traveller_id,
                    tv.first_name,
                    tv.last_name
                FROM tickets t
                JOIN trips tr ON t.trip_id = tr.trip_id
                JOIN travellers tv ON t.traveller_id = tv.id
                WHERE t.traveller_id = ?
                ORDER BY t.ticket_date DESC
                """;

        try (java.sql.Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, travellerId);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                // Reconstruct Traveller
                Traveller traveller = Traveller.getInstance(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("traveller_id"));

                // Reconstruct Trip with original ID
                long tripId = rs.getLong("trip_id");
                List<Connection> connections = getTripConnections(tripId);
                BigDecimal totalFirstClassRate = rs.getBigDecimal("total_first_class_rate");
                BigDecimal totalSecondClassRate = rs.getBigDecimal("total_second_class_rate");
                Duration totalDuration = Duration.ofMinutes(rs.getLong("total_duration_minutes"));

                Trip trip = new Trip(tripId, connections, totalFirstClassRate,
                        totalSecondClassRate, totalDuration);

                // Create Ticket
                Ticket ticket = new Ticket(trip, traveller, rs.getString("class_type"));
                tickets.add(ticket);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving traveller bookings: " + e.getMessage());
        }

        return tickets;
    }

    private int getConnectionId(String routeId) throws SQLException {
        String query = "SELECT id FROM connections WHERE route_id = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, routeId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            throw new SQLException("Connection not found: " + routeId);
        }
    }

    public void loadTravellers() {
        String query = "SELECT id, first_name, last_name FROM travellers";

        try (java.sql.Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                var rs = stmt.executeQuery(query)) {

            int count = 0;
            while (rs.next()) {
                String id = rs.getString("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                // Load traveller into registry if not already present
                if (!Traveller.exists(id)) {
                    Traveller.getInstance(firstName, lastName, id);
                    count++;
                }
            }

            if (count > 0) {
                System.out.println("[INFO] Loaded " + count + " traveller(s) from database.");
            }
        } catch (SQLException e) {
            System.out.println("Error loading travellers: " + e.getMessage());
        }
    }
}
