package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.dto.LedgerHistoryDto;
import com.atlaspay.ledger.application.query.GetLedgerHistoryQuery;
import com.atlaspay.ledger.domain.model.LedgerEntry;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.shared.port.out.AccountQueryPort;
import com.atlaspay.shared.port.out.AccountDetailsDto;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.shared.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GetLedgerHistoryUseCase extends BaseUseCase<GetLedgerHistoryQuery, PageResult<LedgerHistoryDto>> {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountQueryPort accountQueryPort;

    public GetLedgerHistoryUseCase(LedgerEntryRepository ledgerEntryRepository, AccountQueryPort accountQueryPort) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public PageResult<LedgerHistoryDto> execute(GetLedgerHistoryQuery query) {
        List<AccountDetailsDto> accounts = accountQueryPort.findAccountsByIntegration(query.integration());
        
        if (accounts.isEmpty()) {
            return new PageResult<>(List.of(), query.page(), query.perPage(), 0L, 0);
        }

        List<Long> accountIds = accounts.stream().map(AccountDetailsDto::accountId).toList();

        PageResult<LedgerEntry> entryPage = ledgerEntryRepository.findByAccountIds(accountIds, query.page(), query.perPage());

        List<LedgerHistoryDto> dtos = entryPage.content().stream().map(entry -> {
            BigDecimal difference = entry.getType() == EntryType.CREDIT 
                    ? entry.getAmount().amount() 
                    : entry.getAmount().amount().negate();

            return new LedgerHistoryDto(
                    query.integration(),
                    "test", // Domain logic not strictly defined, matching Paystack's "test" or "live"
                    entry.getRunningBalance() != null ? entry.getRunningBalance().amount() : BigDecimal.ZERO,
                    entry.getAmount().currency().name(),
                    difference,
                    entry.getDescription(),
                    entry.getTransactionReference() != null ? entry.getTransactionReference().sourceSystem() : "",
                    entry.getTransactionReference() != null ? entry.getTransactionReference().transactionId() : "",
                    entry.getId(),
                    entry.getCreatedAt(),
                    entry.getCreatedAt() // Assuming immutable ledger entries
            );
        }).toList();

        return new PageResult<>(
                dtos,
                entryPage.pageNumber(),
                entryPage.pageSize(),
                entryPage.totalElements(),
                entryPage.totalPages()
        );
    }
}
