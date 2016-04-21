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
package io.datenwelt.cargo.rest.utils;

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
public class Rfc2047Test {
    
    public Rfc2047Test() {
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
    public void testEncodeHeader1() {
        String input = "Dies ist ein Test mit spanischen Wörtern: ¡Hola, señor!";
        String expected = input;
        String actual = Rfc2047.encodeHeader(input);
        assertEquals(expected, actual);      
    }

    @Test
    public void testEncodeHeader2() {
        String input = "Dies ist ein Test mit arabischen Wörtern: يوم جيد";
        String expected = "Dies ist ein Test mit arabischen Wörtern: =?UTF-8?Q?=D9=8A=D9=88=D9=85?= =?UTF-8?Q?=D8=AC=D9=8A=D8=AF?=";
        String actual = Rfc2047.encodeHeader(input);
        assertEquals(expected, actual);      
    }

    @Test
    public void testEncodeHeader3() {
        String input = "This is a test with an evil URL: http://www.xyz.de/?=?test123?H?dhdhd?=";
        String expected = "This is a test with an evil URL: http://www.xyz.de/?=?test123?H?dhdhd?=";
        String actual = Rfc2047.encodeHeader(input);
        assertEquals(expected, actual);      
    }
    
    @Test
    public void testDecodeHeader1() {
        String input = "Dies ist ein Test mit spanischen Wörtern: =?UTF-8?Q?=C2=A1Hola,_se=C3=B1or!?=";
        String expected = "Dies ist ein Test mit spanischen Wörtern: ¡Hola, señor!";
        String actual = Rfc2047.decodeHeader(input);
        assertEquals(expected, actual);      
    }

    @Test
    public void testDecodeHeader2() {
        String expected = "Dies ist ein Test mit arabischen Wörtern: يوم جيد";
        String input = "Dies ist ein Test mit arabischen Wörtern: =?UTF-8?Q?=D9=8A=D9=88=D9=85?= =?UTF-8?Q?=D8=AC=D9=8A=D8=AF?=";
        String actual = Rfc2047.decodeHeader(input);
        assertEquals(expected, actual);      
    }

    @Test
    public void testDecodeHeader3() {
        String expected = "This is a test with an evil URL: http://www.xyz.de/?=?test123?H?dhdhd?=";
        String input = "This is a test with an evil URL: http://www.xyz.de/?=?test123?H?dhdhd?=";
        String actual = Rfc2047.decodeHeader(input);
        assertEquals(expected, actual);      
    }
    
    @Test
    public void testDecodeHeader4() {
        // The Q encoded string is valid but not a word by itself and should be skipped.
        String expected = "This is a test with an even more evil URL: http://www.xyz.de/?=?utf-8?Q?dhdhd?=";
        String input = "This is a test with an even more evil URL: http://www.xyz.de/?=?utf-8?Q?dhdhd?=";
        String actual = Rfc2047.decodeHeader(input);
        assertEquals(expected, actual);      
    }
}
