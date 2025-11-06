package railsystem;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        String url = "jdbc:sqlite:db/project.db"; // path from inside /demo/src
        DBManager dbManager = new DBManager(url);

        try (var conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                var meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }

        Terminal terminal = Terminal.getInstance();
        Scanner scanner = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           Welcome to the Rail System Booking System!           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        List<Connection> connections;

        // Check if connections exist in database
        if (dbManager.hasConnections()) {
            System.out.println("Loading connections from database...");
            connections = dbManager.loadConnections();
        } else {
            System.out.println("Loading connections from CSV file...");
            connections = terminal.getLoader()
                    .loadConnections(Paths.get("demo/src/main/resources/eu_rail_network.csv"));
            
            System.out.println("Saving connections to database...");
            dbManager.saveConnections(connections);
        }
        
        terminal.setConnectionRepo(new ConnectionRepository(connections));
        System.out.println("Loaded " + connections.size() + " connections.\n");


        boolean running = true;
        while (running) {
            System.out.println("Please select an option:");
            System.out.println("[0] Display all connections");
            System.out.println("[1] Start a search");
            System.out.println("[2] View your trips");
            System.out.println("[q] Quit");
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
                        System.out.println("[7] Create a Booking");
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
                            case "7":
                                System.out.println("Input trip option you'd like to book: ");
                                int option = scanner.nextInt();
                                scanner.nextLine(); // consume newline

                                ArrayList<String> nameList = new ArrayList<>();
                                System.out.println(
                                        "Input passenger(s) first name(s), last name(s), and id(s) (press Enter on empty line to finish): ");
                                while (true) {
                                    String name = scanner.nextLine().trim();
                                    if (name.isEmpty()) {
                                        break;
                                    }
                                    nameList.add(name);
                                }
                                System.out.println("Passengers: " + nameList);
                                System.out.println("Input class rate (First Class/Second Class): ");
                                String classRate = scanner.nextLine().trim();

                                // Validate the option index
                                if (option < 1 || option > searchResult.size()) {
                                    System.out.println("Invalid option. Please select a valid trip number.");
                                    break;
                                }

                                Booking currentBooking = terminal.createBooking(
                                        searchResult.get(option - 1),
                                        nameList,
                                        classRate);
                                System.out.println(currentBooking);
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
                }
            } else if (choice.equals("2")) {
                System.out.println("\nEnter your name and id to view your trips.");
                System.out.print("First Name: ");
                String firstName = scanner.nextLine().trim();
                System.out.print("Last Name: ");
                String lastName = scanner.nextLine().trim();
                System.out.print("ID: ");
                String id = scanner.nextLine().trim();
                Traveller traveller;
                try {
                    traveller = Traveller.getInstance(firstName, lastName, id);
                } catch (IllegalArgumentException e) {
                    System.out.println("\n  " + e.getMessage() + "\n");
                    continue;
                }
                List<Ticket> tickets = traveller.getTickets();
                System.out.println("To view current trips [0], to view past trips [1]: ");
                String tripChoice = scanner.nextLine().trim();
                if (tripChoice.equals("0")) {
                    continue;
                } else if (tripChoice.equals("1")) {
                    System.out.println("\nNo past trips found.\n");
                } else {
                    System.out.println("\n  Invalid choice.\n");
                    continue;
                }

                if (tickets == null || tickets.isEmpty()) {
                    System.out.println("\n  No trips found for " + firstName + " " + lastName + ".\n");
                } else {
                    System.out.println("\n════════════ YOUR TRIPS ════════════\n");
                    for (Ticket ticket : tickets) {
                        System.out.println(ticket.toString());
                        System.out.println("─────────────────────────────────────");
                    }
                }
            } else if (choice.equalsIgnoreCase("q")) {
                System.out.println("\nThank you for using the Rail System Booking System!");
                running = false;
            } else {
                System.out.println("\n  Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }
}
