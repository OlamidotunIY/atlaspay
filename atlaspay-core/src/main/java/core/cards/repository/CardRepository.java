package core.cards.repository;

import core.accounts.valueobjects.AccountId;
import core.cards.entities.Card;
import core.cards.valueobjects.CardId;
import core.shared.Repository;

import java.util.List;

public interface CardRepository extends Repository<Card, CardId> {
    List<Card> findByAccountId(AccountId accountId); // cards linked to an account
}