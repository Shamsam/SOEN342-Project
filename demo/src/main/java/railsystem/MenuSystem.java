package railsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Handles all menu displays and user interactions for the Rail System.
 */
public class MenuSystem {
    private final Scanner scanner;
    private final Terminal terminal;

    // Validation patterns
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern POSITIVE_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    public MenuSystem(Scanner scanner, Terminal terminal) {
        this.scanner = scanner;
        this.terminal = terminal;
    }

    private boolean isValidCity(String city) {
        return city.isEmpty() || City.exists(city);
    }

    private boolean isValidTrainType(String trainType) {
        return trainType.isEmpty() || Train.exists(trainType);
    }

    /**
     * Validates if a string represents a valid time in HH:MM format.
     */
    private boolean isValidTime(String time) {
        return time.isEmpty() || TIME_PATTERN.matcher(time).matches();
    }

    /**
     * Validates if a string represents a valid boolean value.
     */
    private boolean isValidBoolean(String value) {
        return value.isEmpty() || BOOLEAN_PATTERN.matcher(value).matches();
    }

    /**
     * Validates if a string represents a valid positive number.
     */
    private boolean isValidPositiveNumber(String value) {
        return value.isEmpty() || POSITIVE_NUMBER_PATTERN.matcher(value).matches();
    }

