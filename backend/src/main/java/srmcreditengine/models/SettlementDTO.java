package srmcreditengine.models;

import srmcreditengine.entities.Settlement;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementDTO {

    private Long id;
    private Long receivableId;
    private String assignor;
    private BigDecimal faceValue;
    private ReceivableType type;
    private BigDecimal presentValueBrl;
    private BigDecimal settledAmount;
    private Currency paymentCurrency;
    private BigDecimal baseRateUsed;
    private BigDecimal spreadUsed;
    private BigDecimal exchangeRateUsed;
    private Integer termInMonths;
    private LocalDateTime createdAt;

    public SettlementDTO() {
    }

    public SettlementDTO(Settlement entity) {
        this.id = entity.getId();
        this.receivableId = entity.getReceivables().getId();
        this.assignor = entity.getAssignor();
        this.faceValue = entity.getFaceValue();
        this.type = entity.getType();
        this.presentValueBrl = entity.getPresentValueBrl();
        this.settledAmount = entity.getSettledAmount();
        this.paymentCurrency = entity.getPaymentCurrency();
        this.baseRateUsed = entity.getBaseRateUsed();
        this.spreadUsed = entity.getSpreadUsed();
        this.exchangeRateUsed = entity.getExchangeRateUsed();
        this.termInMonths = entity.getTermInMonths();
        this.createdAt = entity.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReceivableId() {
        return receivableId;
    }

    public void setReceivableId(Long receivableId) {
        this.receivableId = receivableId;
    }

    public String getAssignor() {
        return assignor;
    }

    public void setAssignor(String assignor) {
        this.assignor = assignor;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
