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
package io.datenwelt.cargo.rest.content.yaml;

import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.examples.TestPerson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class YamlProducerTest {
    
    public YamlProducerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
    public void testYamlSerialization() throws IOException {
        YamlProducer prod = new YamlProducer();
        TestPerson person = TestPerson.sample();
        Response response = new Response(200, person);
        prod.prepare(response);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        prod.produce(buffer);
        String actual = new String(buffer.toByteArray());
        String expected = "---\n" +
            "firstname: \"Test\"\n" +
            "lastname: \"Testmann\"\n" +
            "birthday: \"1970-10-23\"\n";
        assertEquals("YamlProducer creates YAML output", expected, actual);
    }

    
}
