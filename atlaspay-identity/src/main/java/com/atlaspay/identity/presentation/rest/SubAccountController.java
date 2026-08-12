package com.atlaspay.identity.presentation.rest;

import com.atlaspay.identity.application.command.RegisterSubAccountCommand;
import com.atlaspay.identity.application.dto.RegisterSubAccountResult;
import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.identity.application.query.GetSubAccountQuery;
import com.atlaspay.identity.application.query.ListSubAccountsQuery;
import com.atlaspay.identity.application.usecase.GetSubAccountUseCase;
import com.atlaspay.identity.application.usecase.ListSubAccountsUseCase;
import com.atlaspay.identity.application.usecase.RegisterSubAccountUseCase;
import com.atlaspay.identity.presentation.rest.dto.RegisterSubAccountRequest;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;
import com.atlaspay.shared.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/subaccounts")
@Tag(name = "Sub Accounts", description = "Sub Account management")
public class SubAccountController {

    private final RegisterSubAccountUseCase registerSubAccountUseCase;
    private final GetSubAccountUseCase getSubAccountUseCase;
    private final ListSubAccountsUseCase listSubAccountsUseCase;

    public SubAccountController(RegisterSubAccountUseCase registerSubAccountUseCase,
                                GetSubAccountUseCase getSubAccountUseCase,
                                ListSubAccountsUseCase listSubAccountsUseCase) {
        this.registerSubAccountUseCase = registerSubAccountUseCase;
        this.getSubAccountUseCase = getSubAccountUseCase;
        this.listSubAccountsUseCase = listSubAccountsUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a sub account", description = "Registers a new sub account for a merchant")
    public ResponseEntity<RegisterSubAccountResult> register(@Valid @RequestBody RegisterSubAccountRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        RegisterSubAccountCommand command = new RegisterSubAccountCommand(
                new MerchantId(merchantIdStr),
                request.bankCode(),
                request.accountNumber(),
                request.description()
        );
        RegisterSubAccountResult result = registerSubAccountUseCase.execute(command);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
    
    @GetMapping("/{subAccountId}")
    @Operation(summary = "Get a sub account", description = "Retrieves a sub account by ID")
    public ResponseEntity<SubAccountDto> get(@PathVariable String subAccountId, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        GetSubAccountQuery query = new GetSubAccountQuery(new MerchantId(merchantIdStr), new SubAccountId(subAccountId));
        SubAccountDto result = getSubAccountUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping
    @Operation(summary = "List sub accounts", description = "Retrieves a paginated list of sub accounts")
    public ResponseEntity<PageResult<SubAccountDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        ListSubAccountsQuery query = new ListSubAccountsQuery(new MerchantId(merchantIdStr), page, size);
        PageResult<SubAccountDto> result = listSubAccountsUseCase.execute(query);
        return ResponseEntity.ok(result);
    }
}
