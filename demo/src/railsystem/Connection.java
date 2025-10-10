package demo.src.railsystem;
import java.math.BigDecimal;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Connection {
    private int routeId;
    private Train train;
    private TrainSchedule schedule;
    private TicketRates ticketRates;
    private TrainStop departureStop;
    private TrainStop arrivalStop;

    public Connection(int routeId, String trainType, Set<DayOfWeek> operatingDays, BigDecimal firstClassRate,
            BigDecimal secondClassRate, String departureCity, LocalTime departureTime, String arrivalCity,
            LocalTime arrivalTime, boolean nextDay) {
        this.routeId = routeId;
        this.train = new Train(trainType);
        this.schedule = new TrainSchedule(operatingDays);
        this.ticketRates = new TicketRates(firstClassRate, secondClassRate);
        this.departureStop = new TrainStop(departureCity, departureTime, false);
        this.arrivalStop = new TrainStop(arrivalCity, arrivalTime, nextDay);
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
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
}
