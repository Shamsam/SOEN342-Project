import java.util.Set;
import java.time.DayOfWeek;

public class TrainSchedule {
    private Set<DayOfWeek> operatingDays;

    public TrainSchedule(Set<DayOfWeek> operatingDays) {
        this.operatingDays = operatingDays;
    }

    public Set<DayOfWeek> getOperatingDays() {
        return operatingDays;
    }

    public void setOperatingDays(Set<DayOfWeek> operatingDays) {
        this.operatingDays = operatingDays;
    }
}
