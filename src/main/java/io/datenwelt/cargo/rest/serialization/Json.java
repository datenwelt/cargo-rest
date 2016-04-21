/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.datenwelt.cargo.rest.serialization;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 *
 * @author job
 */
public class Json {

    private static ObjectMapper defaultObjectMapper;

    public static ObjectMapper getDefaultObjectMapper() {
        if (defaultObjectMapper == null) {
            defaultObjectMapper = new ObjectMapper();
            configureObjectMapper(defaultObjectMapper);
        }
        return defaultObjectMapper;
    }

    public static void configureObjectMapper(ObjectMapper om) {
        om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule("datenwelt-serialization-module", new Version(1, 0, 0, ""));
        module.addSerializer(new DateSerializer());
        module.addSerializer(new DateTimeSerializer());
        module.addSerializer(new LocalDateSerializer());
        module.addSerializer(new LocalTimeSerializer());
        module.addDeserializer(DateTime.class, new DateTimeDeserializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        om.registerModule(module);
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

    public static String toJson(Object obj) {
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
