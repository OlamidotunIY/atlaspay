package core.ledger;

import core.ledger.valueobjects.LedgerLine;
import core.ledger.valueobjects.TransactionReference;

import java.util.List;

public interface LedgerPostingService {
    JournalEntry post(TransactionReference reference, List<LedgerLine> lines);
}
