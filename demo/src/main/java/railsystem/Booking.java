package railsystem;

import java.util.List;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class Booking {
    List<Ticket> tickets;

    public Booking(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public BigDecimal getTotalBookingCost() {
        return tickets.stream()
                .map(Ticket::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═════════════════════ Booking Summary ══════════════════\n");
        for (Ticket ticket : tickets) {
            sb.append(ticket.toString());
        }
        sb.append("Total Booking Cost: €").append(getTotalBookingCost()).append("\n");
        sb.append("════════════════════════════════════════════════════════════════\n");
        return sb.toString();
    }
}
