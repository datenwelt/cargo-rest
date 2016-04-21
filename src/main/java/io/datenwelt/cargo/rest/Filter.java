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
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletRequest;

/**
 * Filters are used to check and to alter requests before they are dispatched
 * to an endpoint as well as to alter responses created by the endpoint. When an 
 * incoming request is dispatched to an endpoint by the router, it goes through 
 * different processing phases until an appropriate endpoint is found. After the
 * endpoint provided a response the response is sent to the client.
 * <p> 
 * Filters can be used to observe the request on its way to an endpoint, to 
 * record information about the process or even to change the request or the 
 * response.
 * <p>
 * Each filter hooks into all five phases with the same instance to maintain information
 * throughout the whole process. The router provides an option to have one fresh 
 * filter instance per request and additionally an option to use the same instance
 * for all requests. If the latter option is chosen, special attention has to 
 * be given to thread-safety because the same filter instance will be run
 * for all requests most likely simultaneously. It is not recommended to require
 * any synchronization between the requests. So use this option with caution.
 * <p>
 * There are five different filtering phases the request passes:
 * <ol>
 * <li>prepare</li>
 * <li>before</li>
 * <li>route</li>
 * <li>after</li>
 * <li>finish</li>
 * </ol>
 * <h2>The "prepare" phase</h2>
 * When the servlet container receives an incoming requests it creates an instance
 * of {@code ServletRequest} and fills its properties with information about the 
 * request. The prepare phase creates an instance of {@link Request} from that
 * {@code ServletRequest}. The router calls all filters and checks if one of them
 * provides an Request instance. If one of the filters provides such an instance,
 * it will be used within the further request processing. If no filter provides
 * such an instance, the router creates one on its own.
 * <h2>The "before" phase</h2>
 * After the instance of {@link Request} has been created from the incoming 
 * ServletRequest, this instance can be modified or even replaced entirely based
 * on the available information. This is meant to be used to influence the routing
 * phase which comes next.
 * <h2>The "route" phase</h2>
 * The router prepares a set of possible endpoints that match the request's path 
 * and passes them to the filter for this phase. Because the matching concerns 
 * only the path but ignores the HTTP method of the request for now, the set of 
 * possible endpoints consists of all available endpoints for the resource regardless
 * of the HTTP method.
 * <p>
 * The first endpoint returned by any filter is used to process the request. If
 * no filter provides an endpoint, the router checks the endpoint candidates for
 * an endpoint that matches the HTTP method and dispatches the request there. 
 * If no such endpoint exists a {@code 404 - Not Found} or a {@code 405 - Method Not Allowed} 
 * response is sent depending on the existence of other endpoints which match the path but do not match the HTTP
 * method. If there are no endpoints for the path a 404 is sent. If there are endpoints
 * for the path but none that match the HTTP method, a 405 is sent.
 * <h2>The "after" phase</h2>
 * After the router selects an endpoint and dispatches the request to it, the endpoint
 * should have provided a {@link Response} instance to sent to the client. Otherwise
 * - in case of an error - an appropriate {@link io.datenwelt.api.response.APIErrorResponse}
 * instance should be available. However, any response has come up at this point and
 * this stage is to alter or entirely replace the response before it is sent to
 * the client.
 * <p>
 * Note that this phase is only called if the request has made it to be dispatched to
 * an endpoint. If one of the earlier stages intercepted the request with an 
 * error response, this phase is never reached.
 * 
 * <h2>The "finish" phase</h2>
 * This phase does not have any influence on the outcome of the request anymore. It
 * is called after the response has been sent to the client and meant as a 
 * "monitoring" phase where final logging or recording can take place.
 * 
 * 
 * @author job
 */
public interface Filter {
    
    /**
     * This callback is called in the "prepare" phase during request processing.
     * It receives the ServletRequest instance which has been provided by the
     * servlet container. The filter may provide an instance of
     * {@link Request} as return value. This instance is used by the router
     * to process the request. The first filter that returns such an instance
     * "wins" over the following filters.
     * 
     * @param servletRequest the ServletRequest provided by the servlet container
     * @return an optional Request instance to be used for processing by the router
     * @throws APIException if the filter wants to abort the request processing 
     * it may throw an APIException carrying the corresponding error response to 
     * sent to the client.
     */
    default Optional<Request> parse(ServletRequest servletRequest) throws APIException {
        return Optional.empty();
    };
    
    /**
     * This callback is called by the router after possible endpoints for the request
     * have been resolved. All endpoints matching the request path are passed
     * in as a parameter regardless of their HTTP method, so the filter might check
     * the available methods for this endpoint.<p>
     * If the callback returns an endpoint, this endpoint gets dispatched the request.<p>
     * Otherwise the default dispatch mechanism is used (see the class level documentation
     * for details).
     * 
     * @param endpoints a map of possible endpoints with their corresponding HTTP methods.
     * @param request the current request
     * @return an optional Endpoint to dispatch the request to. The first filter returning an endpoint "wins".
     * @throws APIException if the filter wants to abort the request processing 
     * it may throw an APIException carrying the corresponding error response to 
     * sent to the client.
     */
    default Optional<Endpoint> route(Map<String, Endpoint> endpoints, Request request) throws APIException {
        return Optional.empty();
    };
    
    /**
     * This callback is called by the router before it tries to find an
     * endpoint for the request. It can be used to influence the outcome of
     * the next phase, to change the present request or to entirely swap it.
     * <p>
     * If the request is swapped, the new request will be passed to the following
     * routers.
     * 
     * @param request the current request
     * @return an optional request to replace the current request
     * @throws APIException if the filter wants to abort the request processing 
     * it may throw an APIException carrying the corresponding error response to 
     * sent to the client.
     */
    default Optional<Request> before(Request request) throws APIException {
        return Optional.empty();
    };
    
    /**
     * This callback is called after the router dispatched the request to the 
     * endpoint. The {@link Response} which has been provided by the endpoint
     * can be altered or entirely replaced in this phase before it is sent 
     * to the client.
     * <p>
     * If the response is replaced as a result of this callback, the following
     * filters receive the replaced instance.
     * <p>
     * This phase is not called if any of the previous stages resulted in an 
     * error response caused by a thrown APIException.
     * 
     * @param request the current request
     * @param response the current response which is to be sent to the client
     * @return An optional response which replaces the response passed in as parameter. 
     * @throws APIException if the filter wants to abort the request processing 
     * it may throw an APIException carrying the corresponding error response to 
     * sent to the client.
     */
    default Optional<Response> after(Request request, Response response) throws APIException {
        return Optional.empty();
    };
    
    /**
     * This callback is called after the response has been sent to the client.
     * Altering the response nor request has any effect on the outcome of the
     * request processing anymore but this stage is useful for monitoring such
     * as logging or request recording.
     * 
     * @param request an optional request which is empty if the current request could not be processed
     * @param response the response that has been sent to the client.
     */
    default void finish(Optional<Request> request, Response response) {
    };
}
