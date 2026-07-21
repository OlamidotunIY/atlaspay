package core.transfers;

import core.shared.Result;

import java.util.List;

public interface TransferValidationService {
    Result<Void, List<String>> validate(Transfer transfer); // pre-posting checks before saga proceeds: account-status checks + delegates limit checks to LimitEvaluationService
}
