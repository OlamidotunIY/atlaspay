package core.access.valueobjects;

public record HashedSecret(String algorithm, String hash) {
    public HashedSecret {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Algorithm cannot be null or blank");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Hash cannot be null or blank");
        }
    }
}
