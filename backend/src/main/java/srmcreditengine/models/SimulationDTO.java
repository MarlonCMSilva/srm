package srmcreditengine.models;

import srmcreditengine.entities.Settlement;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;

public class SimulationDTO {


    private Long id;
    private ReceivableType type;
    private BigDecimal faceValue;
    private BigDecimal presentValueBrl;
    private BigDecimal settledAmount;
    private Currency paymentCurrency;
    private BigDecimal baseRateUsed;
    private BigDecimal spreadUsed;
    private BigDecimal exchangeRateUsed;
    private Integer termInMonths;


    public SimulationDTO() {
    }


    public SimulationDTO(Long id, BigDecimal faceValue, ReceivableType type, BigDecimal presentValueBrl, BigDecimal settledAmount, Currency paymentCurrency, BigDecimal baseRateUsed, BigDecimal spreadUsed, BigDecimal exchangeRateUsed, Integer termInMonths) {
        this.id = id;
        this.faceValue = faceValue;
        this.type = type;
        this.presentValueBrl = presentValueBrl;
        this.settledAmount = settledAmount;
        this.paymentCurrency = paymentCurrency;
        this.baseRateUsed = baseRateUsed;
        this.spreadUsed = spreadUsed;
        this.exchangeRateUsed = exchangeRateUsed;
        this.termInMonths = termInMonths;
    }


    public SimulationDTO(Settlement entity) {
        this.id = entity.getId();
        this.faceValue = entity.getFaceValue();
        this.type = entity.getType();
        this.presentValueBrl = entity.getPresentValueBrl();
        this.settledAmount = entity.getSettledAmount();
        this.paymentCurrency = entity.getPaymentCurrency();
        this.baseRateUsed = entity.getBaseRateUsed();
        this.spreadUsed = entity.getSpreadUsed();
        this.exchangeRateUsed = entity.getExchangeRateUsed();
        this.termInMonths = entity.getTermInMonths();
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(BigDecimal faceValue) {
        this.faceValue = faceValue;
    }

    public ReceivableType getType() {
        return type;
    }

    public void setType(ReceivableType type) {
        this.type = type;
    }

    public BigDecimal getPresentValueBrl() {
        return presentValueBrl;
    }

    public void setPresentValueBrl(BigDecimal presentValueBrl) {
        this.presentValueBrl = presentValueBrl;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(BigDecimal settledAmount) {
        this.settledAmount = settledAmount;
    }

    public Currency getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(Currency paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public BigDecimal getBaseRateUsed() {
        return baseRateUsed;
    }

    public void setBaseRateUsed(BigDecimal baseRateUsed) {
        this.baseRateUsed = baseRateUsed;
    }

    public BigDecimal getSpreadUsed() {
        return spreadUsed;
    }

    public void setSpreadUsed(BigDecimal spreadUsed) {
        this.spreadUsed = spreadUsed;
    }

    public BigDecimal getExchangeRateUsed() {
        return exchangeRateUsed;
    }

    public void setExchangeRateUsed(BigDecimal exchangeRateUsed) {
        this.exchangeRateUsed = exchangeRateUsed;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public void setTermInMonths(Integer termInMonths) {
        this.termInMonths = termInMonths;
    }
}
