package com.example;

import java.time.Duration;

public class TrainStop {
    private City city;
    private Duration scheduledStop;

    public TrainStop(City city, Duration scheduledStop) {
        this.city = city;
        this.scheduledStop = scheduledStop;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Duration getScheduledStop() {
        return scheduledStop;
    }

    public void setScheduledStop(Duration scheduledStop) {
        this.scheduledStop = scheduledStop;
    }
}
