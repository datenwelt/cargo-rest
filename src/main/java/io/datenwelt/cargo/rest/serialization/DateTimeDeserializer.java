/*
 * Copyright 2016 job.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datenwelt.cargo.rest.serialization;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

/**
 *
 * @author job
 */
public class DateTimeDeserializer extends StdDeserializer<DateTime> {

    private final DateTimeFormatter FORMAT;

    public DateTimeDeserializer() {
        super(Date.class);
        DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("YYYY-MM-dd'T'hh:mm:ss.SSSZ").getParser(),
            DateTimeFormat.forPattern("YYYY-MM-dd'T'hh:mm:ssZ").getParser()
        };
        FORMAT = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
    }

    @Override
    public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        if (token == null) {
            return null;
        }
        if (!token.isScalarValue()) {
            throw new JsonParseException("Date and time values must be scalars in JSON.", p.getCurrentLocation());
        }
        String str = p.getText();
        try {
            DateTime dt = FORMAT.parseDateTime(str);
            return dt;
        } catch (Exception ex) {
            throw new JsonParseException("Invalid value for date/time:" + str, p.getCurrentLocation());
        }
    }

}
