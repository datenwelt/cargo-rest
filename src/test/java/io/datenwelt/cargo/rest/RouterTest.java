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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import io.datenwelt.cargo.rest.examples.TestAPI;
import io.datenwelt.cargo.rest.examples.TestPerson;
import io.datenwelt.cargo.rest.path.PathRouter;
import io.datenwelt.cargo.rest.path.SegmentMatcher;
import io.datenwelt.cargo.rest.path.errors.InvalidURITemplateException;
import io.datenwelt.cargo.rest.response.APIException;
import io.datenwelt.cargo.rest.response.MethodNotAllowed;
import static io.datenwelt.cargo.rest.test.utils.ContentEncodings.contentEncodings;
import static io.datenwelt.cargo.rest.test.utils.ContentTypes.contentTypes;
import static io.datenwelt.cargo.rest.test.utils.ServletUtils.createRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author job
 */
public class RouterTest {

    public RouterTest() {
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
    public void testRegisterRoot() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        assertEquals("Router has 0 child routes", 0, router.getRouters().size());
        assertEquals("Router has one registered enpoint", 1, router.getEndpoints().size());
        assertTrue("Router has one registered enpoint for GET", router.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, router.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterSlashesAtBeginning() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "////", endpoint);
        assertEquals("Router has 0 child routes", 0, router.getRouters().size());
        assertEquals("Router has one registered enpoint", 1, router.getEndpoints().size());
        assertTrue("Router has one registered enpoint for GET", router.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, router.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterSingleSegment() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter = router.getRouters().get(0);
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterSingleDotAtBeginning() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "./abc", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter = router.getRouters().get(0);
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterDoubleDotAtBeginning() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "../abc", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter = router.getRouters().get(0);
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterWithNormalization1() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/..", endpoint);
        assertEquals("Router has 0 child routes", 0, router.getRouters().size());
        assertEquals("Router has one registered enpoint", 1, router.getEndpoints().size());
        assertTrue("Router has one registered enpoint for GET", router.getEndpoints().containsKey("GET"));
    }

    @Test
    public void testRegisterWithNormalization2() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/../def", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter = router.getRouters().get(0);
        assertEquals("Child router's template", SegmentMatcher.parse("/def"), subRouter.getTemplate());
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterWithNormalization3() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/def/../def/", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter;
        subRouter = router.getRouters().get(0);
        assertEquals("Child router's template", SegmentMatcher.parse("/abc"), subRouter.getTemplate());
        assertEquals("Child router has 1 child routes", 1, subRouter.getRouters().size());
        assertEquals("Child router has zero registered enpoints", 0, subRouter.getEndpoints().size());
        subRouter = subRouter.getRouters().get(0);
        assertEquals("Child router's template", SegmentMatcher.parse("/def"), subRouter.getTemplate());
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterWithNormalization4() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/../def/ghi", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter;
        subRouter = router.getRouters().get(0);
        assertEquals("Child router's template", SegmentMatcher.parse("/def"), subRouter.getTemplate());
        assertEquals("Child router has 1 child routes", 1, subRouter.getRouters().size());
        assertEquals("Child router has zero registered enpoints", 0, subRouter.getEndpoints().size());
        subRouter = subRouter.getRouters().get(0);
        assertEquals("Child router's template", SegmentMatcher.parse("/ghi"), subRouter.getTemplate());
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testRegisterMultipleSegments() throws InvalidURITemplateException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/def/{id}", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        assertEquals("Router has 1 child route", 1, router.getRouters().size());
        assertTrue("Router has no registered endpoints", router.getEndpoints().isEmpty());
        PathRouter subRouter = router.getRouters().get(0);
        assertEquals("Router has 2 child routes", 2, subRouter.getRouters().size());
        assertTrue("Router has no registered endpoints", subRouter.getEndpoints().isEmpty());
        subRouter = subRouter.getRouters().get(0);
        assertEquals("Router has 1 child route", 1, subRouter.getRouters().size());
        assertTrue("Router has no registered endpoints", subRouter.getEndpoints().isEmpty());
        subRouter = subRouter.getRouters().get(0);
        assertEquals("Child router has 0 child routes", 0, subRouter.getRouters().size());
        assertEquals("Child router has one registered enpoint", 1, subRouter.getEndpoints().size());
        assertTrue("Child router has one registered enpoint for GET", subRouter.getEndpoints().containsKey("GET"));
        assertEquals("The registered enpoint for GET is as expected", endpoint, subRouter.getEndpoints().get("GET"));
    }

