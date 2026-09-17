package srmcreditengine.controllers;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import srmcreditengine.models.SettlementDTO;
import srmcreditengine.models.SimulationDTO;
import srmcreditengine.models.SimulationRequestDTO;
import srmcreditengine.services.SettlementService;
import srmcreditengine.services.SimulationService;

import java.net.URI;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    @Autowired
    private SettlementService service;

    @Autowired
    private SimulationService simulationService;

    @PostMapping
    public ResponseEntity<SettlementDTO> settle(@Valid @RequestBody SettlementDTO request) throws BadRequestException {
        SettlementDTO response = service.settle(request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SettlementDTO> findById(@PathVariable Long id) {
        SettlementDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/simulation")
    public ResponseEntity<SimulationDTO> simulate(
            @Valid @RequestBody SimulationRequestDTO request
    ) {
        SimulationDTO response =
                simulationService.simulate(request);

        return ResponseEntity.ok(response);
    }
}
