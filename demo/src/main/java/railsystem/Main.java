package railsystem;

import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Terminal terminal = Terminal.getInstance();
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           Welcome to the Rail System Booking System!           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Connection> connections = terminal.getLoader()
                .loadConnections(Paths.get("demo/src/main/resources/eu_rail_network.csv"));
        terminal.setConnectionRepo(new ConnectionRepository(connections));
        System.out.println("Loaded " + connections.size() + " connections.\n");

        System.out.println("Please select an option:");
        System.out.println("[0] Display all connections");
        System.out.println("[1] Start a search");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("0")) {
            System.out.println("\n════════════ ALL CONNECTIONS ════════════\n");
            for (Connection conn : terminal.getConnectionRepo().getConnections()) {
                System.out.println(conn.toString());
                System.out.println("─────────────────────────────────────");
            }
        } else if (choice.equals("1")) {
            List<String> searchArgs = new ArrayList<>();

            System.out.println("\n════════════ SEARCH CRITERIA ════════════\n");
            System.out.println("Press Enter to skip any field.\n");

            System.out.print("Departure city: ");
            String departureCity = scanner.nextLine().trim();
            searchArgs.add(departureCity);

            System.out.print("Earliest departure time (HH:MM): ");
            String earliestDeparture = scanner.nextLine().trim();
            searchArgs.add(earliestDeparture);

            System.out.print("Arrival city: ");
            String arrivalCity = scanner.nextLine().trim();
            searchArgs.add(arrivalCity);

            System.out.print("Next day arrival (true/false): ");
            String nextDayInput = scanner.nextLine().trim();
            searchArgs.add(nextDayInput);

            System.out.print("Latest arrival time (HH:MM): ");
            String latestArrival = scanner.nextLine().trim();
            searchArgs.add(latestArrival);

            System.out.print("Travel days (e.g., MONDAY,TUESDAY): ");
            String daysInput = scanner.nextLine().trim();
            searchArgs.add(daysInput);

            System.out.print("Preferred train type: ");
            String preferredTrain = scanner.nextLine().trim();
            searchArgs.add(preferredTrain);

            System.out.print("Max first class rate (€): ");
            String firstClassRateInput = scanner.nextLine().trim();
            searchArgs.add(firstClassRateInput);

            System.out.print("Max second class rate (€): ");
            String secondClassRateInput = scanner.nextLine().trim();
            searchArgs.add(secondClassRateInput);

            List<Trip> searchResult = terminal.createSearch(searchArgs);

            if (searchResult != null && !searchResult.isEmpty()) {
                terminal.displayTrips(searchResult, Terminal.SortOption.DURATION);

                // Sorting menu loop
                boolean continueViewing = true;
                while (continueViewing) {
                    System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
                    System.out.println("║                         SORTING OPTIONS                        ║");
                    System.out.println("╚════════════════════════════════════════════════════════════════╝");
                    System.out.println("\nHow would you like to sort the results?");
                    System.out.println("[1] Duration (Fastest first)");
                    System.out.println("[2] Price - First Class (Cheapest first)");
                    System.out.println("[3] Price - Second Class (Cheapest first)");
                    System.out.println("[4] Departure Time (Earliest first)");
                    System.out.println("[5] Arrival Time (Earliest first)");
                    System.out.println("[6] Number of Transfers (Fewest first)");
                    System.out.println("[0] Exit");
                    System.out.print("\nEnter your choice: ");

                    String sortChoice = scanner.nextLine().trim();

                    Terminal.SortOption sortOption = null;
                    switch (sortChoice) {
                        case "1":
                            sortOption = Terminal.SortOption.DURATION;
                            break;
                        case "2":
                            sortOption = Terminal.SortOption.PRICE_FIRST_CLASS;
                            break;
                        case "3":
                            sortOption = Terminal.SortOption.PRICE_SECOND_CLASS;
                            break;
                        case "4":
                            sortOption = Terminal.SortOption.DEPARTURE_TIME;
                            break;
                        case "5":
                            sortOption = Terminal.SortOption.ARRIVAL_TIME;
                            break;
                        case "6":
                            sortOption = Terminal.SortOption.TRANSFERS;
                            break;
                        case "0":
                            continueViewing = false;
                            System.out.println("\nThank you for using the Rail System Booking System!");
                            break;
                        default:
                            System.out.println("\n  Invalid choice. Please try again.");
                    }

                    if (sortOption != null) {
                        terminal.sortTrips(searchResult, sortOption);
                        terminal.displayTrips(searchResult, sortOption);
                    }
                }
            } else {
                System.out.println("\nNo trips found. Please try adjusting your search criteria.");
            }
        } else {
            System.out.println("\n  Invalid choice. Exiting.");
        }

        scanner.close();
    }
}
