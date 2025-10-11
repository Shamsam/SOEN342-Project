import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Terminal terminal = Terminal.getInstance();
        System.out.println("Welcome to the Rail System Demo!");
        List<Connection> connections = terminal.getLoader()
                .loadConnections(Paths.get("demo/resources/eu_rail_network.csv"));
        terminal.setConnectionRepo(new ConnectionRepository(connections));
        System.out.println("Loaded " + connections.size() + " connections.");

        System.out.println("[0] Display all connections");
        System.out.println("[1] Start a search");
        System.out.println("Please enter your choice:");
        int choice = -1;
        try {
            choice = System.in.read();
        } catch (java.io.IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }

        if (choice == '0') {
            for (Connection conn : terminal.getConnectionRepo().getConnections()) {
                System.out.println(conn.toString());
            }
        } else if (choice == '1') {
            SearchCriteria criteria = new SearchCriteria();
            // For simplicity, we hardcode some search criteria here
            criteria.setDepartureCity("Amsterdam");
            criteria.setArrivalCity("Rotterdam");
            criteria.setEarliestDeparture(java.time.LocalTime.of(14, 0));

            List<Connection> results = terminal.getConnectionRepo().search(criteria);
            System.out.println("Search Results:");
            for (Connection conn : results) {
                System.out.println(conn);
            }
        } else {
            System.out.println("Invalid choice. Exiting.");
        }
    }
}
