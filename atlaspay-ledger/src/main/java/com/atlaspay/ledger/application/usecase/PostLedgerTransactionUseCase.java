package com.atlaspay.ledger.application.usecase;

import com.atlaspay.ledger.application.command.PostLedgerTransactionCommand;
import com.atlaspay.ledger.domain.exception.LedgerErrorCode;
import com.atlaspay.ledger.domain.model.LedgerEntry;
import com.atlaspay.ledger.domain.model.LedgerTransaction;
import com.atlaspay.ledger.domain.model.TransactionReference;
import com.atlaspay.ledger.domain.repository.LedgerEntryRepository;
import com.atlaspay.ledger.domain.repository.LedgerTransactionRepository;
import com.atlaspay.ledger.domain.model.BalanceSnapshot;
import com.atlaspay.ledger.domain.model.EntryType;
import com.atlaspay.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlaspay.shared.exception.ConflictException;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.shared.exception.NotFoundException;
import com.atlaspay.shared.port.out.AccountQueryPort;
import com.atlaspay.shared.port.out.AccountDetailsDto;
import com.atlaspay.shared.money.Money;
import com.atlaspay.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PostLedgerTransactionUseCase extends BaseUseCase<PostLedgerTransactionCommand, Void> {

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final AccountQueryPort accountQueryPort;

    public PostLedgerTransactionUseCase(
            LedgerTransactionRepository ledgerTransactionRepository, 
            LedgerEntryRepository ledgerEntryRepository, 
            BalanceSnapshotRepository balanceSnapshotRepository,
            AccountQueryPort accountQueryPort) {
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    @Transactional
    public Void execute(PostLedgerTransactionCommand command) {
        // Idempotency constraint using uniqueness check
        if (ledgerTransactionRepository.existsByReference(command.transactionId(), command.sourceSystem())) {
            throw new ConflictException(LedgerErrorCode.TRANSACTION_ALREADY_POSTED, "Transaction already posted");
        }

        TransactionReference reference = new TransactionReference(command.transactionId(), command.sourceSystem());
        
        Long integrationId;
        try {
            integrationId = Long.valueOf(command.sourceSystem());
        } catch (NumberFormatException e) {
            throw new BusinessRuleException(LedgerErrorCode.UNAUTHORIZED_ACCESS, "Invalid source system format");
        }

        // Validate entries against account invariants
        for (PostLedgerTransactionCommand.EntryCommand entry : command.entries()) {
            AccountDetailsDto account = accountQueryPort.findAccountDetails(entry.accountId())
                    .orElseThrow(() -> new NotFoundException(LedgerErrorCode.ACCOUNT_NOT_FOUND, "Account does not exist"));

            if (!account.integration().equals(integrationId)) {
                throw new BusinessRuleException(LedgerErrorCode.UNAUTHORIZED_ACCESS, "Account does not belong to integration");
            }
            if (account.currency() != entry.currency()) {
                throw new BusinessRuleException(LedgerErrorCode.CURRENCY_MISMATCH, "Entry currency does not match account currency");
            }
        }

        // Lock accounts in a consistent order to prevent deadlocks
        List<Long> accountIds = command.entries().stream()
                .map(PostLedgerTransactionCommand.EntryCommand::accountId)
                .distinct()
                .sorted()
                .toList();

        java.util.Map<Long, BalanceSnapshot> snapshots = new java.util.HashMap<>();
        for (Long accountId : accountIds) {
            BalanceSnapshot snapshot = balanceSnapshotRepository.findLatestByAccountIdForUpdate(accountId)
                    .orElse(new BalanceSnapshot(
                            balanceSnapshotRepository.nextIdentity(),
                            accountId,
                            Money.zero(command.entries().stream().filter(e -> e.accountId().equals(accountId)).findFirst().get().currency()),
                            0L,
                            ZonedDateTime.now()
                    ));
            snapshots.put(accountId, snapshot);
        }

        // Map entries and compute running balances
        List<LedgerEntry> entries = command.entries().stream().map(entry -> {
            BalanceSnapshot snapshot = snapshots.get(entry.accountId());
            Money currentBalance = snapshot.getBalance();
            Money newBalance = entry.type() == EntryType.CREDIT 
                    ? currentBalance.add(Money.of(entry.amount(), entry.currency())) 
                    : currentBalance.subtract(Money.of(entry.amount(), entry.currency()));
            
            Long entryId = ledgerEntryRepository.nextIdentity();
            
            // Update snapshot in memory
            snapshot.updateBalance(newBalance, entryId);

            return new LedgerEntry(
                entryId,
                entry.accountId(),
                Money.of(entry.amount(), entry.currency()),
                entry.type(),
                reference,
                entry.description(),
                newBalance,
                ZonedDateTime.now()
            );
        }).toList();

        // Instantiate Aggregate Root which enforces Double-Entry balancing constraints
        LedgerTransaction transaction = new LedgerTransaction(ledgerTransactionRepository.nextIdentity(), reference, entries, ZonedDateTime.now());

        ledgerTransactionRepository.save(transaction);
        
        // Persist all updated snapshots
        for (BalanceSnapshot snapshot : snapshots.values()) {
            balanceSnapshotRepository.save(snapshot);
        }

        return null;
    }
}
