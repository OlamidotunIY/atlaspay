package com.atlaspay.accounts.presentation;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlaspay.accounts.domain.model.OwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class VirtualAccountController {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;
    private final Random random = new Random();

    @PostMapping("/customers/issue")
    public ResponseEntity<Void> issueCustomerAccount(
            @RequestHeader("X-Customer-Id") String customerId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        
        // Randomly select Wema or Zenith for the customer
        String bankName = random.nextBoolean() ? "Wema" : "Zenith";

        issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                idempotencyKey,
                customerId,
                OwnerType.CUSTOMER,
                bankName
        ));
        
        return ResponseEntity.accepted().build();
    }
}
