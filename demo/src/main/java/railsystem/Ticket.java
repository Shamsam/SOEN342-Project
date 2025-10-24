package railsystem;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Ticket {
    Trip trip;
    Traveller traveller;
    String classRate;


    public Ticket(Trip trip, Traveller traveller, String classRate) {
        this.trip = trip;
        this.traveller = traveller;
        this.classRate = classRate;
    }

    public BigDecimal getTotalCost() {
        BigDecimal cost;

        if(classRate.equals("First Class")){
            cost = trip.getConnections().stream()
                    .map(c -> c.getTicketRates().getFirstClass())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else if (classRate.equals("Second Class")){
            cost = trip.getConnections().stream()
                    .map(c -> c.getTicketRates().getSecondClass())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        else {
            throw new IllegalArgumentException("Invalid class rate: " + classRate);
        }
        return cost;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════ TICKET ════════════════════════════\n");
        sb.append(trip.toString());
        sb.append("────────────────────────────────────────────────────────────────\n");
        sb.append("Traveller:\n");
        sb.append("- ").append(traveller.getLastName()).append(", ").append(traveller.getLastName()).append("\n");
        sb.append("Class Rate: ").append(classRate).append("\n");
        sb.append("Total Cost: $").append(getTotalCost()).append("\n");
        sb.append("════════════════════════════════════════════════════════════════\n");
        return sb.toString();
}
}
