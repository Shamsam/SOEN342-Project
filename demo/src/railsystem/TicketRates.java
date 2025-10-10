package demo.src.railsystem;

import java.math.BigDecimal;

public class TicketRates {
    private BigDecimal firstClass;
    private BigDecimal secondClass;

    public TicketRates(BigDecimal firstClass, BigDecimal secondClass) {
        this.firstClass = firstClass;
        this.secondClass = secondClass;
    }

    public BigDecimal getFirstClass() {
        return firstClass;
    }

    public void setFirstClass(BigDecimal firstClass) {
        this.firstClass = firstClass;
    }

    public BigDecimal getSecondClass() {
        return secondClass;
    }

    public void setSecondClass(BigDecimal secondClass) {
        this.secondClass = secondClass;
    }
}
