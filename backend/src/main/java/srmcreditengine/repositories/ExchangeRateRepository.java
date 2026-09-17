package srmcreditengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import srmcreditengine.entities.ExchangeRate;
import srmcreditengine.entities.enums.Currency;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate>
    findTopBySourceCurrencyAndTargetCurrencyOrderByEffectiveAtDesc(
            Currency sourceCurrency,
            Currency targetCurrency
    );
}
