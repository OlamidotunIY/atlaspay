package core.transfers.valueobjects;

public enum TransferSagaState {
    INITIATED,
    VALIDATED,
    POSTED,
    COMPLETED,
    REVERSED,
    FAILED
}
