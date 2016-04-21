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
package io.datenwelt.cargo.rest.headers;

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
public class AcceptHeaderTest {
    
    public AcceptHeaderTest() {
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
    public void testHeaderParsing1() {
        Header header = new Header("Accept");
        header.set("image/png,image/*;q=0.8,*/*;q=0.5");
        AcceptHeader accept = new AcceptHeader("Accept");
        accept.add(header);
        assertEquals("Number of values parsed from header", 3, accept.values().size());
        AcceptHeader.Value value;
        value = accept.values().first();
        assertEquals("Media type of accept header value", "image/png", value.getValue());
        assertEquals("Quality factor of accept haeder value", 1.0f, value.getQ(), 0.0f);
        assertEquals("Precedence of accept haeder value", 0, value.getPrecedence());
        value = accept.values().higher(value);
        assertEquals("Media type of accept header value", "image/*", value.getValue());
        assertEquals("Quality factor of accept haeder value", 0.8f, value.getQ(), 0.0f);
        assertEquals("Precedence of accept haeder value", 1, value.getPrecedence());
        value = accept.values().higher(value);
        assertEquals("Media type of accept header value", "*/*", value.getValue());
        assertEquals("Quality factor of accept haeder value", 0.5f, value.getQ(), 0.0f);
        assertEquals("Precedence of accept haeder value", 2, value.getPrecedence());
    }
    
    @Test
    public void testHeaderParsing2() {
        Header header = new Header("Accept-Charset");
        header.set("utf-8; q=1, iso-8859-1; q=0.0");
        AcceptHeader accept = new AcceptHeader("Accept");
        accept.add(header);
        assertEquals("Number of values parsed from header", 2, accept.values().size());
        AcceptHeader.Value value;
        value = accept.values().first();
        assertEquals("Charset of accept header value", "utf-8", value.getValue());
        assertEquals("Quality factor of accept haeder value", 1.0f, value.getQ(), 0.0f);
        assertEquals("Precedence of accept haeder value", 0, value.getPrecedence());
        value = accept.values().higher(value);
        assertEquals("Charset of accept header value", "iso-8859-1", value.getValue());
        assertEquals("Quality factor of accept haeder value", 0.0f, value.getQ(), 0.0f);
        assertEquals("Precedence of accept haeder value", 0, value.getPrecedence());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeaderParsing3() {
        Header header = new Header("Accept-Charset");
        header.set("; q=1, iso-8859-1; q=0.0");
        AcceptHeader accept = new AcceptHeader("Accept");
        accept.add(header);
    }
    
    @Test
    public void testAcceptance1() {
        Header header = new Header("Accept-Charset");
        header.set("utf-8; q=1, iso-8859-1; q=0.0");
        AcceptHeader accept = new AcceptHeader("Accept");
        accept.add(header);
        assertTrue("Accepts UTF-8", accept.accepts("UTF-8"));
        assertFalse("Does not accept ISO-8859-1", accept.accepts("ISO-8859-1"));
    }

    @Test
    public void testAcceptance2() {
        Header header = new Header("Accept-Charset");
        header.set("utf-8; q=1, iso-8859-1; q=0.3, *; q=0");
        AcceptHeader accept = new AcceptHeader("Accept");
        accept.add(header);
        assertTrue("Accepts UTF-8", accept.accepts("UTF-8"));
        assertTrue("Accepts ISO-8859-1", accept.accepts("ISO-8859-1"));
        assertFalse("Does not accept ISO-8859-5", accept.accepts("ISO-8859-5"));
    }

    
}
