package railsystem;

import java.util.List;

public class TravellerService {
    private final MenuSystem menuSystem;
    private final Terminal terminal;

    public TravellerService(MenuSystem menuSystem, Terminal terminal) {
        this.menuSystem = menuSystem;
        this.terminal = terminal;
    }

    public void viewTravellerTrips() {
        try {
            String id = menuSystem.getTravellerInfo();

            Traveller traveller;
            try {
                traveller = Traveller.getInstance(id);
            } catch (IllegalArgumentException e) {
                System.out.println("\n  [WARN] " + e.getMessage() + "\n");
                return;
            } catch (Exception e) {
                System.out.println("\n  [ERROR] Error retrieving traveller information: " + e.getMessage() + "\n");
                return;
            }

            String tripChoice = menuSystem.getTripViewChoice();

            if (tripChoice.equals("0")) {
                // View current trips (in-memory from this session)
                try {
                    List<Ticket> tickets = traveller.getTickets();
                    if (tickets.isEmpty()) {
                        System.out.println("\n  No current trips found for " + traveller.getFirstName() + " "
                                + traveller.getLastName() + ".\n");
                    } else {
                        System.out.println("\n════════════ CURRENT TRIPS (This Session) ════════════\n");
                        menuSystem.displayTickets(tickets, traveller.getFirstName(), traveller.getLastName());
                    }
                } catch (Exception e) {
                    System.out.println("\n  [ERROR] Error retrieving tickets: " + e.getMessage() + "\n");
                }
            } else if (tripChoice.equals("1")) {
                // View past trips (from database)
                try {
                    DBManager dbManager = terminal.getDbManager();
                    if (dbManager != null) {
                        List<Ticket> pastTickets = dbManager.getTravellerBookings(traveller.getId());
                        if (pastTickets.isEmpty()) {
                            System.out.println("\n  No past trips found for " + traveller.getFirstName() + " "
                                    + traveller.getLastName() + ".\n");
                        } else {
                            System.out.println("\n════════════ PAST TRIPS (From Previous Sessions) ════════════\n");
                            menuSystem.displayTickets(pastTickets, traveller.getFirstName(), traveller.getLastName());
                        }
                    } else {
                        System.out.println("\n  [WARN] Database not available. Cannot retrieve past trips.\n");
                    }
                } catch (Exception e) {
                    System.out.println("\n  [ERROR] Error retrieving past trips: " + e.getMessage() + "\n");
                }
            } else {
                menuSystem.displayInvalidChoice();
            }
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An unexpected error occurred: " + e.getMessage() + "\n");
        }
    }
}
