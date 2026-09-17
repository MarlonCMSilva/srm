package srmcreditengine.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srmcreditengine.models.ReceivableDTO;
import srmcreditengine.entities.Receivable;
import srmcreditengine.entities.enums.ReceivableStatus;
import srmcreditengine.repositories.ReceivableRepository;

import java.lang.module.ResolutionException;

@Service
public class ReceivableService {

    @Autowired
    private ReceivableRepository repository;


    public ReceivableDTO findById(Long id){
        Receivable receivable = repository.findById(id).orElseThrow(
                ()  -> new ResolutionException("Recurso nao encontrado"));
        return  new ReceivableDTO(receivable);
    }

    @Transactional(readOnly = true)
    public Page<ReceivableDTO> findAll(String assignor, Pageable pageable) {
        Page<Receivable> result = repository.searchByAssignor(assignor, pageable);
        return  result.map(x -> new ReceivableDTO(x));
    }

    @Transactional
    public ReceivableDTO insert(ReceivableDTO dto) {
        Receivable entity = new Receivable();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);

        return new ReceivableDTO(entity);
    }



    private void copyDtoToEntity(ReceivableDTO dto, Receivable entity) {
        entity.setAssignor(dto.getAssignor());
        entity.setFaceValue(dto.getFaceValue());
        entity.setStatus(ReceivableStatus.AVAILABLE);
        entity.setTermInMonths(dto.getTermInMonths());
        entity.setType(dto.getType());
    }

}
