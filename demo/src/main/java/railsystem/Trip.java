package railsystem;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;


public final class Trip {
    private static int idCount = 0;

    private final List<Connection> connections;
    private final BigDecimal totalFirstClassRate;
    private final BigDecimal totalSecondClassRate;
    private final Duration totalDuration;
    private final List<Duration> transferTimes;
    private final int id;

    public Trip(List<Connection> connections) {
        if (connections == null || connections.isEmpty()) {
            throw new IllegalArgumentException("Connections list cannot be null or empty");
        }
        this.connections = List.copyOf(connections); // Immutable copy
        this.totalFirstClassRate = connections.stream()
                .map(c -> c.getTicketRates().getFirstClass())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalSecondClassRate = connections.stream()
                .map(c -> c.getTicketRates().getSecondClass())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalDuration = calculateTotalDuration();
        this.transferTimes = Collections.unmodifiableList(calculateTransferTimes());
        this.id = getNextId();
    }

    private int getNextId() {
        idCount += 1;
        return idCount;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public BigDecimal getTotalFirstClassRate() {
        return totalFirstClassRate;
    }

    public BigDecimal getTotalSecondClassRate() {
        return totalSecondClassRate;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public List<Duration> getTransferTimes() {
        return transferTimes;
    }

    private Duration calculateTotalDuration() {
        if (connections.size() == 1) {
            Connection c = connections.get(0);
            Duration duration = Duration.between(
                    c.getDepartureStop().getScheduledStop(),
                    c.getArrivalStop().getScheduledStop());

            if (c.getArrivalStop().isNextDay() || duration.isNegative()) {
                duration = duration.plusDays(1);
            }
            return duration;
        }

        Connection first = connections.get(0);
        Connection last = connections.get(connections.size() - 1);

        Duration totalDuration = Duration.between(
                first.getDepartureStop().getScheduledStop(),
                last.getArrivalStop().getScheduledStop());

        int daysCrossed = 0;

        for (Connection c : connections) {
            if (c.getArrivalStop().isNextDay()) {
                daysCrossed++;
            }
        }

        for (int i = 0; i < connections.size() - 1; i++) {
            Connection current = connections.get(i);
            Connection next = connections.get(i + 1);

            LocalTime currentArrival = current.getArrivalStop().getScheduledStop();
            LocalTime nextDeparture = next.getDepartureStop().getScheduledStop();

            if (nextDeparture.isBefore(currentArrival)) {
                daysCrossed++;
            }
        }

        if (totalDuration.plusDays(daysCrossed).isNegative()) {
            daysCrossed++;
        }

        return totalDuration.plusDays(daysCrossed);
    }

    private List<Duration> calculateTransferTimes() {
        List<Duration> transfers = new ArrayList<>();
        for (int i = 0; i < connections.size() - 1; i++) {
            Connection current = connections.get(i);
            Connection next = connections.get(i + 1);

            Duration transfer = Duration.between(
                    current.getArrivalStop().getScheduledStop(),
                    next.getDepartureStop().getScheduledStop());

            // Adjust for next-day departure
            if (transfer.isNegative() || next.getDepartureStop().isNextDay()) {
                transfer = transfer.plusDays(1);
            }

            transfers.add(transfer);
        }
        return transfers;
    }

    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append("d ");
        if (hours > 0 || days > 0)
            sb.append(hours).append("h ");
        sb.append(minutes).append("m");

        return sb.toString();
    }

    private String formatOperatingDays(Set<DayOfWeek> days) {
        if (days.isEmpty()) {
            return "No service";
        }

        if (days.size() == 7) {
            return "Daily";
        }

        Set<DayOfWeek> weekdays = Set.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        if (days.equals(weekdays)) {
            return "Weekdays (Mon-Fri)";
        }

        Set<DayOfWeek> weekends = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        if (days.equals(weekends)) {
            return "Weekends (Sat-Sun)";
        }

        List<DayOfWeek> sortedDays = days.stream()
                .sorted()
                .collect(Collectors.toList());

        return sortedDays.stream()
                .map(this::abbreviateDay)
                .collect(Collectors.joining(", "));
    }

    private String abbreviateDay(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return "Mon";
            case TUESDAY:
                return "Tue";
            case WEDNESDAY:
                return "Wed";
            case THURSDAY:
                return "Thu";
            case FRIDAY:
                return "Fri";
            case SATURDAY:
                return "Sat";
            case SUNDAY:
                return "Sun";
            default:
                return day.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n════════════════════════════════════════════════════════════════\n");
        sb.append("TRIP - SUMMARY (").append(connections.size()).append(" Connection");
        if (connections.size() > 1)
            sb.append("s");
        sb.append(")\n────────────────────────────────────────────────────────────────\n");

        Connection first = connections.get(0);
        Connection last = connections.get(connections.size() - 1);

        sb.append("  Journey: ").append(first.getDepartureStop().getCity().getName())
                .append(" → ").append(last.getArrivalStop().getCity().getName()).append("\n");
        sb.append("  Departure: ").append(first.getDepartureStop().getScheduledStop()).append("\n");
        sb.append("  Arrival: ").append(last.getArrivalStop().getScheduledStop());
        if (last.getArrivalStop().isNextDay()) {
            int totalDays = (int) connections.stream().filter(c -> c.getArrivalStop().isNextDay()).count();
            sb.append(" (+").append(totalDays).append("d)");
        }
        sb.append("\n  Total Duration: ").append(formatDuration(totalDuration)).append("\n");
        sb.append("────────────────────────────────────────────────────────────────\n");
        sb.append("CONNECTION DETAILS:\n\n");

        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            sb.append("  [Leg ").append(i + 1).append("] ")
                    .append(c.getDepartureStop().getCity().getName()).append(" → ")
                    .append(c.getArrivalStop().getCity().getName()).append("\n")
                    .append("    Route: ").append(c.getRouteId())
                    .append(" | Train: ").append(c.getTrain().getTrainType()).append("\n")
                    .append("    Operating Days: ").append(formatOperatingDays(c.getSchedule().getOperatingDays()))
                    .append("\n")
                    .append("    Depart: ").append(c.getDepartureStop().getScheduledStop())
                    .append(" → Arrive: ").append(c.getArrivalStop().getScheduledStop());
            if (c.getArrivalStop().isNextDay())
                sb.append(" (+1d)");

            Duration legDuration = Duration.between(
                    c.getDepartureStop().getScheduledStop(),
                    c.getArrivalStop().getScheduledStop());
            if (c.getArrivalStop().isNextDay())
                legDuration = legDuration.plusDays(1);
            if (legDuration.isNegative())
                legDuration = legDuration.plusDays(1);

            sb.append("\n    Duration: ").append(formatDuration(legDuration)).append("\n");

            if (i < transferTimes.size()) {
                sb.append("    Transfer Time in ")
                        .append(c.getArrivalStop().getCity().getName())
                        .append(": ").append(formatDuration(transferTimes.get(i))).append("\n");
            }

            sb.append("\n");
        }

        sb.append("────────────────────────────────────────────────────────────────\n")
                .append("FARE INFORMATION:\n")
                .append("  First Class:  €").append(totalFirstClassRate).append("\n")
                .append("  Second Class: €").append(totalSecondClassRate).append("\n")
                .append("════════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }
}
