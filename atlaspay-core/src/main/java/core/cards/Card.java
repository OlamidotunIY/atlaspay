package core.cards;

import core.accounts.valueobjects.AccountId;
import core.cards.events.CardActivated;
import core.cards.events.CardBlocked;
import core.cards.events.CardIssued;
import core.cards.valueobjects.CardId;
import core.cards.valueobjects.CardStatus;
import core.cards.valueobjects.CardToken;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

public final class Card extends AggregateRoot<CardId> {
    private final CardToken token;                  // tokenized PAN surrogate, never raw PAN
    private final AccountId linkedAccountId;
    private CardStatus status;                        // ISSUED, ACTIVE, BLOCKED, EXPIRED
    private final YearMonth expiry;

    public Card(CardId cardId, CardToken token, AccountId linkedAccountId, YearMonth expiry) {
        super(cardId);
        if (expiry == null) {
            throw new IllegalArgumentException("Expiry date cannot be null");
        }
        if (!expiry.isAfter(YearMonth.now())) {
            throw new IllegalArgumentException("Expiry must be in the future");
        }
        this.token = token;
        this.linkedAccountId = linkedAccountId;
        this.expiry = expiry;
        this.status = CardStatus.ISSUED;
        register(new CardIssued(UUID.randomUUID(), Instant.now(), cardId, linkedAccountId));
    }

    public void activate() {
        if (status == CardStatus.ISSUED) {
            status = CardStatus.ACTIVE;
            register(new CardActivated(UUID.randomUUID(), Instant.now(), id()));
        } else {
            throw new IllegalStateException("Card can only be activated from ISSUED status.");
        }
    }

    public void block(String reason) {
        if (status == CardStatus.ACTIVE) {
            status = CardStatus.BLOCKED;
            register(new CardBlocked(UUID.randomUUID(), Instant.now(), id(), reason));
        } else {
            throw new IllegalStateException("Card can only be blocked from ACTIVE status.");
        }
    }

    public CardStatus status() {
        return status;
    }
}
