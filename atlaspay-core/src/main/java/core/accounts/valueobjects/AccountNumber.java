package core.accounts.valueobjects;

import java.util.Objects;

public record AccountNumber(String value) {

    // NUBAN format: [3-digit bank code][9-digit serial][1 check digit] = 10 digits total.
    // Bank code is fixed for this platform (Zenith Bank, per CBN clearing system codes).
    private static final String BANK_CODE = "057";
    private static final int SERIAL_LENGTH = 9;
    // CBN check-digit weights applied to bankCode(3 digits) + serial(9 digits), in order.
    private static final int[] WEIGHTS = {3, 7, 3, 3, 7, 3, 3, 7, 3, 3, 7, 3};

    public AccountNumber {
        Objects.requireNonNull(value, "value must not be null");

        if (!value.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "NUBAN account number must be exactly 10 digits: " + value);
        }

        if (!isValidCheckDigit(value)) {
            throw new IllegalArgumentException("Invalid NUBAN check digit: " + value);
        }
    }

    /**
     * Builds a valid AccountNumber from a 9-digit serial, computing its check digit.
     */
    public static AccountNumber fromSerial(String serial) {
        if (serial == null || !serial.matches("\\d{" + SERIAL_LENGTH + "}")) {
            throw new IllegalArgumentException(
                    "Serial must be exactly " + SERIAL_LENGTH + " digits: " + serial);
        }
        return new AccountNumber(serial + computeCheckDigit(serial));
    }

    private static boolean isValidCheckDigit(String nuban) {
        String serial = nuban.substring(0, SERIAL_LENGTH);
        int expected = computeCheckDigit(serial);
        int actual = nuban.charAt(nuban.length() - 1) - '0';
        return expected == actual;
    }

    // CBN NUBAN Check Digit Algorithm:
    // 1. weighted sum of bankCode(3) + serial(9) digits using weights 3,7,3 repeated
    // 2. remainder = sum mod 10
    // 3. checkDigit = 10 - remainder
    // 4. if checkDigit == 10, use 0
    private static int computeCheckDigit(String serial) {
        String combined = BANK_CODE + serial; // 12 digits
        int sum = 0;
        for (int i = 0; i < combined.length(); i++) {
            sum += (combined.charAt(i) - '0') * WEIGHTS[i];
        }
        int checkDigit = 10 - (sum % 10);
        return checkDigit == 10 ? 0 : checkDigit;
    }
}
