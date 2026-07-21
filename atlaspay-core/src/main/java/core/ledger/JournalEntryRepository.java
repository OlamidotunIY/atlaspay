package core.ledger;

import core.accounts.valueobjects.AccountId;
import core.ledger.valueobjects.JournalEntryId;
import core.shared.Repository;

import java.time.Instant;
import java.util.List;

public interface JournalEntryRepository extends Repository<JournalEntry, JournalEntryId> {
    List<JournalEntry> findByAccountId(AccountId accountId);
    List<JournalEntry> findByAccountId(AccountId accountId, Instant from, Instant to);
}
