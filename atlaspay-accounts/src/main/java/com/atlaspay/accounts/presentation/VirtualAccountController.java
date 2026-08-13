package com.atlaspay.accounts.presentation;

import com.atlaspay.accounts.application.command.IssueVirtualAccountCommand;
import com.atlaspay.accounts.application.command.ForceCloseAccountsCommand;
import com.atlaspay.accounts.application.query.GetVirtualAccountsQuery;
import com.atlaspay.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlaspay.accounts.application.usecase.GetVirtualAccountsUseCase;
import com.atlaspay.accounts.application.usecase.ForceCloseAccountsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dedicated_account")
@RequiredArgsConstructor
public class VirtualAccountController {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;
    private final GetVirtualAccountsUseCase getVirtualAccountsUseCase;
    private final ForceCloseAccountsUseCase forceCloseAccountsUseCase;

    @PostMapping
    public ResponseEntity<String> issueCustomerAccount(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @RequestBody IssueVirtualAccountCommand command) {
        
        var accountId = issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                Long.valueOf(merchantId),
                command.customerCode(),
                command.accountName(),
                command.bankName(),
                command.idempotencyKey()
        ));
        
        return ResponseEntity.accepted().body(String.valueOf(accountId));
    }

    @GetMapping
    public ResponseEntity<?> listAccounts(@RequestHeader("X-Merchant-Id") String merchantId) {
        var accounts = getVirtualAccountsUseCase.execute(new GetVirtualAccountsQuery(Long.valueOf(merchantId)));
        return ResponseEntity.ok(accounts);
    }
}
