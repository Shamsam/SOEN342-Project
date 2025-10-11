package railsystem;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria {
    private String departureCity;
    private String arrivalCity;
    private LocalTime earliestDeparture;
    private LocalTime latestArrival;
    private Boolean nextDay;
    private String preferredTrain;
    private Set<DayOfWeek> travelDays;
    private BigDecimal firstClassRate;
    private BigDecimal secondClassRate;

    public SearchCriteria(SearchCriteria other) {
        this.departureCity = other.departureCity;
        this.arrivalCity = other.arrivalCity;
        this.earliestDeparture = other.earliestDeparture;
        this.latestArrival = other.latestArrival;
        this.nextDay = other.nextDay;
        this.preferredTrain = other.preferredTrain;
        this.travelDays = other.travelDays;
        this.firstClassRate = other.firstClassRate;
        this.secondClassRate = other.secondClassRate;
    }

    public boolean matches(Connection connection) {
        if (departureCity != null &&
                !connection.getDepartureStop().getCity().getName().equals(departureCity)) {
            return false;
        }

        if (arrivalCity != null &&
                !connection.getArrivalStop().getCity().getName().equals(arrivalCity)) {
            return false;
        }

        if (earliestDeparture != null &&
                connection.getDepartureStop().getScheduledStop()
                        .compareTo(earliestDeparture) < 0) {
            return false;
        }

        if (latestArrival != null &&
                connection.getArrivalStop().getScheduledStop()
                        .compareTo(latestArrival) > 0) {
            return false;
        }

        if (nextDay != null &&
                connection.getArrivalStop().isNextDay() != nextDay) {
            return false;
        }

        if (preferredTrain != null &&
                !connection.getTrain().getTrainType().equals(preferredTrain)) {
            return false;
        }

        if (travelDays != null &&
                !connection.getSchedule().getOperatingDays().containsAll(travelDays)) {
            return false;
        }

        if (firstClassRate != null &&
                connection.getTicketRates().getFirstClass().compareTo(firstClassRate) > 0) {
            return false;
        }

        if (secondClassRate != null &&
                connection.getTicketRates().getSecondClass().compareTo(secondClassRate) > 0) {
            return false;
        }

        return true;
    }
}
