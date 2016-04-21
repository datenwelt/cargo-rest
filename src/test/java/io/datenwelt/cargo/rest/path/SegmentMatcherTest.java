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

import io.datenwelt.cargo.rest.path.errors.URITemplateMismatchException;
import io.datenwelt.cargo.rest.path.errors.InvalidURITemplateException;
import io.datenwelt.cargo.rest.response.APIException;
import java.util.Iterator;
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
public class SegmentMatcherTest {

    public SegmentMatcherTest() {
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
    public void testParseEmptyTemplate() throws Exception {
        SegmentMatcher template = SegmentMatcher.parse("");
        assertEquals("String representation ", "/", template.toString());
        assertEquals("Compiled pattern ", "\\Q/\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 0, template.getVariableNames().size());
    }

    @Test
    public void testParseNullString() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse(null);
        assertEquals("String representation ", "/", template.toString());
        assertEquals("Compiled pattern ", "\\Q/\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 0, template.getVariableNames().size());
    }

    @Test
    public void testParseRootSegment() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse("/");
        assertEquals("String representation ", "/", template.toString());
        assertEquals("Compiled pattern ", "\\Q/\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 0, template.getVariableNames().size());
    }

    @Test
    public void testParseValidCharsOnly() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse(Segment.VALID_CHARS);
        assertEquals("String representation ", "/" + Segment.VALID_CHARS, template.toString());
        assertEquals("Compiled pattern ", "\\Q/" + Segment.VALID_CHARS + "\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 0, template.getVariableNames().size());
    }

    @Test
    public void testParseSomeInvalidChars() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse("/abcdefg/%");
        assertEquals("String representation ", "/abcdefg%2F%25", template.toString());
        assertEquals("Compiled pattern ", "\\Q/abcdefg%2F%25\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 0, template.getVariableNames().size());
    }

    @Test
    public void testParseWithVariableAtStart() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse("/{var1}-abcdefg");
        assertEquals("String representation ", "/{var1}-abcdefg", template.toString());
        assertEquals("Compiled pattern ", "\\Q/\\E(.+)\\Q-abcdefg\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 1, template.getVariableNames().size());
        int pos = 0;
        assertEquals("Variable name ", "var1", template.getVariableNames().get(pos++));
    }

    @Test
    public void testParseWithVariableAtEnd() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse("/abcdefg-{var1}");
        assertEquals("String representation ", "/abcdefg-{var1}", template.toString());
        assertEquals("Compiled pattern ", "\\Q/abcdefg-\\E(.+)\\Q\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 1, template.getVariableNames().size());
        int pos = 0;
        assertEquals("Variable name ", "var1", template.getVariableNames().get(pos++));
    }

    @Test
    public void testParseWithVariableWithin() throws InvalidURITemplateException {
        SegmentMatcher template = SegmentMatcher.parse("/abc-{var1}-defg");
        assertEquals("String representation ", "/abc-{var1}-defg", template.toString());
        assertEquals("Compiled pattern ", "\\Q/abc-\\E(.+)\\Q-defg\\E", template.getPattern().pattern());
        assertEquals("Number of path variables ", 1, template.getVariableNames().size());
        int pos = 0;
        assertEquals("Variable name ", "var1", template.getVariableNames().get(pos++));
    }

    @Test(expected = InvalidURITemplateException.class)
    public void testParseWithUnclosedBracket() throws InvalidURITemplateException {
        SegmentMatcher.parse("/abc-{var1-defg");
    }

    @Test(expected = InvalidURITemplateException.class)
    public void testParseWithInvalidVariableName() throws InvalidURITemplateException {
        SegmentMatcher.parse("/abc-{var+1}-defg");
    }

    @Test(expected = InvalidURITemplateException.class)
    public void testParseWithUnclosedBracketBeforeSlash() throws InvalidURITemplateException {
        SegmentMatcher.parse("/abc-{var/-defg/");
    }

    @Test
    public void testEqualityWithDifferentDefinitions() throws InvalidURITemplateException {
        SegmentMatcher templ1 = SegmentMatcher.parse("/abc-{var1}/defg/");
        SegmentMatcher templ2 = SegmentMatcher.parse("/abc-{var2}/defg/");
        assertTrue("Templates with equivalent patterns are considered equal.", templ1.equals(templ2));
    }

    @Test
    public void testSplitEmptyString() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("");
        assertEquals(1, segments.length);
        assertEquals(SegmentMatcher.parse("/"), segments[0]);
    }

    @Test
    public void testSplitRootPath() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("/");
        assertEquals(1, segments.length);
        assertEquals(SegmentMatcher.parse("/"), segments[0]);
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
    public void testSplitAbsolutePath2() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("/abc/def");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(SegmentMatcher.parse("/abc"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/def"), segments[pos++]);
    }

    @Test
    public void testSplitRelativePath1() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("abc/def/");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(SegmentMatcher.parse("/abc"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/def"), segments[pos++]);
    }

    @Test
    public void testSplitRelativePath2() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("abc/def");
        assertEquals(2, segments.length);
        int pos = 0;
        assertEquals(SegmentMatcher.parse("/abc"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/def"), segments[pos++]);
    }

    @Test
    public void testSplitDoubleSlash1() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("//abc/def");
        assertEquals(3, segments.length);
        int pos = 0;
        assertEquals(SegmentMatcher.parse("/"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/abc"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/def"), segments[pos++]);
    }

    @Test
    public void testSplitDoubleSlash2() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("/abc//def");
        assertEquals(3, segments.length);
        int pos = 0;
        assertEquals(SegmentMatcher.parse("/abc"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/def"), segments[pos++]);
    }

    @Test
    public void testSplitDoubleSlash3() throws InvalidURITemplateException {
        SegmentMatcher[] segments = SegmentMatcher.parseSegments("/abc/def//");
        assertEquals(3, segments.length);
        int pos = 0;
        assertEquals(SegmentMatcher.parse("/abc"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/def"), segments[pos++]);
        assertEquals(SegmentMatcher.parse("/"), segments[pos++]);
    }

    @Test
    public void testMatchEmptyPath() throws InvalidURITemplateException, URITemplateMismatchException {
        SegmentMatcher template = SegmentMatcher.parse("/");
        List<PathParameter> params = template.match("/");
        assertNotNull("Template matches input", params);
        assertEquals("Number of matched variables", 0, params.size());
    }

    @Test
    public void testMatchSingleVariable() throws InvalidURITemplateException, URITemplateMismatchException, APIException {
        SegmentMatcher template = SegmentMatcher.parse("/xyz-{var1}");
        List<PathParameter> params = template.match("/xyz-1234");
        assertNotNull("Template matches input", params);
        assertEquals("Number of matched variables", 1, params.size());
        Iterator<PathParameter> iterator = params.iterator();
        PathParameter param = iterator.next();
        assertEquals("Matched variable name", "var1", param.name());
        assertEquals("Matched variable value", "1234", param.get());

    }
    
    @Test
    public void testMatchMoreVariables() throws InvalidURITemplateException, URITemplateMismatchException, APIException {
        SegmentMatcher template = SegmentMatcher.parse("/xyz-{var1}-{var2}-{var3}");
        List<PathParameter> params = template.match("/xyz-1234-abcdfg-ABCD");
        assertNotNull("Template matches input", params);
        assertEquals("Number of matched variables", 3, params.size());
        Iterator<PathParameter> iterator = params.iterator();
        PathParameter param;
        param = iterator.next();
        assertEquals("Matched variable name", "var1", param.name());
        assertEquals("Matched variable value", "1234", param.get());
        param = iterator.next();
        assertEquals("Matched variable name", "var2", param.name());
        assertEquals("Matched variable value", "abcdfg", param.get());
        param = iterator.next();
        assertEquals("Matched variable name", "var3", param.name());
        assertEquals("Matched variable value", "ABCD", param.get());

    }
    
    @Test(expected = URITemplateMismatchException.class)
    public void testMatchNoMatch1() throws InvalidURITemplateException, URITemplateMismatchException {
        SegmentMatcher template = SegmentMatcher.parse("/abcde-{var1}");
        template.match("/12345-sdsd");
    }

    @Test(expected = URITemplateMismatchException.class)
    public void testMatchNoMatch2() throws InvalidURITemplateException, URITemplateMismatchException {
        SegmentMatcher template = SegmentMatcher.parse("/abcde-{var1}-tail");
        template.match("/12345-sdsd");
    }

    @Test(expected = URITemplateMismatchException.class)
    public void testMatchNoMatch3() throws InvalidURITemplateException, URITemplateMismatchException {
        SegmentMatcher template = SegmentMatcher.parse("/abcde-{var1}{var2}");
        template.match("/12345-sdsd");
    }

}
