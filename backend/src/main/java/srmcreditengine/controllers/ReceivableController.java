package srmcreditengine.controllers;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import srmcreditengine.models.ReceivableDTO;
import srmcreditengine.services.ReceivableService;

import java.net.URI;

@RestController
@RequestMapping(value = "/receivables")
public class ReceivableController {

    @Autowired
    private ReceivableService service;


    @GetMapping(value = "/{id}")
    public ResponseEntity<ReceivableDTO> findById(@PathVariable Long id) {
        ReceivableDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<ReceivableDTO>> findAll(
            @RequestParam(name = "assignor", defaultValue = "") String assignor,
            Pageable pageable) {
        Page<ReceivableDTO> dto = service.findAll(assignor, pageable);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ReceivableDTO> insert(@Valid @RequestBody ReceivableDTO dto) {
        dto  = service.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }


}
