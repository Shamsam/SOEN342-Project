import java.math.BigDecimal;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Connection {
    private String routeId;
    private Train train;
    private TrainSchedule schedule;
    private TicketRates ticketRates;
    private TrainStop departureStop;
    private TrainStop arrivalStop;

    public Connection(String routeId, String trainType, Set<DayOfWeek> operatingDays, BigDecimal firstClassRate,
            BigDecimal secondClassRate, String departureCity, LocalTime departureTime, String arrivalCity,
            LocalTime arrivalTime, boolean nextDay) {
        this.routeId = routeId;
        this.train = new Train(trainType);
        this.schedule = new TrainSchedule(operatingDays);
        this.ticketRates = new TicketRates(firstClassRate, secondClassRate);
        this.departureStop = new TrainStop(departureCity, departureTime, false);
        this.arrivalStop = new TrainStop(arrivalCity, arrivalTime, nextDay);
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public TrainSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TrainSchedule schedule) {
        this.schedule = schedule;
    }

    public TicketRates getTicketRates() {
        return ticketRates;
    }

    public void setTicketRates(TicketRates ticketRates) {
        this.ticketRates = ticketRates;
    }

    public TrainStop getDepartureStop() {
        return departureStop;
    }

    public void setDepartureStop(TrainStop departureStop) {
        this.departureStop = departureStop;
    }

    public TrainStop getArrivalStop() {
        return arrivalStop;
    }

    public void setArrivalStop(TrainStop arrivalStop) {
        this.arrivalStop = arrivalStop;
    }

    @Override
    public String toString() {
        return "Route ID: " + routeId + "\n"
                + "Train Type: " + train.getTrainType() + "\n"
                + "Operating Days: " + schedule.getOperatingDays() + "\n"
                + "First Class Rate: " + ticketRates.getFirstClass() + "\n"
                + "Second Class Rate: " + ticketRates.getSecondClass() + "\n"
                + "Departure City: " + departureStop.getCity().getName() + "\n"
                + "Departure Time: " + departureStop.getScheduledStop() + "\n"
                + "Arrival City: " + arrivalStop.getCity().getName() + "\n"
                + "Arrival Time: " + arrivalStop.getScheduledStop() + (arrivalStop.getNextDay() ? " (+1d)" : "");
    }
}
