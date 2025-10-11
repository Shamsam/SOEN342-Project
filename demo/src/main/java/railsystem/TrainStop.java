package railsystem;

import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class TrainStop {

    private final City city;
    private final LocalTime scheduledStop;
    private final boolean nextDay;

    public TrainStop(City city, LocalTime scheduledStop, boolean nextDay) {
        if (city == null) {
            throw new IllegalArgumentException("City cannot be null");
        }
        if (scheduledStop == null) {
            throw new IllegalArgumentException("Scheduled stop time cannot be null");
        }

        this.city = city;
        this.scheduledStop = scheduledStop;
        this.nextDay = nextDay;
    }

    public TrainStop(String cityName, LocalTime scheduledStop, boolean nextDay) {
        this(City.getInstance(cityName), scheduledStop, nextDay);
    }
}
