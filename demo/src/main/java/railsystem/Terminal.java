package railsystem;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.List;
import java.util.Comparator;
import java.util.Scanner;

final class Terminal {
    private static Terminal instance;
    private Loader loader;
    private ConnectionRepository connectionRepo;

    private Terminal() {
        this.loader = new Loader();
        this.connectionRepo = null;
    }

    public static Terminal getInstance() {
        if (instance == null) {
            instance = new Terminal();
        }
        return instance;
    }

    public Loader getLoader() {
        return loader;
    }

    public ConnectionRepository getConnectionRepo() {
        return connectionRepo;
    }

    public void setConnectionRepo(ConnectionRepository connectionRepo) {
        this.connectionRepo = connectionRepo;
    }

    public List<Trip> createSearch(List<String> args) {
        SearchCriteria criteria = new SearchCriteria();

        if (args.size() > 0 && !args.get(0).isEmpty()) {
            criteria.setDepartureCity(args.get(0));
        }
        if (args.size() > 1 && !args.get(1).isEmpty()) {
            criteria.setEarliestDeparture(LocalTime.parse(args.get(1)));
        }
        if (args.size() > 2 && !args.get(2).isEmpty()) {
            criteria.setArrivalCity(args.get(2));
        }
        if (args.size() > 3 && !args.get(3).isEmpty()) {
            criteria.setNextDay(Boolean.parseBoolean(args.get(3)));
        }
        if (args.size() > 4 && !args.get(4).isEmpty()) {
            criteria.setLatestArrival(LocalTime.parse(args.get(4)));
        }
        if (args.size() > 5 && !args.get(5).isEmpty()) {
            String[] days = args.get(5).split(",");
            Set<DayOfWeek> travelDays = Arrays.stream(days)
                    .map(String::toUpperCase)
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());
            criteria.setTravelDays(travelDays);
        }
        if (args.size() > 6 && !args.get(6).isEmpty()) {
            criteria.setPreferredTrain(args.get(6));
        }
        if (args.size() > 7 && !args.get(7).isEmpty()) {
            criteria.setFirstClassRate(new BigDecimal(args.get(7)));
        }
        if (args.size() > 8 && !args.get(8).isEmpty()) {
            criteria.setSecondClassRate(new BigDecimal(args.get(8)));
        }

