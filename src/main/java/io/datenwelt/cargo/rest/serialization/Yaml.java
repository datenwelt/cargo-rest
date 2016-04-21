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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author job
 */
public class Yaml {
    
    private static YAMLMapper defaultObjectMapper;
    
    public static ObjectMapper getDefaultObjectMapper() {
        if (defaultObjectMapper == null) {
            defaultObjectMapper = new YAMLMapper();
            Json.configureObjectMapper(defaultObjectMapper);
        }
        return defaultObjectMapper;
    }
    
    public static void serialize(OutputStream os, Object input) throws IOException, JsonGenerationException {
        ObjectMapper om = getDefaultObjectMapper();
        if (JsonSerializable.class.isInstance(input)) {
            JsonSerializable serializable = (JsonSerializable) input;
            serializable.writeJson(os);
        } else {
            om.writeValue(os, input);
        }
    }
    
    public static void deserialize(InputStream is, Object target) throws IOException, JsonParseException {
        ObjectMapper om = getDefaultObjectMapper();
        if (JsonDeserializable.class.isInstance(target)) {
            JsonDeserializable deserializable = (JsonDeserializable) target;
            deserializable.readJson(is);
        } else {
            ObjectReader reader = om.readerForUpdating(target);
            reader.readValue(is);
        }
    }
    
    public static String toYaml(Object obj) {
        if (obj == null) {
            return "null";
        }
        ObjectMapper om = getDefaultObjectMapper();
        try {
            return om.writeValueAsString(obj);
        } catch (Exception ex) {
            try {
                return om.writeValueAsString(obj.toString());
            } catch (Exception ex1) {
                return "<SERFAIL: " + ex1.getMessage() + ">";
            }
        }
    }
}
