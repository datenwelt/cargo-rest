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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datenwelt.cargo.rest.serialization.examples.SampleEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author job
 */
public class JsonTest {
    
    private static ObjectMapper defaultObjectMapper;
    
    public JsonTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        defaultObjectMapper = Json.getDefaultObjectMapper();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void serializeDateTimeTest() {
        DateTime value = new DateTime(2016, 12, 3, 4, 56, 7, 890, DateTimeZone.forOffsetHours(2));
        String expected = "2016-12-03T04:56:07.890+0200";
        JsonNode node = defaultObjectMapper.valueToTree(value);
        assertTrue(node.isTextual());
        assertEquals(expected, node.asText());
    }

    @Test
    public void serializeLocalDateTest() {
        LocalDate value = new LocalDate(2016, 12, 3);
        String expected = "2016-12-03";
        JsonNode node = defaultObjectMapper.valueToTree(value);
        assertTrue(node.isTextual());
        assertEquals(expected, node.asText());
    }
    
    @Test
    public void serializeLocalTimeTest1() {
        LocalTime value = new LocalTime(4, 56, 7, 890);
        String expected = "04:56:07.890";
        JsonNode node = defaultObjectMapper.valueToTree(value);
        assertTrue(node.isTextual());
        assertEquals(expected, node.asText());
    }

    @Test
    public void serializeLocalTimeTest2() {
        LocalTime value = new LocalTime(4, 56, 7, 0);
        String expected = "04:56:07";
        JsonNode node = defaultObjectMapper.valueToTree(value);
        assertTrue(node.isTextual());
        assertEquals(expected, node.asText());
    }
    
    @Test
    public void deserializeDateTime() throws IOException {
        String jsonString = "\"2016-05-04T03:02:01.890+0200\"";
        DateTime actual = defaultObjectMapper.readValue(jsonString, DateTime.class);
        DateTime expected = new DateTime(2016, 5, 4, 3, 2, 1, 890, DateTimeZone.forOffsetHours(2));
        assertTrue(expected.isEqual(actual));
    }
    
    @Test
    public void deserializeLocalDate() throws IOException {
        String jsonString = "\"2016-05-04\"";
        LocalDate actual = defaultObjectMapper.readValue(jsonString, LocalDate.class);
        LocalDate expected = new LocalDate(2016, 5, 4);
        assertTrue(expected.isEqual(actual));
    }
    
    @Test
    public void deserializeLocalTime1() throws IOException {
        String jsonString = "\"01:23:45.678\"";
        LocalTime actual = defaultObjectMapper.readValue(jsonString, LocalTime.class);
        LocalTime expected = new LocalTime(1, 23, 45, 678);
        assertTrue(expected.isEqual(actual));
    }
    
    @Test
    public void deserializeLocalTime2() throws IOException {
        String jsonString = "\"01:23:45\"";
        LocalTime actual = defaultObjectMapper.readValue(jsonString, LocalTime.class);
        LocalTime expected = new LocalTime(1, 23, 45, 0);
        assertTrue(expected.isEqual(actual));
    }
    
    @Test
    public void deserializerTest() throws IOException {
        String uri = "http://www.xyz.de/123456";
        ByteArrayInputStream bis = new ByteArrayInputStream(("\"" + uri + "\"").getBytes());
        SampleEntity sample = new SampleEntity();
        Json.deserialize(bis, sample);
        assertEquals(uri, sample.uri.toString());
    }
    
    @Test
    public void serializerTest() throws IOException {
        String uri = "http://www.xyz.de/123456";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SampleEntity sample = new SampleEntity();
        sample.uri = URI.create(uri);
        Json.serialize(bos, sample);
        String str = bos.toString();
        String expected = "\"" + uri + "\"";
        assertEquals(expected, str);
    }
    
}
