package core.identity.valueobject;

import java.time.LocalDate;

public record PersonalDetails(String fullName, LocalDate dateOfBirth, Address address) {

    public PersonalDetails {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
    }
}
