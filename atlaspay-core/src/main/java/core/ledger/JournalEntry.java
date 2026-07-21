package core.ledger;

import core.ledger.events.JournalEntryPosted;
import core.ledger.valueobjects.JournalEntryId;
import core.ledger.valueobjects.LedgerLine;
import core.ledger.valueobjects.Money;
import core.ledger.valueobjects.TransactionReference;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JournalEntry extends AggregateRoot<JournalEntryId> {
    private final Instant postedAt;                  // append-only: immutable once constructed
    private final TransactionReference reference;     // correlates to originating transfer/payment
    private final List<LedgerLine> lines;               // unmodifiable, at least 2 lines

    public JournalEntry(JournalEntryId id, TransactionReference reference, List<LedgerLine> lines) {
        super(id);
        Objects.requireNonNull(reference, "reference must not be null");
        Objects.requireNonNull(lines, "lines must not be null");

        if (lines.size() < 2) {
            throw new IllegalArgumentException("A JournalEntry must contain at least 2 lines");
        }

        this.reference = reference;
        this.lines = List.copyOf(lines); // defensive copy, unmodifiable
        this.postedAt = Instant.now();

        if (!isBalanced()) {
            throw new IllegalArgumentException("JournalEntry lines must balance to zero (double-entry)");
        }

        register(new JournalEntryPosted(UUID.randomUUID(), postedAt, this.id(), reference));
    }


    public List<LedgerLine> lines() {
        return lines;
    }

    public boolean isBalanced() {
        Money net = null;

        for (LedgerLine line : lines) {
            Money signed = switch (line.direction()) {
                case DEBIT -> line.amount();
                case CREDIT -> line.amount().negate();
            };
            net = (net == null) ? signed : net.add(signed);
        }

        return net != null && net.isZero();
    }
}
