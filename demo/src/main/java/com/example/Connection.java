package com.example;

import java.math.BigDecimal;
import java.util.Set;
import java.time.Duration;

public class Connection {
    private int routeId;
    private Train train;
    private TrainSchedule schedule;
    private TicketRates ticketRates;
    private TrainStop departure;
    private TrainStop arrival;

    public Connection(int routeId, Train train, TrainSchedule schedule, TicketRates ticketRates, TrainStop departure,
            TrainStop arrival) {
        this.routeId = routeId;
        this.train = train;
        this.schedule = schedule;
        this.ticketRates = ticketRates;
        this.departure = departure;
        this.arrival = arrival;
    }

    public Connection(int routeId, String trainType, Set<DaysOfWeek> operatingDays, BigDecimal firstClassRate,
            BigDecimal secondClassRate, City departureCity, Duration departureTime, City arrivalCity,
            Duration arrivalTime) {
        this.routeId = routeId;
        this.train = new Train(trainType);
        this.schedule = new TrainSchedule(operatingDays);
        this.ticketRates = new TicketRates(firstClassRate, secondClassRate);
        this.departure = new TrainStop(departureCity, departureTime);
        this.arrival = new TrainStop(arrivalCity, arrivalTime);
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

    public TrainStop getDeparture() {
        return departure;
    }

    public void setDeparture(TrainStop departure) {
        this.departure = departure;
    }

    public TrainStop getArrival() {
        return arrival;
    }

    public void setArrival(TrainStop arrival) {
        this.arrival = arrival;
    }
}
