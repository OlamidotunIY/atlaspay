package core.cards.repository;

import core.cards.entities.Dispute;
import core.cards.valueobjects.CardId;
import core.cards.valueobjects.DisputeId;
import core.shared.Repository;

import java.util.List;

public interface DisputeRepository extends Repository<Dispute, DisputeId> {
    List<Dispute> findByCardId(CardId cardId); // dispute history for a card
}
