package railsystem;

import java.util.List;

/**
 * Handles search and sorting operations for trips.
 */
public class SearchService {
    private final Terminal terminal;
    private final MenuSystem menuSystem;

    public SearchService(Terminal terminal, MenuSystem menuSystem) {
        this.terminal = terminal;
        this.menuSystem = menuSystem;
    }

    /**
     * Performs a search based on user input and handles the sorting/display loop.
     */
    public void performSearch() {
        try {
            List<String> searchArgs = menuSystem.getSearchCriteria();
            List<Trip> searchResult = terminal.createSearch(searchArgs);

            if (searchResult == null) {
                System.out.println("\n  [WARN] Search could not be completed. Please try again.\n");
                return;
            }

            if (searchResult.isEmpty()) {
                System.out.println("\n  [INFO] No trips found matching your search criteria.\n");
                return;
            }

            terminal.displayTrips(searchResult, Terminal.SortOption.DURATION);
            handleSearchResults(searchResult);

        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred during search: " + e.getMessage() + "\n");
        }
    }

    /**
     * Handles the sorting menu and actions after a search.
     */
    private void handleSearchResults(List<Trip> searchResult) {
        boolean continueViewing = true;
        while (continueViewing) {
            try {
                String sortChoice = menuSystem.displaySortingMenu();

                Terminal.SortOption sortOption = menuSystem.getSortOption(sortChoice);

                if (sortChoice.equals("7")) {
                    handleBookingCreation(searchResult);
                } else if (sortChoice.equals("0")) {
                    continueViewing = false;
                    menuSystem.displayGoodbye();
                } else if (sortOption != null) {
                    terminal.sortTrips(searchResult, sortOption);
                    terminal.displayTrips(searchResult, sortOption);
                } else {
                    menuSystem.displayInvalidChoice();
                }
            } catch (Exception e) {
                System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
                System.out.println("  Please try again.\n");
            }
        }
    }

    /**
     * Handles the creation of a booking from search results.
     */
    private void handleBookingCreation(List<Trip> searchResult) {
        try {
            MenuSystem.BookingRequest request = menuSystem.getBookingRequest(searchResult);

            if (request == null) {
                System.out.println("\n  [WARN] Booking was not completed.\n");
                return;
            }

            Booking booking = terminal.createBooking(
                    request.getTrip(),
                    request.getPassengers(),
                    request.getClassRate());

            if (booking != null) {
                System.out.println("\n [INFO] Booking successful!");
                System.out.println(booking);
            } else {
                System.out.println("\n  [WARN] Booking could not be created. Please try again.\n");
            }

        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred while creating the booking: " + e.getMessage() + "\n");
        }
    }
}
