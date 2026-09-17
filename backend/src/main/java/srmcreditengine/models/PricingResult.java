package srmcreditengine.models;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal presentValueBrl,
        BigDecimal settledAmount,
        BigDecimal baseRate,
        BigDecimal spread,
        BigDecimal exchangeRate
) {
}
