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
package io.datenwelt.cargo.rest;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import io.datenwelt.cargo.rest.examples.TestAPI;
import io.datenwelt.cargo.rest.headers.Header;
import io.datenwelt.cargo.rest.query.Query;
import io.datenwelt.cargo.rest.response.APIException;
import static io.datenwelt.cargo.rest.test.utils.ContentEncodings.contentEncodings;
import static io.datenwelt.cargo.rest.test.utils.ContentTypes.contentTypes;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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
public class RequestTest {
    
    public RequestTest() {
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
    public void testConstructorFromServletRequest() throws IOException, ServletException, APIException {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("api/*", TestAPI.class.getName());
        ServletUnitClient sc = sr.newClient();
        WebRequest getRequest = new GetMethodWebRequest("http://localhost/api/person/Test/Testmann");
        getRequest.setHeaderField("Origin", "http://datenwelt.io");
        getRequest.setParameter("abc", "def");
        getRequest.setParameter("ghi", "jkl");
        InvocationContext ic = sc.newInvocation( getRequest );
        HttpServletRequest servletRequest = ic.getRequest();
        Request request = new Request(servletRequest, contentTypes(), contentEncodings());
        Header actual = request.header("origin").get();
        assertEquals("Header value of 'Origin'", "http://datenwelt.io", actual.get());
        String path = request.getPath();
        assertEquals("Value of path", "/person/Test/Testmann", path);
        String method = request.getMethod();
        assertEquals("Value of path", "GET", method);
        List<Query> queries = request.queries();
        assertEquals("Number of queries", 2, queries.size());
        assertEquals("Query key #1", "abc", queries.get(0).getKey());
        assertEquals("Query value #1", "def", queries.get(0).getValue().get());
        assertEquals("Query key #2", "ghi", queries.get(1).getKey());
        assertEquals("Query value #2", "jkl", queries.get(1).getValue().get());
    }
    
}
