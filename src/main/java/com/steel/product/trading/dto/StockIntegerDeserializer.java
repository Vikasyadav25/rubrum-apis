package com.steel.product.trading.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;

/** Prevent Jackson's default truncation of fractional stock counts and IDs. */
public class StockIntegerDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            throw JsonMappingException.from(parser, "Stock counts, versions and location IDs must be whole numbers.");
        }
        return parser.getIntValue();
    }
}
