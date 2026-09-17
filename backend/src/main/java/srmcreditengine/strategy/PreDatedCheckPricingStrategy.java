package srmcreditengine.strategy;

import org.springframework.stereotype.Component;
import srmcreditengine.entities.enums.ReceivableType;

import java.math.BigDecimal;

@Component
public class PreDatedCheckPricingStrategy implements PricingStrategy{
    @Override
    public boolean supports(ReceivableType type) {
        return type == ReceivableType.PRE_DATED_CHECK;
    }

    @Override
    public BigDecimal getSpread() {
        return new BigDecimal("0.025");
    }
}
