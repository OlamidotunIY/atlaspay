package core.identity.valueobject;

public record RegistrationNumber(String value) {
    private static final String PATTERN = "^[A-Z0-9-]{6,20}$";

    public RegistrationNumber {
        if (value == null || !value.matches(PATTERN)) {
            throw new IllegalArgumentException("Invalid registration number");
        }
    }
}
