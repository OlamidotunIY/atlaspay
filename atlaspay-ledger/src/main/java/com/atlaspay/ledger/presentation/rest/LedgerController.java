package com.atlaspay.ledger.presentation.rest;

import com.atlaspay.ledger.application.dto.BalanceDto;
import com.atlaspay.ledger.application.dto.LedgerHistoryDto;
import com.atlaspay.ledger.application.query.GetAccountBalanceQuery;
import com.atlaspay.ledger.application.query.GetLedgerHistoryQuery;
import com.atlaspay.ledger.application.usecase.GetAccountBalanceUseCase;
import com.atlaspay.ledger.application.usecase.GetLedgerHistoryUseCase;
import com.atlaspay.shared.dto.ApiResponse;
import com.atlaspay.shared.money.Money;
import com.atlaspay.shared.port.out.AccountQueryPort;
import com.atlaspay.shared.port.out.AccountDetailsDto;
import com.atlaspay.shared.util.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/balance")
public class LedgerController {

    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final GetLedgerHistoryUseCase getLedgerHistoryUseCase;
    private final AccountQueryPort accountQueryPort;

    public LedgerController(GetAccountBalanceUseCase getAccountBalanceUseCase, GetLedgerHistoryUseCase getLedgerHistoryUseCase, AccountQueryPort accountQueryPort) {
        this.getAccountBalanceUseCase = getAccountBalanceUseCase;
        this.getLedgerHistoryUseCase = getLedgerHistoryUseCase;
        this.accountQueryPort = accountQueryPort;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BalanceDto>>> getBalances(@RequestHeader("X-Merchant-Id") String integrationStr) {
        Long integration = Long.valueOf(integrationStr);
        List<AccountDetailsDto> accounts = accountQueryPort.findAccountsByIntegration(integration);
        
        List<BalanceDto> balances = accounts.stream().map(acc -> {
            Money balance = getAccountBalanceUseCase.execute(new GetAccountBalanceQuery(acc.accountId(), integration));
            return new BalanceDto(balance.currency().name(), balance.amount());
        }).toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Balances retrieved", balances, null));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<List<LedgerHistoryDto>>> getLedgerHistory(
            @RequestHeader("X-Merchant-Id") String integrationStr,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int perPage) {
        
        Long integration = Long.valueOf(integrationStr);
        PageResult<LedgerHistoryDto> result = getLedgerHistoryUseCase.execute(new GetLedgerHistoryQuery(integration, page, perPage));
        
        ApiResponse.Meta meta = new ApiResponse.Meta(
                result.totalElements(),
                (page - 1) * perPage,
                result.pageSize(),
                result.pageNumber(),
                result.totalPages()
        );
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Balance ledger retrieved", result.content(), meta));
    }
}
