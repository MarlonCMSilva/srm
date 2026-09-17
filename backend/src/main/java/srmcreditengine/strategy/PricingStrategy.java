package srmcreditengine.strategy;

import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;

public interface PricingStrategy {

    boolean supports(ReceivableType type);

    BigDecimal getSpread();
}
