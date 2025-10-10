package com.example;

import java.util.Set;

enum DaysOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

public class TrainSchedule {
    private Set<DaysOfWeek> operatingDays;

    public TrainSchedule(Set<DaysOfWeek> operatingDays) {
        this.operatingDays = operatingDays;
    }

    public Set<DaysOfWeek> getOperatingDays() {
        return operatingDays;
    }

    public void setOperatingDays(Set<DaysOfWeek> operatingDays) {
        this.operatingDays = operatingDays;
    }
}
