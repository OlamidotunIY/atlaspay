package com.atlaspay.shared.money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.RoundingMode;

public class MoneySerializer extends JsonSerializer<Money> {
    @Override
    public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        // Writing amount as a plain string preserves exact precision in JSON parsers
        gen.writeStringField("amount", value.amount().setScale(4, RoundingMode.HALF_EVEN).toPlainString());
        gen.writeStringField("currency", value.currency().name());
        gen.writeEndObject();
    }
}
