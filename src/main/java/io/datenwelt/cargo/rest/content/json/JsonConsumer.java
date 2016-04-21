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
package io.datenwelt.cargo.rest.content.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datenwelt.cargo.rest.Request;
import io.datenwelt.cargo.rest.content.ContentConsumer;
import io.datenwelt.cargo.rest.response.APIException;
import io.datenwelt.cargo.rest.response.BadRequest;
import io.datenwelt.cargo.rest.serialization.Json;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author job
 */
public class JsonConsumer implements ContentConsumer {
   
    @Override
    public <T> T consume(InputStream inputStream, Request request, Class<? extends T> targetClass) throws APIException, IOException {
        ObjectMapper om = Json.getDefaultObjectMapper();
        try {
           return om.readValue(inputStream, targetClass);
        } catch (JsonParseException | JsonMappingException ex) {
            throw new APIException(new BadRequest());
        }
    }

    
}