    @Test
    public void testMatchSlash1() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/"), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
    }

    @Test
    public void testMatchSlash2() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/.."), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
    }

    @Test
    public void testMatchFirstSegment() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc"), contentTypes(), contentEncodings());
        Map<String, Endpoint> endpoints = router.route(request.getPath(), request.getParameters());
        assertEquals("Number of endpoints", 0, endpoints.size());
    }

    @Test
    public void testMatchSecondSegment() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/876"), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
        assertEquals("Number of path parameters", 1, request.getParameters().size());
        assertEquals("Path parameter #1 name", "id", request.getParameters().get(0).name());
        assertEquals("Path parameter #1 value", "876", request.getParameters().get(0).get());
    }

    @Test
    public void testMatchDotDotsSegments1() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/123/../0ab/../cde"), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
        assertEquals("Number of path parameters", 1, request.getParameters().size());
        assertEquals("Path parameter #1 name", "id", request.getParameters().get(0).name());
        assertEquals("Path parameter #1 value", "cde", request.getParameters().get(0).get());
    }

    @Test
    public void testMatchDotDotsSegments3() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}", endpoint);
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/xyz/.."), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
    }

    @Test
    public void testMatchDotDotsSegments4() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/xyz/../def/123"), contentTypes(), contentEncodings()); // resolves to /abc/def/123
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
    }
    
    @Test
    public void testMatchDotDotsSegments5() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/xyz/../def/123/../"), contentTypes(), contentEncodings()); // resolves to /abc/def
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNull(ep);
    }

        @Test
    public void testMatchDotDotsSegments6() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/xyz/../uvw/123/../../def/123"), contentTypes(), contentEncodings()); // resolves to /abc/def/123
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
    }

    @Test
    public void testMatchDotDotsSegments2() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/abc/def/{id}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/def/xyz/0ab/.."), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
        assertEquals("Number of path parameters", 1, request.getParameters().size());
        assertEquals("Path parameter #1 name", "id", request.getParameters().get(0).name());
        assertEquals("Path parameter #1 value", "xyz", request.getParameters().get(0).get());
    }

    @Test
    public void testMatchTwoParams() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}/xyz/{id2}", endpoint);
        Request request = new Request(createRequest("GET", "/abc/876/xyz/543"), contentTypes(), contentEncodings());
        Map<String, Endpoint> eps = router.route(request.getPath(), request.getParameters());
        Endpoint ep = eps.get("GET");
        assertNotNull(ep);
        assertEquals("Number of path parameters", 2, request.getParameters().size());
        assertEquals("Path parameter #1 name", "id", request.getParameters().get(0).name());
        assertEquals("Path parameter #1 value", "876", request.getParameters().get(0).get());
        assertEquals("Path parameter #2 name", "id2", request.getParameters().get(1).name());
        assertEquals("Path parameter #2 value", "543", request.getParameters().get(1).get());
    }

    @Test
    public void testNotFound1() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}/xyz/{id2}", endpoint);
        Request request = new Request(createRequest("GET", "/def"), contentTypes(), contentEncodings());
        Map<String, Endpoint> endpoints = router.route(request.getPath(), request.getParameters());
        assertEquals("Number of endpoints", 0, endpoints.size());
    }

    @Test
    public void testNotFound2() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}/xyz/{id2}", endpoint);
        Request request = new Request(createRequest("POST", "/def"), contentTypes(), contentEncodings());
        Map<String, Endpoint> endpoints = router.route(request.getPath(), request.getParameters());
        assertEquals("Number of endpoints", 0, endpoints.size());
    }

    @Test
    public void testNotFound3() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}/xyz/{id2}", endpoint);
        Request request = new Request(createRequest("POST", "/abc/xyz/../def"), contentTypes(), contentEncodings());
        Map<String, Endpoint> endpoints = router.route(request.getPath(), request.getParameters());
        assertEquals("Number of endpoints", 0, endpoints.size());
        router.register("GET", "/abc/{id}", endpoint);
        request = new Request(createRequest("POST", "/abc/xyz/../def"), contentTypes(), contentEncodings());
        endpoints = router.route(request.getPath(), request.getParameters());
        assertEquals("Number of endpoints", 1, endpoints.size());
    }

    @Test
    public void testMethodNotAllowed() throws InvalidURITemplateException, APIException, IOException {
        Endpoint endpoint = (r) -> {
            return null;
        };
        Router router = new Router();
        router.register("GET", "/", endpoint);
        router.register("GET", "/abc/{id}/xyz/{id2}", endpoint);
        Request request = new Request(createRequest("POST", "/abc/123/xyz/456"), contentTypes(), contentEncodings());
        try {
            Map<String, Endpoint> route = router.route(request.getPath(), request.getParameters());
            if (route.get("POST") == null) {
                throw new APIException(new MethodNotAllowed());
            }
            fail("route() should throw an APIException.");
        } catch (APIException ex) {
            Response response = ex.getResponse();
            assertEquals("Error code", 405, response.getStatus());
        }
    }

    @Test
    public void testFullAPIRoundtrip() throws IOException, SAXException {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("api/*", TestAPI.class.getName());
        ServletUnitClient sc = sr.newClient();
        WebRequest request = new GetMethodWebRequest("http://localhost/api/person/Test/Testmann");
        WebResponse response = sc.getResponse(request);
        int responseCode = response.getResponseCode();
        assertEquals("HTTP status code", 200, responseCode);
        InputStream inputStream = response.getInputStream();
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(inputStream);
        assertTrue("Body is an JSON object", root.isObject());
        ObjectNode rootObj = (ObjectNode) root;
        assertTrue("firstname present in result", rootObj.has("firstname"));
        assertTrue("lastname present in result", rootObj.has("lastname"));
        assertTrue("birthday present in result", rootObj.has("birthday"));
        TestPerson person = TestPerson.sample();
        assertEquals("Value of field firstname", person.getFirstname(), rootObj.get("firstname").asText());
        assertEquals("Value of field lastname", person.getLastname(), rootObj.get("lastname").asText());
        LocalDate birthday = new LocalDate(rootObj.get("birthday").asText());
        assertEquals("Value of field lastname", person.getBirthday(), birthday);

    }

    @Test
    public void testMethodNotAllowedRoundtrip() throws IOException, SAXException {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("api/*", TestAPI.class.getName());
        ServletUnitClient sc = sr.newClient();
        ByteArrayInputStream input = new ByteArrayInputStream("null".getBytes());
        WebRequest request = new PutMethodWebRequest("http://localhost/api/person/Test/Testmann", input, "application/json");
        try {
            sc.getResponse(request);
        } catch (HttpException ex) {
            int responseCode = ex.getResponseCode();
            assertEquals("HTTP status code", 405, responseCode);
        }
    }

    @Test
    public void testNotFoundRoundtrip() throws IOException, SAXException {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("api/*", TestAPI.class.getName());
        ServletUnitClient sc = sr.newClient();
        ByteArrayInputStream input = new ByteArrayInputStream("null".getBytes());
        WebRequest request = new PutMethodWebRequest("http://localhost/api/notfound/Test/Testmann", input, "application/json");
        try {
            sc.getResponse(request);
        } catch (HttpException ex) {
            int responseCode = ex.getResponseCode();
            assertEquals("HTTP status code", 404, responseCode);
        }
    }
}
