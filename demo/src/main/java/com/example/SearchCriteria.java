package com.example;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;

public class SearchCriteria {
    City departureCity;
    City arrivalCity;
    Duration earliestDeparture;
    Duration latestArrival;
    Train preferredTrain;
    Set<DaysOfWeek> travelDays;
    BigDecimal firstClassrate;
    BigDecimal secondClassRate;

    public SearchCriteria(City departureCity, City arrivalCity, Duration earliestDeparture, Duration latestArrival,
            Train preferredTrain, Set<DaysOfWeek> travelDays, BigDecimal firstClassrate,
            BigDecimal secondClassRate) {
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.earliestDeparture = earliestDeparture;
        this.latestArrival = latestArrival;
        this.preferredTrain = preferredTrain;
        this.travelDays = travelDays;
        this.firstClassrate = firstClassrate;
        this.secondClassRate = secondClassRate;
    }

    public City getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(City departureCity) {
        this.departureCity = departureCity;
    }

    public City getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(City arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public Duration getEarliestDeparture() {
        return earliestDeparture;
    }

    public void setEarliestDeparture(Duration earliestDeparture) {
        this.earliestDeparture = earliestDeparture;
    }

    public Duration getLatestArrival() {
        return latestArrival;
    }

    public void setLatestArrival(Duration latestArrival) {
        this.latestArrival = latestArrival;
    }

    public Train getPreferredTrain() {
        return preferredTrain;
    }

    public void setPreferredTrain(Train preferredTrain) {
        this.preferredTrain = preferredTrain;
    }

    public Set<DaysOfWeek> getTravelDays() {
        return travelDays;
    }

    public void setTravelDays(Set<DaysOfWeek> travelDays) {
        this.travelDays = travelDays;
    }

    public BigDecimal getFirstClassrate() {
        return firstClassrate;
    }

    public void setFirstClassrate(BigDecimal firstClassrate) {
        this.firstClassrate = firstClassrate;
    }

    public BigDecimal getSecondClassRate() {
        return secondClassRate;
    }

    public void setSecondClassRate(BigDecimal secondClassRate) {
        this.secondClassRate = secondClassRate;
    }

    public boolean matches(Connection connection) {
        if (connection.getDeparture().getCity() != this.departureCity) {
            return false;
        }
        if (connection.getArrival().getCity() != this.arrivalCity) {
            return false;
        }
        if (connection.getDeparture().getScheduledStop().compareTo(this.earliestDeparture) < 0) {
            return false;
        }
        if (connection.getArrival().getScheduledStop().compareTo(this.latestArrival) > 0) {
            return false;
        }
        if (this.preferredTrain != null && connection.getTrain() != this.preferredTrain) {
            return false;
        }
        if (!connection.getSchedule().getOperatingDays().containsAll(this.travelDays)) {
            return false;
        }
        if (this.firstClassrate != null
                && connection.getTicketRates().getFirstClass().compareTo(this.firstClassrate) > 0) {
            return false;
        }
        if (this.secondClassRate != null
                && connection.getTicketRates().getSecondClass().compareTo(this.secondClassRate) > 0) {
            return false;
        }
        return true;
    }

}
