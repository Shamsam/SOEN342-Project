package railsystem;
import java.util.List;

import lombok.Data;

@Data
public class Ticket {
    Trip trip;
    List<Traveller> travellers;

    public Ticket(Trip trip, List<Traveller> travellers) {
        this.trip = trip;
        this.travellers = travellers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════ TICKET ════════════════════════\n");
        sb.append(trip.toString());
        sb.append("──────────────────────────────────────────────────────────\n");
        sb.append("Travellers:\n");
        for (Traveller traveller : travellers) {
            sb.append("- ").append(traveller.getLastName()).append(", ").append(traveller.getLastName()).append("\n");
        }
        sb.append("══════════════════════════════════════════════════════════\n");
        return sb.toString();
}
}
