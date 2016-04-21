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
package io.datenwelt.cargo.rest.query;

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
public class QueryTest {
    
    public QueryTest() {
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
    public void testParseQueryString1() {
        String input = "abc=xyz";
        List<Query> queries = Query.parseQueryString(input);
        assertEquals("Number of returned queries", 1, queries.size());
        assertEquals("Parsed key", "abc", queries.get(0).getKey());
        assertTrue("Value present", queries.get(0).getValue().isPresent());
        assertEquals("Parsed value", "xyz", queries.get(0).getValue().get());
    }
    
    @Test
    public void testParseQueryString2() {
        String input = "abc=";
        List<Query> queries = Query.parseQueryString(input);
        assertEquals("Number of returned queries", 1, queries.size());
        assertEquals("Parsed key", "abc", queries.get(0).getKey());
        assertFalse("Value present", queries.get(0).getValue().isPresent());
    }
    
    @Test
    public void testParseQueryString3() {
        String input = "abc";
        List<Query> queries = Query.parseQueryString(input);
        assertEquals("Number of returned queries", 1, queries.size());
        assertEquals("Parsed key", "abc", queries.get(0).getKey());
        assertFalse("Value present", queries.get(0).getValue().isPresent());
    }

    @Test
    public void testParseQueryString4() {
        String input = "";
        List<Query> queries = Query.parseQueryString(input);
        assertEquals("Number of returned queries", 0, queries.size());
    }

    @Test
    public void testParseQueryString5() {
        String input = "abc=xyz=";
        List<Query> queries = Query.parseQueryString(input);
        assertEquals("Number of returned queries", 1, queries.size());
        assertEquals("Parsed key", "abc", queries.get(0).getKey());
        assertTrue("Value present", queries.get(0).getValue().isPresent());
        assertEquals("Parsed value", "xyz=", queries.get(0).getValue().get());
    }
 
    @Test
    public void testParseQueryString6() {
        String input = "abc=xyz&def=xyz%25";
        List<Query> queries = Query.parseQueryString(input);
        assertEquals("Number of returned queries", 2, queries.size());
        assertEquals("Parsed key #1", "abc", queries.get(0).getKey());
        assertTrue("Value #1 present", queries.get(0).getValue().isPresent());
        assertEquals("Parsed value #1", "xyz", queries.get(0).getValue().get());
        assertEquals("Parsed key #2", "def", queries.get(1).getKey());
        assertTrue("Value #2 present", queries.get(1).getValue().isPresent());
        assertEquals("Parsed value #2", "xyz%", queries.get(1).getValue().get());
    }
    
}
