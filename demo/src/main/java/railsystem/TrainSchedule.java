package railsystem;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class TrainSchedule {

    private final Set<DayOfWeek> operatingDays;

    public TrainSchedule(Set<DayOfWeek> operatingDays) {
        if (operatingDays == null || operatingDays.isEmpty()) {
            throw new IllegalArgumentException("Operating days cannot be null or empty");
        }

        // Defensive copy to prevent external modification
        this.operatingDays = Collections.unmodifiableSet(EnumSet.copyOf(operatingDays));
    }

    public boolean operatesOn(DayOfWeek day) {
        return operatingDays.contains(day);
    }
}
