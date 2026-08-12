package com.atlaspay.shared.money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;

public class MoneyDeserializer extends JsonDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String amountStr = node.get("amount").asText();
        String currencyStr = node.get("currency").asText();
        return Money.of(new BigDecimal(amountStr), CurrencyCode.valueOf(currencyStr));
    }
}
