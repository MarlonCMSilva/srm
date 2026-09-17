package srmcreditengine.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;

public class SimulationRequestDTO {
    @NotBlank
    private String assignor;

    @NotNull
    private ReceivableType type;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal faceValue;

    @NotNull
    @Min(0)
    private Integer termInMonths;

    @NotNull
    private Currency paymentCurrency;

    public SimulationRequestDTO() {
    }

    public String getAssignor() {
        return assignor;
    }

    public void setAssignor(String assignor) {
        this.assignor = assignor;
    }

    public ReceivableType getType() {
        return type;
    }

    public void setType(ReceivableType type) {
        this.type = type;
    }

    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(BigDecimal faceValue) {
        this.faceValue = faceValue;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public void setTermInMonths(Integer termInMonths) {
        this.termInMonths = termInMonths;
    }

    public Currency getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(Currency paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }
}
