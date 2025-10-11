package railsystem;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Connection {

    String routeId;
    Train train;
    TrainSchedule schedule;
    TicketRates ticketRates;
    TrainStop departureStop;
    TrainStop arrivalStop;

    public static Connection of(
            String routeId,
            String trainType,
            Set<DayOfWeek> operatingDays,
            BigDecimal firstClassRate,
            BigDecimal secondClassRate,
            String departureCity,
            LocalTime departureTime,
            String arrivalCity,
            LocalTime arrivalTime,
            boolean nextDay) {

        Objects.requireNonNull(routeId, "routeId cannot be null");
        Objects.requireNonNull(trainType, "trainType cannot be null");
        Objects.requireNonNull(operatingDays, "operatingDays cannot be null");
        Objects.requireNonNull(firstClassRate, "firstClassRate cannot be null");
        Objects.requireNonNull(secondClassRate, "secondClassRate cannot be null");
        Objects.requireNonNull(departureCity, "departureCity cannot be null");
        Objects.requireNonNull(arrivalCity, "arrivalCity cannot be null");
        Objects.requireNonNull(departureTime, "departureTime cannot be null");
        Objects.requireNonNull(arrivalTime, "arrivalTime cannot be null");

        return Connection.builder()
                .routeId(routeId)
                .train(Train.getInstance(trainType))
                .schedule(new TrainSchedule(operatingDays))
                .ticketRates(new TicketRates(firstClassRate, secondClassRate))
                .departureStop(new TrainStop(departureCity, departureTime, false))
                .arrivalStop(new TrainStop(arrivalCity, arrivalTime, nextDay))
                .build();
    }

    @Override
    public String toString() {
        return """
                Route ID: %s
                Train Type: %s
                Operating Days: %s
                First Class Rate: %s
                Second Class Rate: %s
                Departure City: %s
                Departure Time: %s
                Arrival City: %s
                Arrival Time: %s%s
                """.formatted(
                routeId,
                train.getTrainType(),
                schedule.getOperatingDays(),
                ticketRates.getFirstClass(),
                ticketRates.getSecondClass(),
                departureStop.getCity().getName(),
                departureStop.getScheduledStop(),
                arrivalStop.getCity().getName(),
                arrivalStop.getScheduledStop(),
                arrivalStop.isNextDay() ? " (+1d)" : "");
    }
}
