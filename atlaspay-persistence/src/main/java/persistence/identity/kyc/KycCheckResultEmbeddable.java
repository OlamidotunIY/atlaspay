package persistence.identity.kyc;

import core.identity.valueobject.KycCheckResult;
import jakarta.persistence.Embeddable;

@Embeddable
public class KycCheckResultEmbeddable {
    private String checkName;
    private boolean passed;
    private String detail;

    static KycCheckResultEmbeddable from(KycCheckResult checkResult) {
        KycCheckResultEmbeddable embeddable = new KycCheckResultEmbeddable();
        embeddable.checkName = checkResult.checkName();
        embeddable.passed = checkResult.passed();
        embeddable.detail = checkResult.detail();
        return embeddable;
    }

    public String getCheckName() {
        return checkName;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getDetail() {
        return detail;
    }
}
