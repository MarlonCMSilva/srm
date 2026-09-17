package srmcreditengine.entities;


import jakarta.persistence.*;
import srmcreditengine.entities.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_exchange_rate")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Currency sourceCurrency;

    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    private BigDecimal rate;

    @Column(name = "effective_at")
    private LocalDateTime effectiveAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public ExchangeRate() {
    }

    public ExchangeRate(Long id, Currency sourCurrency, Currency targetCurrency, BigDecimal rate, LocalDateTime effectiveAt, LocalDateTime createdAt) {
        this.id = id;
        this.sourceCurrency = sourCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.effectiveAt = effectiveAt;
        this.createdAt = createdAt;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Currency getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDateTime getEffectiveAt() {
        return effectiveAt;
    }

    public void setEffectiveAt(LocalDateTime effectiveAt) {
        this.effectiveAt = effectiveAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
