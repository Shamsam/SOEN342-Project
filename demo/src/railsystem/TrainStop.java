package demo.src.railsystem;
import java.time.LocalTime;

public class TrainStop {
    private City city;
    private LocalTime scheduledStop;
    private boolean nextDay;

    public TrainStop(City city, LocalTime scheduledStop, boolean nextDay) {
        this.city = city;
        this.scheduledStop = scheduledStop;
        this.nextDay = nextDay;
    }

    public TrainStop(String cityName, LocalTime scheduledStop, boolean nextDay) {
        this.city = new City(cityName);
        this.scheduledStop = scheduledStop;
        this.nextDay = nextDay;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public LocalTime getScheduledStop() {
        return scheduledStop;
    }

    public void setScheduledStop(LocalTime scheduledStop) {
        this.scheduledStop = scheduledStop;
    }

    public void setNextDay(boolean nextDay) {
        this.nextDay = nextDay;
    }

    public boolean getNextDay() {
        return nextDay;
    }
}