        List<Trip> trips = searchForConnections(criteria);
        return trips;
    }

    public Booking createBooking(Trip trip, String[] names, String classRate) {
        List<Ticket> tickets;
        for (String name : names) {
            String[] fullName = name.split(" ");
            Traveller traveller = new Traveller(fullName[0], fullName[1], 0);
            tickets.add(new Ticket(trip, traveller, classRate));
        }
        Booking booking = new Booking(null);
        return booking;
    }

    public enum SortOption {
        DURATION("Duration (Fastest first)"),
        PRICE_FIRST_CLASS("Price - First Class (Cheapest first)"),
        PRICE_SECOND_CLASS("Price - Second Class (Cheapest first)"),
        DEPARTURE_TIME("Departure Time (Earliest first)"),
        ARRIVAL_TIME("Arrival Time (Earliest first)"),
        TRANSFERS("Number of Transfers (Fewest first)");

        private final String description;

        SortOption(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void sortTrips(List<Trip> trips, SortOption sortBy) {
        switch (sortBy) {
            case DURATION:
                trips.sort(Comparator.comparing(Trip::getTotalDuration));
                break;
            case PRICE_FIRST_CLASS:
                trips.sort(Comparator.comparing(Trip::getTotalFirstClassRate));
                break;
            case PRICE_SECOND_CLASS:
                trips.sort(Comparator.comparing(Trip::getTotalSecondClassRate));
                break;
            case DEPARTURE_TIME:
                trips.sort((t1, t2) -> {
                    LocalTime time1 = t1.getConnections().get(0).getDepartureStop().getScheduledStop();
                    LocalTime time2 = t2.getConnections().get(0).getDepartureStop().getScheduledStop();
                    return time1.compareTo(time2);
                });
                break;
            case ARRIVAL_TIME:
                trips.sort((t1, t2) -> {
                    Connection last1 = t1.getConnections().get(t1.getConnections().size() - 1);
                    Connection last2 = t2.getConnections().get(t2.getConnections().size() - 1);
                    int dayCompare = Boolean.compare(last1.getArrivalStop().isNextDay(),
                            last2.getArrivalStop().isNextDay());
                    if (dayCompare != 0)
                        return dayCompare;
                    return last1.getArrivalStop().getScheduledStop()
                            .compareTo(last2.getArrivalStop().getScheduledStop());
                });
                break;
            case TRANSFERS:
                trips.sort(Comparator.comparing((Trip t) -> t.getConnections().size())
                        .thenComparing(Trip::getTotalDuration));
                break;
        }
    }

    public void displayTrips(List<Trip> trips, SortOption currentSort) {
        // Display results summary
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      SEARCH RESULTS SUMMARY                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        if (trips.isEmpty()) {
            System.out.println("\n  No trips found matching your criteria.");
        } else {
            // Count direct vs transfer trips
            long directTrips = trips.stream()
                    .filter(t -> t.getConnections().size() == 1)
                    .count();

            System.out.println("\n  Total trips found: " + trips.size());
            System.out.println("  ├─ Direct connections: " + directTrips);
            System.out.println("  └─ With transfers: " + (trips.size() - directTrips));

            System.out.println("\n  Currently sorted by: " + currentSort.getDescription());

            // Show key statistics
            if (!trips.isEmpty()) {
                Trip fastest = trips.stream()
                        .min(Comparator.comparing(Trip::getTotalDuration))
                        .get();
                Trip cheapestFirst = trips.stream()
                        .min(Comparator.comparing(Trip::getTotalFirstClassRate))
                        .get();
                Trip cheapestSecond = trips.stream()
                        .min(Comparator.comparing(Trip::getTotalSecondClassRate))
                        .get();

                System.out.println("\n   Quick Stats:");
                System.out.println("     Fastest trip: " + formatDuration(fastest.getTotalDuration()));
                System.out.println("     Cheapest 1st class: €" + cheapestFirst.getTotalFirstClassRate());
                System.out.println("     Cheapest 2nd class: €" + cheapestSecond.getTotalSecondClassRate());
            }

            System.out.println("\n──────────────────────────────────────────────────────────────────");

            // Display all trips
            for (int i = 0; i < trips.size(); i++) {
                System.out.println("\n【 OPTION " + (i + 1) + " 】");
                System.out.println(trips.get(i));
            }
        }
    }

    public List<Trip> searchForConnections(SearchCriteria criteria) {
        if (connectionRepo == null) {
            throw new IllegalStateException("Connection repository is not set.");
        }

        System.out.println("\nSearching for connections...\n");

        List<Connection> directConnections = connectionRepo.search(criteria);

        List<Trip> trips = directConnections.stream()
                .map(conn -> new Trip(List.of(conn)))
                .collect(Collectors.toList());

        if (!trips.isEmpty()) {
            System.out.printf("Found %d direct connection(s).%n%n", trips.size());
            sortTrips(trips, SortOption.DURATION);
            return trips;
        }

        System.out.println("No direct connections found.");

        if (criteria.getDepartureCity() == null || criteria.getArrivalCity() == null) {
            return trips;
        }

        System.out.println("Searching for connections with transfers...\n");

        List<Connection> firstLegs = connectionRepo.search(
                SearchCriteria.builder()
                        .departureCity(criteria.getDepartureCity())
                        .earliestDeparture(criteria.getEarliestDeparture())
                        .latestArrival(criteria.getLatestArrival())
                        .nextDay(criteria.getNextDay())
                        .preferredTrain(criteria.getPreferredTrain())
                        .travelDays(criteria.getTravelDays())
                        .firstClassRate(criteria.getFirstClassRate())
                        .secondClassRate(criteria.getSecondClassRate())
                        .build());

        for (Connection firstLeg : firstLegs) {
            String transferCity = firstLeg.getArrivalStop().getCity().getName();

            if (transferCity.equals(firstLeg.getDepartureStop().getCity().getName()))
                continue;

            SearchCriteria secondLegCriteria = new SearchCriteria(criteria);
            secondLegCriteria.setDepartureCity(transferCity);

            adjustTravelDaysForNextDay(criteria, secondLegCriteria, firstLeg.getArrivalStop().isNextDay());
            secondLegCriteria.setEarliestDeparture(firstLeg.getArrivalStop().getScheduledStop().plusMinutes(20));

            List<Connection> secondLegs = connectionRepo.search(secondLegCriteria);

            for (Connection secondLeg : secondLegs) {
                String secondArrivalCity = secondLeg.getArrivalStop().getCity().getName();

                if (secondArrivalCity.equals(criteria.getArrivalCity())) {
                    trips.add(new Trip(List.of(firstLeg, secondLeg)));
                    continue;
                }

                SearchCriteria thirdLegCriteria = new SearchCriteria(criteria);
                thirdLegCriteria.setDepartureCity(secondArrivalCity);

                int daysToAdd = calculateDaysToAdd(firstLeg, secondLeg);
                adjustTravelDaysForNextDay(criteria, thirdLegCriteria, daysToAdd > 0);
                thirdLegCriteria.setEarliestDeparture(secondLeg.getArrivalStop().getScheduledStop().plusMinutes(20));

                List<Connection> thirdLegs = connectionRepo.search(thirdLegCriteria);

                thirdLegs.stream()
                        .filter(thirdLeg -> thirdLeg.getArrivalStop().getCity().getName()
                                .equals(criteria.getArrivalCity()))
                        .forEach(thirdLeg -> trips.add(new Trip(List.of(firstLeg, secondLeg, thirdLeg))));
            }
        }

        sortTrips(trips, SortOption.DURATION);
        return trips;
    }

    private void adjustTravelDaysForNextDay(SearchCriteria original,
            SearchCriteria target,
            boolean addDay) {
        if (addDay && original.getTravelDays() != null) {
            target.setTravelDays(
                    original.getTravelDays().stream()
                            .map(day -> day.plus(1))
                            .collect(Collectors.toSet()));
        }
    }

    private int calculateDaysToAdd(Connection firstLeg, Connection secondLeg) {
        int daysToAdd = 0;
        if (firstLeg.getArrivalStop().isNextDay())
            daysToAdd++;
        if (secondLeg.getArrivalStop().isNextDay())
            daysToAdd++;
        return daysToAdd;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 24) {
            long days = hours / 24;
            hours = hours % 24;
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
