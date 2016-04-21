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
package io.datenwelt.cargo.rest.content.xml;

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
public class XmlProducerTest {
    
    public XmlProducerTest() {
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
    public void testXmlSerialization() throws IOException {
        XmlProducer prod = new XmlProducer();
        TestPerson person = TestPerson.sample();
        Response response = new Response(200, person);
        prod.prepare(response);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        prod.produce(buffer);
        String actual = new String(buffer.toByteArray());
        String expected = "<?xml version='1.1' encoding='UTF-8'?><TestPerson><firstname>Test</firstname><lastname>Testmann</lastname><birthday>1970-10-23</birthday></TestPerson>";
        assertEquals("XmlProducer creates XML output", expected, actual);
    }


    
}
