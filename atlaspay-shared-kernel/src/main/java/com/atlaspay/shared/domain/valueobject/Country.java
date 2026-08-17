package com.atlaspay.shared.domain.valueobject;

import com.atlaspay.shared.money.CurrencyCode;
import lombok.Getter;

@Getter
public enum Country {
    NIGERIA(CurrencyCode.NGN),
    KENYA(CurrencyCode.KES),
    USA(CurrencyCode.USD),
    UNITED_KINGDOM(CurrencyCode.GBP);

    private final CurrencyCode defaultCurrency;

    Country(CurrencyCode defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public static Country fromString(String countryStr) {
        if (countryStr == null) return null;
        if (countryStr.equalsIgnoreCase("NG") || countryStr.equalsIgnoreCase("Nigeria")) {
            return Country.NIGERIA;
        }
        if (countryStr.equalsIgnoreCase("KE") || countryStr.equalsIgnoreCase("Kenya")) {
            return Country.KENYA;
        }
        if (countryStr.equalsIgnoreCase("US") || countryStr.equalsIgnoreCase("USA")) {
            return Country.USA;
        }
        if (countryStr.equalsIgnoreCase("UK") || countryStr.equalsIgnoreCase("United Kingdom")) {
            return Country.UNITED_KINGDOM;
        }
        try {
            return Country.valueOf(countryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Or throw a domain exception depending on context
        }
    }
}
