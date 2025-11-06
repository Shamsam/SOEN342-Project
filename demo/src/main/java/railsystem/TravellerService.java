package railsystem;

import java.util.List;

/**
 * Handles traveller-related operations such as viewing trips.
 */
public class TravellerService {
    private final MenuSystem menuSystem;

    public TravellerService(MenuSystem menuSystem) {
        this.menuSystem = menuSystem;
    }

    /**
     * Handles viewing a traveller's trips (current or past).
     */
    public void viewTravellerTrips() {
        try {
            MenuSystem.TravellerInfo info = menuSystem.getTravellerInfo();

            Traveller traveller;
            try {
                traveller = Traveller.getInstance(info.getFirstName(), info.getLastName(), info.getId());
            } catch (IllegalArgumentException e) {
                System.out.println("\n  [WARN] " + e.getMessage() + "\n");
                return;
            } catch (Exception e) {
                System.out.println("\n  [ERROR] Error retrieving traveller information: " + e.getMessage() + "\n");
                return;
            }

            String tripChoice = menuSystem.getTripViewChoice();

            if (tripChoice.equals("0")) {
                // View current trips
                try {
                    List<Ticket> tickets = traveller.getTickets();
                    menuSystem.displayTickets(tickets, info.getFirstName(), info.getLastName());
                } catch (Exception e) {
                    System.out.println("\n  [ERROR] Error retrieving tickets: " + e.getMessage() + "\n");
                }
            } else if (tripChoice.equals("1")) {
                System.out.println("\n  [INFO] No past trips found.\n");
            } else {
                menuSystem.displayInvalidChoice();
            }
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An unexpected error occurred: " + e.getMessage() + "\n");
        }
    }
}
