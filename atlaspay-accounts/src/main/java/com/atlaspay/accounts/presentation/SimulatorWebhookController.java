package com.atlaspay.accounts.presentation;

import com.atlaspay.accounts.application.command.ActivateVirtualAccountCommand;
import com.atlaspay.accounts.application.usecase.ActivateVirtualAccountUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts/webhooks")
@Tag(name = "Simulator Webhooks", description = "Mock banking provider webhooks (For testing)")
@RequiredArgsConstructor
public class SimulatorWebhookController {

    private final ActivateVirtualAccountUseCase activateVirtualAccountUseCase;

    @PostMapping("/simulator")
    public ResponseEntity<Void> handleSimulatorWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received simulator webhook: {}", payload);
        
        if ("virtual_account.created".equals(payload.get("event"))) {
            @SuppressWarnings("unchecked")
            Map<String, String> data = (Map<String, String>) payload.get("data");
            
            if (data != null && data.containsKey("reference") && data.containsKey("nuban")) {
                activateVirtualAccountUseCase.execute(new ActivateVirtualAccountCommand(
                        data.get("reference"),
                        data.get("nuban")
                ));
            }
        }
        
        return ResponseEntity.ok().build();
    }
}
