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

import io.datenwelt.cargo.rest.response.APIException;

/**
 * Interface for endpoints of RESTful resources. This interface defines 
 * just one method {@code call} which is invoked when the router dispatches
 * in incoming HTTP request to this endpoint.
 * <p>
 * An incoming HTTP request is dispatched to an endpoint when the endpoint was
 * previously registered to the router whith a matching URI path template and 
 * the matching HTTP method.
 * <p>
 * When the router invokes the {@code call()} method, it passes the {@link  Request}
 * instance as an argument and expects an {@link Response} instance as return value.
 * If the method throws an instance of {@link io.datenwelt.cargo.rest.response.APIException} instead
 * the containing {@link io.datenwelt.api.response.APIErrorResponse} instance is
 * used as the response object.
 * <p>
 * If the method invocation throws an RuntimeException instead, the exception will
 * be logged and an instance of {@link io.datenwelt.cargo.rest.response.InternalServerError} will
 * be sent back to the client.
 * <p>
 * This interface is meant to be used as a lambda and not to be implemented directly.
 * If not used as a lambda, pay attention to a thread-safe implementation. The
 * same instance is used for every incoming request and will most likely be
 * invoked simultaneously with different requests.
 * <p>
 * Example:
 * <code>
 * <pre>
 *      public static Endpoint GET = (request) -> {
 *          return new OK();
 *      };
 *      router.get("GET", "/", GET);
 * </pre>
 * </code>
 * 
 * @author job
 */
@FunctionalInterface
public interface Endpoint {
    
    /**
     * The method called by the router when dispatching an incoming request to
     * this endpoint.
     * 
     * @param request the incoming request.
     * @return The response after processing the request. A return value of {@code null}
     * corresponds to an empty response with an HTTP status of {@code 204 - No Content}.
     * @throws APIException if the endpoint throws an APIException the containing error 
     * response will be sent to the client and the error will be logged. The difference
     * between returning an instance of {@link io.datenwelt.api.response.APIErrorResponse}
     * and throwing an APIException is, that in the first case the error is logged
     * and in the second case no error logging takes place.
     */
    Response call(Request request) throws APIException;
    
}
