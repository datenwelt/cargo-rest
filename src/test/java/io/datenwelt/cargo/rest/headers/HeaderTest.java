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

import java.util.List;
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
public class HeaderTest {

    public HeaderTest() {
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
    public void testNormalizeName1() {
        String input = "content-type";
        String expected = "Content-Type";
        String actual = Header.normalizeName(input);
        assertEquals("Normalized string for " + input, expected, actual);
    }

    @Test
    public void testNormalizeName2() {
        String input = "lengTH";
        String expected = "Length";
        String actual = Header.normalizeName(input);
        assertEquals("Normalized string for " + input, expected, actual);
    }

    @Test
    public void testNormalizeName3() {
        String input = "t";
        String expected = "T";
        String actual = Header.normalizeName(input);
        assertEquals("Normalized string for " + input, expected, actual);
    }

    @Test
    public void testParse() {
        String input = "de_DE;0.5, *";
        String[] expected = new String[]{"de_DE;0.5", "*"};
        Header header = Header.create("Accept-Language", input);
        List<String> actual = header.asList();
        for (int idx = 0; idx < expected.length; idx++) {
            assertEquals("Header value #" + idx, expected[idx], actual.get(idx));
        }
    }

    @Test
    public void testGet() {
        String input = "de_DE;0.5, *";
        String expected = "de_DE;0.5";
        Header header = Header.create("Accept-Language", input);
        String actual = header.get();
        assertEquals("Value returned by get()", expected, actual);
    }
    
    @Test
    public void testAdd() {
        String input = "de_DE;0.5, *";
        String expected = "test1";
        Header header = Header.create("Accept-Language", input);
        header.add("test1");
        List<String> values = header.asList();
        String actual = values.get(2);
        assertEquals("Value returned after add()", expected, actual);
    }
    
    @Test
    public void testCombined() {
        Header header = new Header("Accept-Language");
        header.add("de_DE;0.5");
        header.add("fr_FR;0.3");
        header.add("*");
        String actual = header.combined();
        String expected = "de_DE;0.5, fr_FR;0.3, *";
        
        assertEquals("Value returned by combined()", expected, actual);
    }
    

}
