package srmcreditengine.services;

import org.springframework.stereotype.Service;
import srmcreditengine.entities.Receivable;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.models.PricingResult;
import srmcreditengine.strategy.PricingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PricingService {

    private static final BigDecimal BASE_RATE =
            new BigDecimal("0.0100");

    private static final int SCALE = 2;

    private final List<PricingStrategy> strategies;

    public PricingService(List<PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public PricingResult calculate(
            Receivable receivable,
            Currency paymentCurrency,
            BigDecimal exchangeRate
    ) {
        BigDecimal spread = strategies.stream()
                .filter(strategy ->
                        strategy.supports(receivable.getType()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estratégia não encontrada para o tipo"))
                .getSpread();

        BigDecimal totalRate = BASE_RATE.add(spread);

        BigDecimal divisor = BigDecimal.ONE
                .add(totalRate)
                .pow(receivable.getTermInMonths());

        BigDecimal presentValueBrl = receivable.getFaceValue()
                .divide(divisor, 10, RoundingMode.HALF_EVEN)
                .setScale(SCALE, RoundingMode.HALF_EVEN);

        BigDecimal settledAmount = presentValueBrl;

        if (paymentCurrency == Currency.USD) {
            if (exchangeRate == null ||
                    exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Taxa de câmbio obrigatória para pagamento em USD");
            }

            settledAmount = presentValueBrl
                    .divide(exchangeRate, 10, RoundingMode.HALF_EVEN)
                    .setScale(SCALE, RoundingMode.HALF_EVEN);
        }

        return new PricingResult(
                presentValueBrl,
                settledAmount,
                BASE_RATE,
                spread,
                exchangeRate
        );
    }
}
