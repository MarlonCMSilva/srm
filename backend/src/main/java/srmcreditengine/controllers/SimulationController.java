package srmcreditengine.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import srmcreditengine.entities.Receivable;
import srmcreditengine.models.PricingResult;
import srmcreditengine.models.SettlementRequestDTO;
import srmcreditengine.models.SimulationDTO;
import srmcreditengine.repositories.ReceivableRepository;
import srmcreditengine.services.PricingService;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;

@RestController
@RequestMapping(value = "/simulations")
public class SimulationController {

    @Autowired
    private ReceivableRepository receivableRepository;

    @Autowired
    private PricingService pricingService;


    @PostMapping
    public ResponseEntity<SimulationDTO> simulate( @Valid @RequestBody SettlementRequestDTO request) {

        Receivable receivable = receivableRepository
                .findById(request.getReceivableId()).orElseThrow(() ->
                new ResolutionException("Recebível não encontrado"));

        BigDecimal exchangeRate = null;

        PricingResult result = pricingService.calculate(
                receivable,
                request.getPaymentCurrency(),
                exchangeRate
        );

        BigDecimal discountAmount = receivable.getFaceValue().subtract(result.presentValueBrl());

        SimulationDTO response = new SimulationDTO(
                receivable.getId(),
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

        return ResponseEntity.ok(response);
    }

}
