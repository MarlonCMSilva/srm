package srmcreditengine.entities;

import jakarta.persistence.*;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.entities.enums.ReceivableStatus;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assignor;
    private BigDecimal faceValue;

    @Enumerated(EnumType.STRING)
    private ReceivableType type;
    private BigDecimal presentValueBrl;
    private BigDecimal settledAmount;

    @Enumerated(EnumType.STRING)
    private Currency paymentCurrency;
    private BigDecimal baseRateUsed;
    private BigDecimal spreadUsed;

    @Enumerated(EnumType.STRING)
    private ReceivableStatus status;
    private BigDecimal exchangeRateUsed;
    private Integer termInMonths;

    @Column(name = "created_at",
            columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "receivable_id", unique = true, nullable = false)
    private Receivable receivable;

    public Settlement() {
    }

    public Settlement(Long id, String assignor, BigDecimal faceValue, ReceivableType type, BigDecimal presentValueBrl, BigDecimal settledAmount, Currency paymentCurrency, BigDecimal baseRateUsed, BigDecimal spreadUsed, ReceivableStatus status, BigDecimal exchangeRateUsed, Integer termInMonths, LocalDateTime createdAt, Receivable receivables) {
        this.id = id;
        this.assignor = assignor;
        this.faceValue = faceValue;
        this.type = type;
        this.presentValueBrl = presentValueBrl;
        this.settledAmount = settledAmount;
        this.paymentCurrency = paymentCurrency;
        this.baseRateUsed = baseRateUsed;
        this.spreadUsed = spreadUsed;
        this.status = status;
        this.exchangeRateUsed = exchangeRateUsed;
        this.termInMonths = termInMonths;
        this.createdAt = createdAt;
        this.receivable = receivables;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ReceivableStatus getStatus() {
        return status;
    }

    public void setStatus(ReceivableStatus status) {
        this.status = status;
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

    public Receivable getReceivables() {
        return receivable;
    }

    public void setReceivables(Receivable receivables) {
        this.receivable = receivables;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Settlement that = (Settlement) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
