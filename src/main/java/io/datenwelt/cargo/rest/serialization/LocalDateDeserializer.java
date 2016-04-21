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
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author job
 */
public class LocalDateDeserializer extends StdDeserializer<LocalDate>{

    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd");
    
    public LocalDateDeserializer() {
        super(LocalDate.class);
    }
    
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken token = p.getCurrentToken();
        if ( token == null ) {
            return null;
        }
        if ( !token.isScalarValue() ) {
            throw new JsonParseException("Date and time values must be scalars in JSON.", p.getCurrentLocation());
        }
        String str = p.getText();
        try {
            LocalDate result = FORMAT.parseLocalDate(str);
            return result;
        } catch (Exception ex) {
            throw new JsonParseException("Invalid value for date/time:" + str, p.getCurrentLocation());
        }       
    }
    
}
