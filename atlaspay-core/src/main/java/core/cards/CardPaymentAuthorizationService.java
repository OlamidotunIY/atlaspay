package core.cards;

import core.cards.valueobjects.AuthorizationDecision;
import core.ledger.valueobjects.Money;
import core.shared.Result;

import java.util.List;

public interface CardPaymentAuthorizationService {
    Result<AuthorizationDecision, List<String>> authorize(Card card, Money amount); // checks Card.status() itself, then delegates limit checks to Limits' LimitEvaluationService
}
