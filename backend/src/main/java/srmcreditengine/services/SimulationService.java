package srmcreditengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import srmcreditengine.entities.ExchangeRate;
import srmcreditengine.entities.Receivable;
import srmcreditengine.entities.enums.Currency;
import srmcreditengine.models.PricingResult;
import srmcreditengine.models.SimulationDTO;
import srmcreditengine.models.SimulationRequestDTO;
import srmcreditengine.repositories.ExchangeRateRepository;

import java.math.BigDecimal;

@Service
public class SimulationService {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;


    public SimulationDTO simulate(
            SimulationRequestDTO request
    ) {
        Receivable receivable = new Receivable();

        receivable.setType(request.getType());
        receivable.setFaceValue(request.getFaceValue());
        receivable.setTermInMonths(request.getTermInMonths());

        BigDecimal exchangeRate = null;

        if (request.getPaymentCurrency() == Currency.USD) {
            exchangeRate = exchangeRateRepository
                    .findTopBySourceCurrencyAndTargetCurrencyOrderByEffectiveAtDesc(
                            Currency.BRL,
                            Currency.USD)
                    .map(ExchangeRate::getRate)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Taxa BRL/USD não encontrada"
                            )
                    );
        }

        PricingResult result = pricingService.calculate(
                receivable,
                request.getPaymentCurrency(),
                exchangeRate
        );

        return new SimulationDTO(
                null,
                receivable.getFaceValue(),
                receivable.getType(),
                result.presentValueBrl(),
                result.settledAmount(),
                request.getPaymentCurrency(),
                result.baseRate(),
                result.spread(),
                result.exchangeRate(),
                receivable.getTermInMonths()
        );
    }
}