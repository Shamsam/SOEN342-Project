package railsystem;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.List;
import java.util.Comparator;

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

    public Booking createBooking(Trip trip, ArrayList<String> names, String classRate) {
        List<Ticket> tickets = new ArrayList<Ticket>();
        for (String name : names) {
            String[] fullName = name.split(" ");
            System.out.println(fullName.length);
            System.out.println(fullName[0]);
            Traveller traveller = Traveller.getInstance(fullName[0], fullName[1], Traveller.incrementIdCount());
            tickets.add(new Ticket(trip, traveller, classRate));
        }
        Booking booking = new Booking(tickets);
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
                        .preferredTrain(criteria.getPreferredTrain())
                        .travelDays(criteria.getTravelDays())
                        .firstClassRate(criteria.getFirstClassRate())
                        .secondClassRate(criteria.getSecondClassRate())
                        .build());

        for (Connection firstLeg : firstLegs) {
            String transferCity = firstLeg.getArrivalStop().getCity().getName();

            if (transferCity.equals(firstLeg.getDepartureStop().getCity().getName())) // Prevent loops
                continue;

            Set<DayOfWeek> secondLegValidDays = calculateValidDaysForNextLeg(
                    firstLeg.getSchedule().getOperatingDays(),
                    firstLeg.getDepartureStop().getScheduledStop(),
                    firstLeg.getArrivalStop().getScheduledStop(),
                    firstLeg.getArrivalStop().isNextDay());

            SearchCriteria secondLegCriteria = SearchCriteria.builder()
                    .departureCity(transferCity)
                    .preferredTrain(criteria.getPreferredTrain())
                    .travelDays(secondLegValidDays)
                    .firstClassRate(criteria.getFirstClassRate())
                    .secondClassRate(criteria.getSecondClassRate())
                    .build();

            List<Connection> secondLegs = connectionRepo.search(secondLegCriteria);

            for (Connection secondLeg : secondLegs) {
                if (!areConnectionDaysCompatible(firstLeg, secondLeg)) {
                    continue;
                }

                String secondArrivalCity = secondLeg.getArrivalStop().getCity().getName();

                if (secondArrivalCity.equals(criteria.getArrivalCity())) {
                    if (criteria.getLatestArrival() != null && secondLeg.getArrivalStop().getScheduledStop()
                            .compareTo(criteria.getLatestArrival()) > 0) {
                        continue;
                    }
                    trips.add(new Trip(List.of(firstLeg, secondLeg)));
                    continue;
                }

                Set<DayOfWeek> thirdLegValidDays = calculateValidDaysForNextLeg(
                        secondLeg.getSchedule().getOperatingDays(),
                        secondLeg.getDepartureStop().getScheduledStop(),
                        secondLeg.getArrivalStop().getScheduledStop(),
                        secondLeg.getArrivalStop().isNextDay());

                SearchCriteria thirdLegCriteria = SearchCriteria.builder()
                        .departureCity(secondArrivalCity)
                        .arrivalCity(criteria.getArrivalCity())
                        .preferredTrain(criteria.getPreferredTrain())
                        .travelDays(thirdLegValidDays)
                        .firstClassRate(criteria.getFirstClassRate())
                        .secondClassRate(criteria.getSecondClassRate())
                        .build();

                List<Connection> thirdLegs = connectionRepo.search(thirdLegCriteria);

                for (Connection thirdLeg : thirdLegs) {
                    if (!areConnectionDaysCompatible(secondLeg, thirdLeg)) {
                        continue;
                    }

                    if (thirdLeg.getArrivalStop().getCity().getName().equals(criteria.getArrivalCity())) {
                        if (criteria.getLatestArrival() != null && thirdLeg.getArrivalStop()
                                .getScheduledStop().compareTo(criteria.getLatestArrival()) > 0) {
                            continue;
                        }
                        trips.add(new Trip(List.of(firstLeg, secondLeg, thirdLeg)));
                    }
                }
            }
        }

        sortTrips(trips, SortOption.DURATION);
        return trips;
    }

    private Set<DayOfWeek> calculateValidDaysForNextLeg(
            Set<DayOfWeek> operationDays,
            LocalTime departureTime,
            LocalTime arrivalTime,
            boolean arrivalIsNextDay) {

        if (!arrivalIsNextDay) {
            return operationDays;
        } else {
            return operationDays.stream()
                    .map(day -> day.plus(1).compareTo(DayOfWeek.SUNDAY) > 0 ? DayOfWeek.MONDAY : day.plus(1))
                    .collect(Collectors.toSet());
        }
    }

    private boolean areConnectionDaysCompatible(Connection previous, Connection next) {
        Set<DayOfWeek> previousOperationDays = previous.getSchedule().getOperatingDays();
        Set<DayOfWeek> nextOperationDays = next.getSchedule().getOperatingDays();

        LocalTime previousArrival = previous.getArrivalStop().getScheduledStop();
        LocalTime nextDeparture = next.getDepartureStop().getScheduledStop();
        boolean previousArrivalNextDay = previous.getArrivalStop().isNextDay();
        boolean nextDepartureNextDay = next.getDepartureStop().isNextDay();

        for (DayOfWeek prevOpDay : previousOperationDays) {
            DayOfWeek actualArrivalDay = prevOpDay;
            if (previousArrivalNextDay) {
                actualArrivalDay = prevOpDay.plus(1);
                if (actualArrivalDay.compareTo(DayOfWeek.SUNDAY) > 0) {
                    actualArrivalDay = DayOfWeek.MONDAY;
                }
            }

            for (DayOfWeek nextOpDay : nextOperationDays) {
                DayOfWeek actualDepartureDay = nextOpDay;
                if (nextDepartureNextDay) {
                    actualDepartureDay = nextOpDay.plus(1);
                    if (actualDepartureDay.compareTo(DayOfWeek.SUNDAY) > 0) {
                        actualDepartureDay = DayOfWeek.MONDAY;
                    }
                }

                if (actualDepartureDay.equals(actualArrivalDay)) {
                    if (nextDeparture.isAfter(previousArrival.plusMinutes(19))) {
                        long minutesDiff = java.time.Duration.between(previousArrival, nextDeparture).toMinutes();
                        if (minutesDiff >= 20 && minutesDiff <= 1440) {
                            return true;
                        }
                    }
                } else {
                    DayOfWeek nextDayAfterArrival = actualArrivalDay.plus(1);
                    if (nextDayAfterArrival.compareTo(DayOfWeek.SUNDAY) > 0) {
                        nextDayAfterArrival = DayOfWeek.MONDAY;
                    }

                    if (actualDepartureDay.equals(nextDayAfterArrival)) {
                        long minutesUntilMidnight = java.time.Duration.between(previousArrival, LocalTime.MAX)
                                .toMinutes() + 1;
                        long minutesAfterMidnight = java.time.Duration.between(LocalTime.MIN, nextDeparture)
                                .toMinutes();
                        long totalMinutes = minutesUntilMidnight + minutesAfterMidnight;

                        if (totalMinutes >= 20 && totalMinutes <= 1440) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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
