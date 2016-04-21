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
public class ContentTypeHeaderTest {

    public ContentTypeHeaderTest() {
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
    public void setterTest1() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json");
        assertEquals("application/json", cth.mediaType());
        assertFalse(cth.charset().isPresent());
    }

    @Test
    public void setterTest2() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=UTF-8");
        assertEquals("application/json", cth.mediaType());
        assertTrue(cth.charset().isPresent());
    }

    @Test
    public void setterTest3() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; someAdditionalParam=1;charset=utf-8");
        assertEquals("application/json", cth.mediaType());
        assertTrue(cth.charset().isPresent());
    }

    @Test
    public void setterTest4() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json;charset=utf-8; someAdditionalParam=1");
        assertEquals("application/json", cth.mediaType());
        assertTrue(cth.charset().isPresent());
    }

    @Test
    public void changeTest1() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json;charset=utf-8");
        cth.change("text/html");
        assertEquals("text/html", cth.mediaType());
        assertFalse(cth.charset().isPresent());
        assertEquals("text/html", cth.normalized());
    }

    @Test
    public void changeTest2() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json;charset=utf-8");
        cth.change("Text/Html", "ISO-8859-1");
        assertEquals("text/html", cth.mediaType());
        assertTrue(cth.charset().isPresent());
        assertEquals("iso-8859-1", cth.charset().get());
        assertEquals("text/html; charset=iso-8859-1", cth.normalized());
    }

    @Test
    public void matchesTest1() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=utf-8");
        cth.matches("*/*");
        assertTrue(cth.matches("*/*"));
    }

    @Test
    public void matchesTest2() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=utf-8");
        assertTrue(cth.matches("application/*"));
    }

    @Test
    public void matchesTest3() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=utf-8");
        assertTrue(cth.matches("application/json"));
    }

    @Test
    public void matchesTest4() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=utf-8");
        assertFalse(cth.matches("application/xml"));
    }

    @Test
    public void matchesTest5() {
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=utf-8");
        assertFalse(cth.matches("text/json"));
    }
    @Test
    public void matchesTest6() {
        // TODO: Shouldn't this fail with an IllegalArgumentException?
        ContentTypeHeader cth = new ContentTypeHeader();
        cth.set("application/json; charset=utf-8");
        assertTrue(cth.matches("*/json"));
    }

    
}
