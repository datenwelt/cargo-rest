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
package io.datenwelt.cargo.rest.serialization.examples;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datenwelt.cargo.rest.serialization.Json;
import io.datenwelt.cargo.rest.serialization.JsonDeserializable;
import io.datenwelt.cargo.rest.serialization.JsonSerializable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 *
 * @author job
 */
public class SampleEntity implements JsonSerializable, JsonDeserializable {
    
    public URI uri;

    @Override
    public void writeJson(OutputStream os) throws IOException, JsonGenerationException {
        ObjectMapper om = Json.getDefaultObjectMapper();
        om.writeValue(os, uri.toString());
    }

    @Override
    public void readJson(InputStream is) throws IOException, JsonParseException {
        ObjectMapper om = Json.getDefaultObjectMapper();
        String str = om.readValue(is, String.class);
        try {
            uri = URI.create(str);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid URI value: " + str);
        }
    }
    
    
    
    
}
