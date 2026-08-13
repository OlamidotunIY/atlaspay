package com.atlaspay.simulator.presentation.rest;

import com.atlaspay.simulator.application.command.GenerateSimulatorAccountCommand;
import com.atlaspay.simulator.application.dto.AccountGenerationRequestDto;
import com.atlaspay.simulator.application.usecase.GenerateSimulatorAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulator/mock/accounts")
@RequiredArgsConstructor
public class SimulatorAccountController {

    private final GenerateSimulatorAccountUseCase useCase;

    @PostMapping("/issue")
    public ResponseEntity<Void> issueAccount(@RequestBody AccountGenerationRequestDto request) {
        useCase.execute(new GenerateSimulatorAccountCommand(
                request.referenceId(),
                request.accountName(),
                request.callbackUrl(),
                request.bankName()
        ));
        
        return ResponseEntity.accepted().build();
    }
}
