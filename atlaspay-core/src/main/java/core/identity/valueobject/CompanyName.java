package core.identity.valueobject;

public record CompanyName(String value) {
    public CompanyName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Company name must not be blank");
        }
    }
}
