package railsystem;

import java.util.Scanner;

/**
 * Main entry point for the Rail System Booking System.
 * This class has been refactored to delegate responsibilities to specialized
 * service classes.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = null;

        try {
            // Initialize database
            String url = "jdbc:sqlite:db/project.db";
            Terminal terminal = Terminal.getInstance();

            DatabaseInitializer dbInitializer = new DatabaseInitializer(url, terminal);

            // Check if database initialization was successful
            boolean dbInitialized = dbInitializer.initializeDatabase();
            if (!dbInitialized) {
                System.out.println("\n[WARN] Warning: Database initialization failed. Some features may be limited.\n");
            }

            // Check if connections were loaded successfully
            boolean connectionsLoaded = dbInitializer.loadConnections();
            if (!connectionsLoaded) {
                System.out.println("\n[ERROR] Critical Error: Unable to load connection data.");
                System.out.println("The application cannot start without connection data.");
                System.out.println("Please ensure the database or CSV file is accessible and try again.\n");
                return;
            }

            // Initialize services
            scanner = new Scanner(System.in);
            MenuSystem menuSystem = new MenuSystem(scanner, terminal);
            SearchService searchService = new SearchService(terminal, menuSystem);
            TravellerService travellerService = new TravellerService(menuSystem);

            // Display welcome message
            menuSystem.displayWelcome();

            // Main application loop
            boolean running = true;
            while (running) {
                try {
                    String choice = menuSystem.displayMainMenu();

                    switch (choice) {
                        case "0":
                            menuSystem.displayAllConnections();
                            break;
                        case "1":
                            searchService.performSearch();
                            break;
                        case "2":
                            travellerService.viewTravellerTrips();
                            break;
                        case "q":
                        case "Q":
                            menuSystem.displayGoodbye();
                            running = false;
                            break;
                        default:
                            menuSystem.displayInvalidChoice();
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("\n[ERROR] An unexpected error occurred: " + e.getMessage());
                    System.out.println("Please try again or contact support if the problem persists.\n");
                }
            }
        } catch (Exception e) {
            System.out.println("\n[ERROR] Critical Error: " + e.getMessage());
            System.out.println("The application must exit.\n");
            e.printStackTrace();
        } finally {
            // Ensure scanner is always closed
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}
