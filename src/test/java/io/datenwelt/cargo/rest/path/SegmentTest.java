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
package io.datenwelt.cargo.rest.path;

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
public class SegmentTest {
    
    public SegmentTest() {
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
    public void testParseEmptyString() {
        Segment segment = Segment.parse("");
        assertEquals("/", segment.toString());
    }

    @Test
    public void testParseNullString() {
        Segment segment = Segment.parse(null);
        assertEquals("/", segment.toString());
    }

    @Test
    public void testParseRootSegment() {
        Segment segment = Segment.parse("/");
        assertEquals("/", segment.toString());
    }
    
    @Test
    public void testParseValidCharsOnly() {
        Segment segment = Segment.parse(Segment.VALID_CHARS);
        assertEquals("/" + Segment.VALID_CHARS, segment.toString());
        segment = Segment.parse("/" + Segment.VALID_CHARS);
        assertEquals("/" + Segment.VALID_CHARS, segment.toString());
    }
    
    @Test
    public void testParseSomeInvalidChars() {
        Segment segment = Segment.parse("/abcdefg/%");
        assertEquals("/abcdefg%2F%25", segment.toString());
    }

    @Test
    public void testParseWithPctEncoded() {
        Segment segment = Segment.parse("/abcdefg/%2F");
        assertEquals("/abcdefg%2F%2F", segment.toString());
        segment = Segment.parse("/abcdefg/%2f");
        assertEquals("/abcdefg%2F%2F", segment.toString());
    }
    
    @Test
    public void testSplitEmptyString() {
        Segment[] segments = Segment.parseSegments("");
        assertEquals(1, segments.length);
        assertEquals(new Segment("/"), segments[0]);
    }
    
    @Test
    public void testSplitRootPath() {
        Segment[] segments = Segment.parseSegments("/");
        assertEquals(1, segments.length);
        assertEquals(new Segment("/"), segments[0]);
    }

    @Test
    public void testSplitAbsolutePath1() {
        Segment[] segments = Segment.parseSegments("/abc/def/");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
    }

    @Test
    public void testSplitAbsolutePath2() {
        Segment[] segments = Segment.parseSegments("/abc/def");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
    }
    
    @Test
    public void testSplitRelativePath1() {
        Segment[] segments = Segment.parseSegments("abc/def/");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
    }

    @Test
    public void testSplitRelativePath2() {
        Segment[] segments = Segment.parseSegments("abc/def");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
    }

    @Test
    public void testSplitDoubleSlash1() {
        Segment[] segments = Segment.parseSegments("//abc/def");
        assertEquals(3, segments.length);
        int pos = 0;
        assertEquals(new Segment("/"), segments[pos++]);
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
    }

    @Test
    public void testSplitDoubleSlash2() {
        Segment[] segments = Segment.parseSegments("/abc//def");
        assertEquals(3, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
    }

    @Test
    public void testSplitDoubleSlash3() {
        Segment[] segments = Segment.parseSegments("/abc/def//");
        assertEquals(3, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/def"), segments[pos++]);
        assertEquals(new Segment("/"), segments[pos++]);
    }
    
    @Test
    public void testNormalization() {
        Segment[] segments = Segment.parseSegments("/abc/./defg/../");
        assertEquals(4, segments.length);
        int pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
        assertEquals(new Segment("/."), segments[pos++]);
        assertEquals(new Segment("/defg"), segments[pos++]);
        assertEquals(new Segment("/.."), segments[pos++]);
        segments = Segment.normalize(segments);
        assertEquals(1, segments.length);
        pos = 0;
        assertEquals(new Segment("/abc"), segments[pos++]);
    }

    @Test
    public void testScannerWithSingleSegment() {
        Segment.Scanner scanner = new Segment.Scanner("/abcdefg");
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/abcdefg", scanner.next());
        assertFalse("Scanner has finished after all segments are consumed", scanner.hasNext());
    }

        @Test
    public void testScannerWithMultipleSegment() {
        Segment.Scanner scanner = new Segment.Scanner("/abcdefg/hijklmn/opqrstuv");
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/abcdefg", scanner.next());
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/hijklmn", scanner.next());
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/opqrstuv", scanner.next());
        assertFalse("Scanner has finished after all segments are consumed", scanner.hasNext());
    }

    @Test
    public void testScannerWithRootSegment() {
        Segment.Scanner scanner = new Segment.Scanner("/");
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/", scanner.next());
        assertFalse("Scanner has finished after all segments are consumed", scanner.hasNext());
    }

@Test
    public void testScannerWithTrailingSlashSegment() {
        Segment.Scanner scanner = new Segment.Scanner("/abcdefg//");
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/abcdefg", scanner.next());
        assertTrue("Scanner has more segments", scanner.hasNext());
        assertEquals("Scanner returns next segment", "/", scanner.next());
        assertFalse("Scanner has finished after all segments are consumed", scanner.hasNext());
    }    
}
