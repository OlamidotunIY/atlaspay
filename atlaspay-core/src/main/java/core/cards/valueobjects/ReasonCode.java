package core.cards.valueobjects;

public record ReasonCode(String code, String description) {

    public ReasonCode {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Reason code cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Reason description cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