    /**
     * Safely reads an integer with error handling.
     */
    private Integer readIntegerSafely(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    return null;
                }

                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println("  [WARN] Please enter a number between " + min + " and " + max + ".");
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println("  [WARN] Invalid input. Please enter a valid number.");
            }
        }
    }

    /**
     * Validates user input with retry capability.
     */
    private String getValidatedInput(String prompt, java.util.function.Predicate<String> validator,
            String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (validator.test(input)) {
                return input;
            }

            if (!input.isEmpty()) {
                System.out.println("  [WARN] " + errorMessage);
            }
        }
    }

    public void displayWelcome() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           Welcome to the Rail System Booking System!           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    public String displayMainMenu() {
        System.out.println("Please select an option:");
        System.out.println("[0] Display all connections");
        System.out.println("[1] Start a search");
        System.out.println("[2] View your trips");
        System.out.println("[q] Quit");
        System.out.print("Enter your choice: ");
        return scanner.nextLine().trim();
    }

    public void displayAllConnections() {
        System.out.println("\n════════════ ALL CONNECTIONS ════════════\n");
        for (Connection conn : terminal.getConnectionRepo().getConnections()) {
            System.out.println(conn.toString());
            System.out.println("─────────────────────────────────────────");
        }
    }

    public List<String> getSearchCriteria() {
        List<String> searchArgs = new ArrayList<>();

        System.out.println("\n════════════ SEARCH CRITERIA ════════════\n");
        System.out.println("Press Enter to skip any field.\n");

        // Departure city
        String departureCity = getValidatedInput(
                "Departure city: ",
                this::isValidCity,
                "Invalid city name. Please try again.");
        searchArgs.add(departureCity);

        // Earliest departure time with validation
        String earliestDeparture = getValidatedInput(
                "Earliest departure time (HH:MM): ",
                this::isValidTime,
                "Invalid time format. Please use HH:MM format (e.g., 09:30).");
        searchArgs.add(earliestDeparture);

        // Arrival city
        String arrivalCity = getValidatedInput(
                "Arrival city: ",
                this::isValidCity,
                "Invalid city name. Please try again.");
        searchArgs.add(arrivalCity);

        // Next day arrival with validation
        String nextDayArrival = getValidatedInput(
                "Next day arrival (true/false): ",
                this::isValidBoolean,
                "Please enter 'true' or 'false'.");
        searchArgs.add(nextDayArrival);

        // Latest arrival time with validation
        String latestArrival = getValidatedInput(
                "Latest arrival time (HH:MM): ",
                this::isValidTime,
                "Invalid time format. Please use HH:MM format (e.g., 18:45).");
        searchArgs.add(latestArrival);

        // Travel days
        System.out.print("Travel days (e.g., MONDAY,TUESDAY): ");
        searchArgs.add(scanner.nextLine().trim());

        // Preferred train type
        String preferredTrainType = getValidatedInput(
                "Preferred train type: ",
                this::isValidTrainType,
                "Invalid train type. Please try again.");
        searchArgs.add(preferredTrainType);

        // Max first class rate with validation
        String firstClassRate = getValidatedInput(
                "Max first class rate (€): ",
                this::isValidPositiveNumber,
                "Please enter a valid positive number.");
        searchArgs.add(firstClassRate);

        // Max second class rate with validation
        String secondClassRate = getValidatedInput(
                "Max second class rate (€): ",
                this::isValidPositiveNumber,
                "Please enter a valid positive number.");
        searchArgs.add(secondClassRate);

        return searchArgs;
    }

    public String displaySortingMenu() {
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
        return scanner.nextLine().trim();
    }

    public Terminal.SortOption getSortOption(String choice) {
        switch (choice) {
            case "1":
                return Terminal.SortOption.DURATION;
            case "2":
                return Terminal.SortOption.PRICE_FIRST_CLASS;
            case "3":
                return Terminal.SortOption.PRICE_SECOND_CLASS;
            case "4":
                return Terminal.SortOption.DEPARTURE_TIME;
            case "5":
                return Terminal.SortOption.ARRIVAL_TIME;
            case "6":
                return Terminal.SortOption.TRANSFERS;
            default:
                return null;
        }
    }

    public BookingRequest getBookingRequest(List<Trip> searchResult) {
        try {
            // Get trip option with validation
            Integer option = readIntegerSafely(
                    "Input trip option you'd like to book (1-" + searchResult.size() + "): ",
                    1,
                    searchResult.size());

            if (option == null) {
                System.out.println("  [WARN] Booking cancelled.");
                return null;
            }

            // Get passenger information
            ArrayList<String> nameList = new ArrayList<>();
            System.out.println("\nEnter passenger details (press Enter on empty line to finish):");
            System.out.println("Format: FirstName, LastName, ID");
            System.out.println("Example: John, Smith, 12345\n");

            int passengerCount = 0;
            while (true) {
                System.out.print("Passenger " + (passengerCount + 1) + ": ");
                String name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    break;
                }
                nameList.add(name);
                passengerCount++;
            }

            // Validate at least one passenger was entered
            if (nameList.isEmpty()) {
                System.out.println("  [WARN] At least one passenger is required for booking.");
                return null;
            }

            // Display added passengers
            System.out.println("\n✓ Added " + passengerCount + " passenger(s):");
            for (int i = 0; i < nameList.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + nameList.get(i));
            }

            // Get class rate with validation
            System.out.println("\nSelect ticket class:");
            System.out.println("[1] First Class");
            System.out.println("[2] Second Class");
            String classChoice = getValidatedInput(
                    "Enter your choice (1/2): ",
                    value -> value.equals("1") || value.equals("2"),
                    "Please enter '1' for First Class or '2' for Second Class.");

            if (classChoice.isEmpty()) {
                System.out.println("  [WARN] Class selection is required for booking.");
                return null;
            }

            String classRate = classChoice.equals("1") ? "First Class" : "Second Class";

            return new BookingRequest(searchResult.get(option - 1), nameList, classRate);

        } catch (Exception e) {
            System.out.println("  [WARN] An error occurred while processing your booking request: " + e.getMessage());
            scanner.nextLine(); // Clear any remaining input
            return null;
        }
    }

    public String getTravellerInfo() {
        System.out.println("\nEnter your id to view your trips.");

        // Validate ID is not empty
        String id;
        while (true) {
            System.out.print("ID: ");
            id = scanner.nextLine().trim();
            if (!id.isEmpty()) {
                break;
            }
            System.out.println("  [WARN] ID cannot be empty.");
        }

        return id;
    }

    public String getTripViewChoice() {
        System.out.println("\nSelect trip view:");
        System.out.println("[0] Current trips (booked in this session)");
        System.out.println("[1] Past trips (from previous sessions)");
        System.out.print("Enter your choice: ");
        return scanner.nextLine().trim();
    }

    public void displayTickets(List<Ticket> tickets, String firstName, String lastName) {
        if (tickets == null || tickets.isEmpty()) {
            System.out.println("\n  No trips found.\n");
        } else {
            for (Ticket ticket : tickets) {
                System.out.println(ticket.toString());
                System.out.println("─────────────────────────────────────");
            }
        }
    }

    public void displayGoodbye() {
        System.out.println("\nThank you for using the Rail System Booking System!");
    }

    public void displayInvalidChoice() {
        System.out.println("\n  [WARN] Invalid choice. Please try again.");
    }

    public void displayError(String message) {
        System.out.println("\n  [ERROR] Error: " + message + "\n");
    }

    public void displayWarning(String message) {
        System.out.println("\n  [WARN] " + message + "\n");
    }

    /**
     * Simple data class to hold booking request information.
     */
    public static class BookingRequest {
        private final Trip trip;
        private final ArrayList<String> passengers;
        private final String classRate;

        public BookingRequest(Trip trip, ArrayList<String> passengers, String classRate) {
            this.trip = trip;
            this.passengers = passengers;
            this.classRate = classRate;
        }

        public Trip getTrip() {
            return trip;
        }

        public ArrayList<String> getPassengers() {
            return passengers;
        }

        public String getClassRate() {
            return classRate;
        }
    }

    /**
     * Simple data class to hold traveller information.
     */
    public static class TravellerInfo {
        private final String firstName;
        private final String lastName;
        private final String id;

        public TravellerInfo(String firstName, String lastName, String id) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getId() {
            return id;
        }
    }
}
