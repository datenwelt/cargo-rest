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
package io.datenwelt.cargo.rest.filters;

import io.datenwelt.cargo.rest.Endpoint;
import io.datenwelt.cargo.rest.Request;
import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.path.errors.InvalidURITemplateException;
import io.datenwelt.cargo.rest.response.APIException;
import static io.datenwelt.cargo.rest.test.utils.ContentEncodings.contentEncodings;
import static io.datenwelt.cargo.rest.test.utils.ContentTypes.contentTypes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static io.datenwelt.cargo.rest.test.utils.ServletUtils.createRequest;

/**
 *
 * @author job
 */
public class CORSFilterTest {
    
    public CORSFilterTest() {
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
    public void testWithMissingEndpointForOptions() throws InvalidURITemplateException, APIException, IOException {
        Endpoint p = (req) -> { return new Response(200); };
        Map<String, Endpoint> endpoints = new HashMap<>();
        endpoints.put("GET", p);
        endpoints.put("POST", p);
        endpoints.put("DELETE", p);
        Request request = new Request(createRequest("OPTIONS", "/abc"), contentTypes(), contentEncodings());
        request.header("Origin", "http://datenwelt.io");
        CORSFilter cors = new CORSFilter();
        Optional<Endpoint> endpoint = cors.route(endpoints, request);
        assertFalse("CORS filter returns an endpoint.", endpoint.isPresent());
        assertTrue("CORS filter added an OPTIONS endpoint for pre-flight request.", endpoints.containsKey("OPTIONS"));
        endpoint = Optional.of(endpoints.get("OPTIONS"));
        Response response = endpoint.get().call(request);
        assertTrue("CORS Origin header present", response.header("Access-Control-Allow-Origin").isPresent());
        String actual = response.header("Access-Control-Allow-Origin").get().asString();
        assertEquals("CORS Origin header", "http://datenwelt.io", actual);
        assertTrue("CORS Origin header present", response.header("Access-Control-Allow-Headers").isPresent());
        actual = response.header("Access-Control-Allow-Headers").get().asString();
        assertEquals("CORS Headers header", "Content-Type", actual);
        assertTrue("CORS Methods header present", response.header("Access-Control-Allow-Methods").isPresent());
        actual = response.header("Access-Control-Allow-Methods").get().combined();
        assertTrue("CORS Methods header contains GET", actual.contains("GET"));
        assertTrue("CORS Methods header contains POST", actual.contains("POST"));
        assertTrue("CORS Methods header contains DELETE", actual.contains("DELETE"));
        assertTrue("CORS Methods header contains OPTIONS", actual.contains("OPTIONS"));
    }
    
}
