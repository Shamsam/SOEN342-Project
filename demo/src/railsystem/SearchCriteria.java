import java.math.BigDecimal;
import java.util.Set;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

public class SearchCriteria {
    Optional<String> departureCity;
    Optional<String> arrivalCity;
    Optional<LocalTime> earliestDeparture;
    Optional<LocalTime> latestArrival;
    Optional<Boolean> nextDay;
    Optional<String> preferredTrain;
    Optional<Set<DayOfWeek>> travelDays;
    Optional<BigDecimal> firstClassrate;
    Optional<BigDecimal> secondClassRate;

    public SearchCriteria(String departureCity, String arrivalCity, LocalTime earliestDeparture,
            LocalTime latestArrival,
            boolean nextDay,
            String preferredTrain, Set<DayOfWeek> travelDays, BigDecimal firstClassrate,
            BigDecimal secondClassRate) {
        this.departureCity = Optional.ofNullable(departureCity);
        this.arrivalCity = Optional.ofNullable(arrivalCity);
        this.earliestDeparture = Optional.ofNullable(earliestDeparture);
        this.latestArrival = Optional.ofNullable(latestArrival);
        this.nextDay = Optional.of(nextDay);
        this.preferredTrain = Optional.ofNullable(preferredTrain);
        this.travelDays = Optional.ofNullable(travelDays);
        this.firstClassrate = Optional.ofNullable(firstClassrate);
        this.secondClassRate = Optional.ofNullable(secondClassRate);
    }

    public SearchCriteria() {
        this.departureCity = Optional.empty();
        this.arrivalCity = Optional.empty();
        this.earliestDeparture = Optional.empty();
        this.latestArrival = Optional.empty();
        this.nextDay = Optional.empty();
        this.preferredTrain = Optional.empty();
        this.travelDays = Optional.empty();
        this.firstClassrate = Optional.empty();
        this.secondClassRate = Optional.empty();
    }

    public String getDepartureCity() {
        return departureCity.orElse(null);
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = Optional.ofNullable(departureCity);
    }

    public String getArrivalCity() {
        return arrivalCity.orElse(null);
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = Optional.ofNullable(arrivalCity);
    }

    public LocalTime getEarliestDeparture() {
        return earliestDeparture.orElse(null);
    }

    public void setEarliestDeparture(LocalTime earliestDeparture) {
        this.earliestDeparture = Optional.ofNullable(earliestDeparture);
    }

    public LocalTime getLatestArrival() {
        return latestArrival.orElse(null);
    }

    public void setLatestArrival(LocalTime latestArrival) {
        this.latestArrival = Optional.ofNullable(latestArrival);
    }

    public boolean getNextDay() {
        return nextDay.orElse(false);
    }

    public void setNextDay(boolean nextDay) {
        this.nextDay = Optional.of(nextDay);
    }

    public String getPreferredTrain() {
        return preferredTrain.orElse(null);
    }

    public void setPreferredTrain(String preferredTrain) {
        this.preferredTrain = Optional.ofNullable(preferredTrain);
    }

    public Set<DayOfWeek> getTravelDays() {
        return travelDays.orElse(null);
    }

    public void setTravelDays(Set<DayOfWeek> travelDays) {
        this.travelDays = Optional.ofNullable(travelDays);
    }

    public BigDecimal getFirstClassrate() {
        return firstClassrate.orElse(null);
    }

    public void setFirstClassrate(BigDecimal firstClassrate) {
        this.firstClassrate = Optional.ofNullable(firstClassrate);
    }

    public BigDecimal getSecondClassRate() {
        return secondClassRate.orElse(null);
    }

    public void setSecondClassRate(BigDecimal secondClassRate) {
        this.secondClassRate = Optional.ofNullable(secondClassRate);
    }

    public boolean matches(Connection connection) {
        if (this.departureCity.isPresent()
                && !connection.getDepartureStop().getCity().getName().equals(this.departureCity.get())) {
            return false;
        }
        if (this.arrivalCity.isPresent()
                && !connection.getArrivalStop().getCity().getName().equals(this.arrivalCity.get())) {
            return false;
        }
        if (this.earliestDeparture.isPresent() &&
                connection.getDepartureStop().getScheduledStop().compareTo(this.earliestDeparture.get()) < 0) {
            return false;
        }
        if (this.nextDay.isPresent() && connection.getArrivalStop().getNextDay() == this.nextDay.get()
                && connection.getArrivalStop().getScheduledStop().compareTo(this.latestArrival.orElse(null)) > 0) {
            return false;
        }
        if (this.preferredTrain.isPresent()
                && !connection.getTrain().getTrainType().equals(this.preferredTrain.get())) {
            return false;
        }
        if (this.travelDays.isPresent()
                && !connection.getSchedule().getOperatingDays().containsAll(this.travelDays.get())) {
            return false;
        }
        if (this.firstClassrate.isPresent()
                && connection.getTicketRates().getFirstClass().compareTo(this.firstClassrate.get()) > 0) {
            return false;
        }
        if (this.secondClassRate.isPresent()
                && connection.getTicketRates().getSecondClass().compareTo(this.secondClassRate.get()) > 0) {
            return false;
        }
        return true;
    }

}
