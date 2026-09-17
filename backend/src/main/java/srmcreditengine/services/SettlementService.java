package srmcreditengine.services;


import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srmcreditengine.models.PricingResult;
import srmcreditengine.models.SettlementDTO;
import srmcreditengine.entities.Receivable;
import srmcreditengine.entities.Settlement;
import srmcreditengine.entities.enums.ReceivableStatus;
import srmcreditengine.models.SettlementRequestDTO;
import srmcreditengine.repositories.ReceivableRepository;
import srmcreditengine.repositories.SettlementRepository;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SettlementService {

    @Autowired
    private ReceivableRepository receivableRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private PricingService pricingService;

    @Transactional
    public SettlementDTO settle(SettlementDTO request) throws BadRequestException {

        Receivable receivable = receivableRepository
                .findById(request.getReceivableId())
                .orElseThrow(() -> new ResolutionException("Recebivel nao encontrado"));

        if (receivable.getStatus() != ReceivableStatus.AVAILABLE) {
            throw new ResolutionException(
                    "Recebível já foi liquidado ou não está disponível");
        }

        if (settlementRepository
                .existsByReceivableId(receivable.getId())) {
            throw new BadRequestException(
                    "Recebível já possui uma liquidação");
        }

        BigDecimal exchangeRate = null;


        PricingResult result = pricingService.calculate(
                receivable,
                request.getPaymentCurrency(),
                exchangeRate
        );

        Settlement settlement = new Settlement();

        settlement.setAssignor(receivable.getAssignor());
        settlement.setFaceValue(receivable.getFaceValue());
        settlement.setType(receivable.getType());
        settlement.setPresentValueBrl(result.presentValueBrl());
        settlement.setSettledAmount(result.settledAmount());
        settlement.setPaymentCurrency(request.getPaymentCurrency());
        settlement.setBaseRateUsed(result.baseRate());
        settlement.setSpreadUsed(result.spread());
        settlement.setExchangeRateUsed(result.exchangeRate());
        settlement.setTermInMonths(receivable.getTermInMonths());
        settlement.setStatus(ReceivableStatus.SETTLED);
        settlement.setCreatedAt(LocalDateTime.now());
        settlement.setReceivables(receivable);

        settlement = settlementRepository.save(settlement);

        receivable.setStatus(ReceivableStatus.SETTLED);
        receivable.setSettlement(settlement);

        receivableRepository.save(receivable);

        return new SettlementDTO(settlement);
    }


    public SettlementDTO findById(Long id){
        Settlement result = settlementRepository.findById(id).orElseThrow(
                ()  -> new ResolutionException("Recurso nao encontrado"));
        return  new SettlementDTO(result);
    }

}
