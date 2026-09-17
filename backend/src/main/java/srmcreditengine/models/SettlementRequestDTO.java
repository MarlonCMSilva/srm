package srmcreditengine.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;

public class SettlementRequestDTO {

    @NotNull
    private Long receivableId;

    @NotNull
    private Currency paymentCurrency;


    public SettlementRequestDTO() {
    }

    public Long getReceivableId() {
        return receivableId;
    }

    public void setReceivableId(Long receivableId) {
        this.receivableId = receivableId;
    }

    public Currency getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(Currency paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }
}

