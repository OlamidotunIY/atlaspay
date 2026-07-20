package core.identity.valueobject;

public record Address(String line1, String line2, String city, String postalCode, String countryCode) {

    public Address {
        if (line1 == null || line1.isBlank()) {
            throw new IllegalArgumentException("Address line 1 cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code cannot be null or blank");
        }
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("Country code cannot be null or blank");
        }
    }
}
