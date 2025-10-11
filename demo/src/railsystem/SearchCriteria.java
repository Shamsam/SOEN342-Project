import java.math.BigDecimal;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class SearchCriteria {
    City departureCity;
    City arrivalCity;
    LocalTime earliestDeparture;
    LocalTime latestArrival;
    boolean nextDay;
    Train preferredTrain;
    Set<DayOfWeek> travelDays;
    BigDecimal firstClassrate;
    BigDecimal secondClassRate;

    public SearchCriteria(City departureCity, City arrivalCity, LocalTime earliestDeparture, LocalTime latestArrival,
            boolean nextDay,
            Train preferredTrain, Set<DayOfWeek> travelDays, BigDecimal firstClassrate,
            BigDecimal secondClassRate) {
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.earliestDeparture = earliestDeparture;
        this.latestArrival = latestArrival;
        this.nextDay = nextDay;
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

    public LocalTime getEarliestDeparture() {
        return earliestDeparture;
    }

    public void setEarliestDeparture(LocalTime earliestDeparture) {
        this.earliestDeparture = earliestDeparture;
    }

    public LocalTime getLatestArrival() {
        return latestArrival;
    }

    public void setLatestArrival(LocalTime latestArrival) {
        this.latestArrival = latestArrival;
    }

    public boolean getNextDay() {
        return nextDay;
    }

    public void setNextDay(boolean nextDay) {
        this.nextDay = nextDay;
    }

    public Train getPreferredTrain() {
        return preferredTrain;
    }

    public void setPreferredTrain(Train preferredTrain) {
        this.preferredTrain = preferredTrain;
    }

    public Set<DayOfWeek> getTravelDays() {
        return travelDays;
    }

    public void setTravelDays(Set<DayOfWeek> travelDays) {
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
        if (connection.getDepartureStop().getCity() != this.departureCity) {
            return false;
        }
        if (connection.getArrivalStop().getCity() != this.arrivalCity) {
            return false;
        }
        if (connection.getDepartureStop().getScheduledStop().compareTo(this.earliestDeparture) < 0) {
            return false;
        }
        if (connection.getArrivalStop().getNextDay() == this.nextDay
                && connection.getArrivalStop().getScheduledStop().compareTo(this.latestArrival) > 0) {
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
