package core.identity.valueobject;

public record KycCheckResult(String checkName, boolean passed, String detail) {

    public KycCheckResult {
        if (checkName == null || checkName.isBlank()) {
            throw new IllegalArgumentException("checkName cannot be null or blank");
        }
        if (detail == null) {
            throw new IllegalArgumentException("detail cannot be null");
        }
    }
}
