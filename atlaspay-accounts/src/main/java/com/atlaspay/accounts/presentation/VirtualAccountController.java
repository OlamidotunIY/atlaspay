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

    @PostMapping("/merchants/issue")
    public ResponseEntity<String> issueMerchantAccount(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        String actualIdempotencyKey = idempotencyKey != null ? idempotencyKey : java.util.UUID.randomUUID().toString();
        String bankName = random.nextBoolean() ? "Wema" : "Zenith";

        var accountId = issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                actualIdempotencyKey,
                merchantId,
                OwnerType.MERCHANT,
                bankName
        ));
        
        return ResponseEntity.accepted().body(accountId.value());
    }

    @PostMapping("/customers/{customerId}/issue")
    public ResponseEntity<String> issueCustomerAccount(
            @org.springframework.web.bind.annotation.PathVariable String customerId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        String actualIdempotencyKey = idempotencyKey != null ? idempotencyKey : java.util.UUID.randomUUID().toString();
        String bankName = random.nextBoolean() ? "Wema" : "Zenith";

        var accountId = issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                actualIdempotencyKey,
                customerId,
                OwnerType.CUSTOMER,
                bankName
        ));
        
        return ResponseEntity.accepted().body(accountId.value());
    }
}
