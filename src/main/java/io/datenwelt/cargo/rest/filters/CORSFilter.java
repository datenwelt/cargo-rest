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
import io.datenwelt.cargo.rest.Filter;
import io.datenwelt.cargo.rest.Request;
import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.response.APIException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author job
 */
public class CORSFilter implements Filter {

    @Override
    public Optional<Endpoint> route(Map<String, Endpoint> endpoints, Request request) throws APIException {
        if (!request.header("Origin").isPresent()) {
            // No CORS request.
            return Optional.empty();
        }
        if (!"OPTIONS".equals(request.getMethod())) {
            // Not a pre-flight request.
            return Optional.empty();
        }
        Set<String> methods = new HashSet(endpoints.keySet());
        if (methods.contains("OPTIONS")) {
            // OPTIONS is handled by a registered endpoint, do not interfere.
            return Optional.empty();
        } else {
            methods.add("OPTIONS");
        }
        // Create the pre-flight response, an endpoint and add it to the possible endpoints.
        Endpoint preflight = (req) -> {
            Response response = new Response(200);
            addCORSHeaders(req, response);
            if (!response.header("Access-Control-Allow-Methods").isPresent()) {
                methods.stream().forEachOrdered((m) -> {
                    response.header("Access-Control-Allow-Methods", m);
                });
            }
            return response;
        };
        endpoints.put("OPTIONS", preflight);
        return Optional.empty();
    }

    @Override
    public Optional<Response> after(Request request, Response response) throws APIException {
        if (request.header("Origin").isPresent() && !"OPTIONS".equals(request.getMethod())) {
            addCORSHeaders(request, response);
        }
        return Optional.empty();
    }

    public void addCORSHeaders(Request request, Response response) {
        String origin = request.header("Origin").get().asString();
        if (!response.header("Access-Control-Allow-Origin").isPresent()) {
            response.header("Access-Control-Allow-Origin", origin);
        }
        if (!response.header("Access-Control-Allow-Headers").isPresent()) {
            response.header("Access-Control-Allow-Headers", "Content-Type");
        }
    }

}
