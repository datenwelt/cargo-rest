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

import io.datenwelt.cargo.rest.content.ContentEncoder;
import io.datenwelt.cargo.rest.content.ContentEncoding;
import io.datenwelt.cargo.rest.content.ContentProducer;
import io.datenwelt.cargo.rest.content.ContentType;
import io.datenwelt.cargo.rest.content.empty.EmptyContentType;
import io.datenwelt.cargo.rest.content.empty.EmptyProducer;
import io.datenwelt.cargo.rest.content.gzip.GzipEncoding;
import io.datenwelt.cargo.rest.content.identity.IdentityEncoder;
import io.datenwelt.cargo.rest.content.identity.IdentityEncoding;
import io.datenwelt.cargo.rest.content.json.ApplicationJson;
import io.datenwelt.cargo.rest.content.xml.ApplicationXml;
import io.datenwelt.cargo.rest.content.yaml.ApplicationYaml;
import io.datenwelt.cargo.rest.headers.AcceptHeader;
import io.datenwelt.cargo.rest.path.PathParameter;
import io.datenwelt.cargo.rest.path.errors.InvalidURITemplateException;
import io.datenwelt.cargo.rest.path.Segment;
import io.datenwelt.cargo.rest.path.SegmentMatcher;
import io.datenwelt.cargo.rest.path.PathRouter;
import io.datenwelt.cargo.rest.path.Segment.Scanner;
import io.datenwelt.cargo.rest.path.errors.URITemplateMismatchException;
import io.datenwelt.cargo.rest.response.APIException;
import io.datenwelt.cargo.rest.response.BadRequest;
import io.datenwelt.cargo.rest.response.InternalServerError;
import io.datenwelt.cargo.rest.response.MethodNotAllowed;
import io.datenwelt.cargo.rest.response.NotAcceptable;
import io.datenwelt.cargo.rest.response.NoContent;
import io.datenwelt.cargo.rest.response.NotFound;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for RESTful APIs.
 *
 * This class extends the standard {@code HttpServlet} class and provides a
 * router for incoming REST API requests. It contains several methods to
 * register endpoints for the most common HTTP methods like GET, POST, PUT and
 * DELETE.
 * <p>
 * An endpoint is a resource accessed by a specific HTTP method like
 * {@code "GET /animals"}. For more terminology about resources, endpoints etc.
 * see the package documentation.
 * <p>
 * For this API an endpoint is a functional interface of class {@link Endpoint}
 * which receives an instance of {@link Request} and returns an instance of
 * {@link Response}. The request instance represents the incoming request and
 * the response represents the response which is send to the client.
 * <p>
 * For altering incoming requests and outgoing responses on the fly, use
 * filters. A filter is a class implementing the {@link Filter} interface.
 * Filters can be registered to the router by the {@code filter()}-Methods. Most
 * common examples of filters are made for logging requests or for checking
 * authorization infos.
 * <p>
 * This router receives an incoming request through the {@code service()} method
 * from {@code HttpServlet} and parses the request into an new instance of
 * {@link Request}. It applies all registered filters to the Request in
 * different stages which in turn alter, replace or intercept the request before
 * the actual routing takes place.
 * <p>
 * If the request is not intercepted before the routing phase, the path of the
 * request is matched against the registered endpoints and if an corresponding
 * endpoint is available, the request is passed to the {@code call()} method of
 * the endpoint.
 * <p>
 * The {@code call()} method is expected to return a {@link Response} instance.
 * If the endpoint instead throws an exception, a suitable instance of
 * {@link APIErrorResponse} is generated which extends {@link Response} and can
 * be sent to the client. In most cases this will be an instance of
 * {@link io.datenwelt.cargo.rest.response.InternalServerError}. For more control of
 * the error type, use the class {@link io.datenwelt.cargo.rest.response.APIException}.
 * <p>
 * After the response is generated, again filters can be applied to the response
 * and alter it. Finally the response is handed over to the
 * {@code HttpServletResponse} instance from the {@code service()} method and
 * sent out to the client.
 *
 * @author job
 */
public class Router extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    protected final List<PathRouter> routers = new ArrayList<>();
    protected final Map<String, Endpoint> endpoints = new HashMap<>();
    protected final List<Object> filters = new ArrayList<>();
    protected final List<ContentType> contentTypes = new ArrayList<>();
    protected final List<ContentEncoding> contentEncodings = new ArrayList<>();

    /**
     * Default constructor for the servlet container.
     */
    public Router() {
        contentTypes.add(new EmptyContentType());
        contentTypes.add(new ApplicationJson());
        contentTypes.add(new ApplicationXml());
        contentTypes.add(new ApplicationYaml());
        contentEncodings.add(new IdentityEncoding());
        contentEncodings.add(new GzipEncoding());
    }

    public void contentType(ContentType contentType) {
        contentTypes.add(contentType);
    }

    public void removeContentType(ContentType contentType) {
        contentTypes.remove(contentType);

    }

    /**
     * Registers an endpoint for a specific resource accessible through a
     * specific HTTP method.
     *
     * @param method the HTTP method through which the endpoint is accessed. The
     * argument is case insensitive.
     * @param template the URI path template of the resource.
     * @param endpoint the endpoint which handles incoming requests for the
     * resource.
     * @throws InvalidURITemplateException if the URI path template is invalid.
     */
    public void register(String method, String template, Endpoint endpoint) throws InvalidURITemplateException {
        Scanner scanner = new Scanner(template);
        while (scanner.hasNext()) {
            SegmentMatcher nextSegment = SegmentMatcher.parse(scanner.next());
            if (nextSegment.equals(SegmentMatcher.EMPTY_SEGMENT)
                    || nextSegment.equals(SegmentMatcher.CURRENT_SEGMENT)
                    || nextSegment.equals(SegmentMatcher.PREVIOUS_SEGMENT)) {
                continue;
            }
            boolean isNewSegment = true;
            PathRouter router = new PathRouter(nextSegment);
            for (PathRouter r : routers) {
                if (r.getTemplate().equals(nextSegment)) {
                    router = r;
                    isNewSegment = false;
                    break;
                }
            }
            scanner = router.register(scanner, method, endpoint);
            if (!scanner.hasNext()) {
                if (isNewSegment) {
                    routers.add(router);
                }
                return;
            }
        }
        endpoints.put(method, endpoint);
    }

    /**
     * Registers a filter instance. This method registers an instance of a class
     * implementing the {@link Filter} interface. This instance is used for all
     * requests and <b>must</b> be implemented in a thread-safe manner.
     * <p>
     * Filters are applied in the order of their registration.
     * <p>
     * @param filter the filter instance to register.
     */
    public void filter(Filter filter) {
        filters.add(filter);
    }

    /**
     * Registers a filter class. This method registers a class implementing the
     * {@link Filter} interface. The class must provide a default constructor
     * which is used to create a new filter instance for each incoming request.
     * <p>
     * Filters are applied in the order of their registration.
     * <p>
     *
     * @param filterClass the filter instance to register.
     */
    public void filter(Class<? extends Filter> filterClass) {
        filters.add(filterClass);
    }

    /**
     * Removes the filter instance, if it has been registered before.
     *
     * @param filter the filter instance to remove.
     */
    public void remove(Filter filter) {
        filters.remove(filter);
    }

    /**
     * Removes the filter class, if it has been registered before.
     *
     * @param filterClass
     */
    public void remove(Class<? extends Filter> filterClass) {
        filters.remove(filterClass);
    }

    /**
     * Registers an endpoint for a resource with a specific URI path template
     * for use with the GET method.
     *
     * @param path the URI path template of the resource
     * @param endpoint the endpoint to register
     * @throws InvalidURITemplateException if the URI path template is invalid.
     */
    public void GET(String path, Endpoint endpoint) throws InvalidURITemplateException {
        register("GET", path, endpoint);
    }

    /**
     * Registers an endpoint for a resource with a specific URI path template
     * for use with the POST method.
     *
     * @param path the URI path template of the resource
     * @param endpoint the endpoint to register
     * @throws InvalidURITemplateException if the URI path template is invalid.
     */
    public void POST(String path, Endpoint endpoint) throws InvalidURITemplateException {
        register("POST", path, endpoint);
    }

    /**
     * Registers an endpoint for a resource with a specific URI path template
     * for use with the PUT method.
     *
     * @param path the URI path template of the resource
     * @param endpoint the endpoint to register
     * @throws InvalidURITemplateException if the URI path template is invalid.
     */
    public void PUT(String path, Endpoint endpoint) throws InvalidURITemplateException {
        register("PUT", path, endpoint);
    }

    /**
     * Registers an endpoint for a resource with a specific URI path template
     * for use with the DELETE method.
     *
     * @param path the URI path template of the resource
     * @param endpoint the endpoint to register
     * @throws InvalidURITemplateException if the URI path template is invalid.
     */
    public void DELETE(String path, Endpoint endpoint) throws InvalidURITemplateException {
        register("DELETE", path, endpoint);
    }

    /**
     * Registers an endpoint for a resource with a specific URI path template
     * for use with the OPTIONS method.
     *
     * @param path the URI path template of the resource
     * @param endpoint the endpoint to register
     * @throws InvalidURITemplateException if the URI path template is invalid.
     */
    public void OPTIONS(String path, Endpoint endpoint) throws InvalidURITemplateException {
        register("OPTIONS", path, endpoint);
    }

    /**
     * This method finds a set of possible endpoints for a specific path within
     * the registered endpoints of this router. If a matching endpoint is found
     * the list of {@link io.datenwelt.cargo.rest.path.PathParameter} is filled with
     * the matched parameters from the URI path template which matched the path.
     * <p>
     * If no matching endpoints are found, this method returns an empty map.
     *
     * @param path the path of the request
     * @param parameters a list which is filled by this method
     * @return A (possibly empty) map of HTTP methods to endpoints that matched
     * the input path. It is guaranteed to be not {@code null}.
     */
    protected Map<String, Endpoint> route(String path, List<PathParameter> parameters) {
        path = Segment.normalize(path);
        Scanner scanner = new Scanner(path);
        Segment next = Segment.parse(scanner.next());
        if (!SegmentMatcher.ROOT_SEGMENT.mismatch(next)) {
            return new LinkedHashMap<>(endpoints);
        }
        for (PathRouter router : routers) {
            int scannerPos = scanner.getPos();
            List<PathParameter> paramsFound;
            try {
                paramsFound = router.getTemplate().match(next);
                Map<String, Endpoint> endpointsFound = router.route(scanner, paramsFound);
                parameters.addAll(paramsFound);
                return endpointsFound;
            } catch (URITemplateMismatchException ex) {
                scanner.setPos(scannerPos);
            }
        }
        return new HashMap<>();
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        String queryString = servletRequest.getQueryString();
        String requestMethod = servletRequest.getMethod();
        String requestString = new StringBuilder("\"")
                .append(requestMethod)
                .append(" ")
                .append(servletRequest.getRequestURL())
                .append((queryString != null && !queryString.isEmpty()) ? "?" + queryString : "")
                .append("\"")
                .toString();

        // Create all filter instances.
        List<Filter> filterInstances = new ArrayList<>();
        Iterator filterIterator = filters.iterator();
        while (filterIterator.hasNext()) {
            Object filterObject = filterIterator.next();
            Optional<Filter> filter = createFilterInstance(filterObject);
            if (filter.isPresent()) {
                filterInstances.add(filter.get());
            }
        }

        Optional<Response> response = Optional.empty();

        // Apply "prepare" filters.
        Optional<Request> optionalRequest = Optional.empty();
        Iterator<Filter> iterator = filterInstances.iterator();
        while (!optionalRequest.isPresent() && !response.isPresent() && iterator.hasNext()) {
            Filter filter = iterator.next();
            try {
                optionalRequest = filter.parse(servletRequest);
                if (optionalRequest.isPresent()) {
                    LOG.debug("Filter {} provided a request instance for request {} in \"prepare\" stage.", filter.getClass().getName(), requestString);
                }
            } catch (APIException ex) {
                response = Optional.ofNullable(ex.getResponse());
                if (response.isPresent()) {
                    int status = response.get().getStatus();
                    LOG.debug("Filter {} intercepting request {} in \"prepare\" stage with HTTP status {}",
                            filter.getClass().getName(),
                            requestString, status);
                }
            } catch (RuntimeException ex) {
                LOG.error("Unable to apply \"prepare\" filter {} to request {}: {}", filter.getClass().getName(), requestString, ex.getMessage(), ex);
            }
        }

        Request request;
        if (response.isPresent()) {
            // If there is a response after the filter stage, we have an 
            // error in the request and it must not be processed any further.
            request = null;
        } else if (optionalRequest.isPresent()) {
            // If there is a new request, replace the original request with the
            // new one.
            request = optionalRequest.get();
        } else {
            // If the filering resulted in no new request instance 
            // and no response was present, the original request passed the filter.
            try {

                request = new Request(servletRequest, contentTypes, contentEncodings);
                requestString = request.toString();
            } catch (RuntimeException ex) {
                request = null;
                LOG.error("Error interpreting incoming request {}: {}", requestString, ex.getMessage(), ex);
                response = Optional.of(new InternalServerError());
            } catch (APIException ex) {
                request = null;
                LOG.debug("Error interpreting incoming request {}: {}", requestString, ex.getMessage(), ex);
                response = Optional.ofNullable(ex.getResponse() != null ? ex.getResponse() : new BadRequest());
            }
        }

        // If there is a request, work it out. 
        if (request != null) {
            // Apply "before" filter.
            iterator = filterInstances.iterator();
            while (!response.isPresent() && iterator.hasNext()) {
                Filter filter = iterator.next();
                try {
                    request = filter.before(request).orElse(request);
                } catch (APIException ex) {
                    response = Optional.ofNullable(ex.getResponse());
                    if (response.isPresent()) {
                        int status = response.get().getStatus();
                        LOG.debug("Filter {} intercepting request {} in \"before\" stage with HTTP status {}",
                                filter.getClass().getName(),
                                requestString, status);
                    }
                } catch (RuntimeException ex) {
                    LOG.error("Unable to apply \"before\" filter {} to request {}: {}", filter.getClass().getName(), requestString, ex.getMessage(), ex);
                }
            }

            // Find corresponding endpoints.
            List<PathParameter> parameters = new ArrayList<>();
            Map<String, Endpoint> possibleEndpoints = new HashMap<>();
            try {
                possibleEndpoints = route(request.getPath(), parameters);
                request.getParameters().addAll(parameters);
            } catch (RuntimeException ex) {
                LOG.error("Unable to route request {}: {}", requestString, ex.getMessage(), ex);
                response = Optional.of(new InternalServerError());
            }

            // Apply "routing" filters.
            iterator = filterInstances.iterator();
            while (!response.isPresent() && iterator.hasNext()) {
                Filter filter = iterator.next();
                try {
                    Optional<Endpoint> endpoint = filter.route(possibleEndpoints, request);
                    if (endpoint.isPresent()) {
                        LOG.debug("Filter {} overrides endpoints for request {} in \"routing\" stage.",
                                filter.getClass().getName(),
                                requestString);
                        possibleEndpoints.clear();
                        possibleEndpoints.put(request.getMethod(), endpoint.get());
                        break;
                    }
                } catch (APIException ex) {
                    response = Optional.ofNullable(ex.getResponse());
                    if (response.isPresent()) {
                        int status = response.get().getStatus();
                        LOG.debug("Filter {} intercepting request {} in \"routing\" stage with HTTP status {}",
                                filter.getClass().getName(),
                                requestString, status);
                    }
                } catch (RuntimeException ex) {
                    LOG.error("Unable to apply \"routing\" filter {} to request {}: {}", filter.getClass().getName(), requestString, ex.getMessage(), ex);
                }
            }

            // Check if we have any applicable endpoints and croak if not.
            if (possibleEndpoints.isEmpty()) {
                response = Optional.of(new NotFound());
            } else if (!possibleEndpoints.containsKey(requestMethod)) {
                response = Optional.of(new MethodNotAllowed());
            }

            // Call the endpoint if we have no response yet.
            if (!response.isPresent()) {
                Endpoint endpoint = possibleEndpoints.get(requestMethod);
                try {
                    Response resp = endpoint.call(request);
                    response = Optional.ofNullable(resp);
                } catch (APIException ex) {
                    response = Optional.ofNullable(ex.getResponse());
                    LOG.error("Endpoint for request {} has thrown an exception: {}", requestString, ex.getMessage(), ex);
                } catch (RuntimeException ex) {
                    LOG.error("Unable to call endpoint for request {}: {}", requestString, ex.getMessage(), ex);
                    response = Optional.of(new InternalServerError());
                }
            }

            // Apply "after" filters.
            iterator = filterInstances.iterator();
            while (response.isPresent() && iterator.hasNext()) {
                Filter filter = iterator.next();
                try {
                    Optional<Response> resp = filter.after(request, response.get());
                    if (resp.isPresent()) {
                        LOG.debug("Filter {} overrides response for request {} in \"after\" stage.",
                                filter.getClass().getName(),
                                requestString);
                        response = resp;
                        break;
                    }
                } catch (APIException ex) {
                    response = Optional.ofNullable(ex.getResponse());
                    if (response.isPresent()) {
                        int status = response.get().getStatus();
                        LOG.debug("Filter {} intercepting response {} in \"after\" stage with HTTP status {}",
                                filter.getClass().getName(),
                                requestString, status);
                    }
                } catch (RuntimeException ex) {
                    LOG.error("Unable to apply \"after\" filter {} to request {}: {}", filter.getClass().getName(), requestString, ex.getMessage(), ex);
                }
            }
        }

        // We really need a response present at this point.
        Response actualResponse;
        if (!response.isPresent()) {
            LOG.debug("No response present after calling the endpoint and applying \"after\" filters for request {}. Sending 204 - \"No Content\".");
            actualResponse = new NoContent();
        } else {
            actualResponse = response.get();
        }

        ContentProducer contentProducer;
        ContentEncoder contentEncoder;

        // Negotiate the encoding. Use identity as fallback.
        try {
            contentEncoder = negotiateEncoding(request, actualResponse);
        } catch (Exception ex) {
            LOG.error("Content encoding negotiation for request {} failed internally, using \"identity\" as fallback. Reason: ", requestString, ex.getMessage(), ex);
            contentEncoder = new IdentityEncoder();
        }
        try {
            // Negotiate the content type.
            contentProducer = negotiateContent(request, actualResponse);
        } catch (APIException ex) {
            try {
                // Negotiate again for the error response.
                LOG.error("Content negotiation for request {} failed. Reason: {}", requestString, ex.getMessage(), ex);
                actualResponse = ex.getResponse();
                contentProducer = negotiateContent(request, actualResponse);
                contentEncoder = new IdentityEncoder();
            } catch (APIException fallbackEx) {
                // Fallback to an empty internal server error.
                LOG.error("Error during content negotiation for an error response for request {}: {}", requestString, fallbackEx.getMessage(), fallbackEx);
                contentProducer = new EmptyProducer();
                contentEncoder = new IdentityEncoder();
                actualResponse = new InternalServerError();
            }
        } catch (RuntimeException ex) {
            // Fallback to an empty internal server error.
            LOG.error("Runtime exception during content negotiation for an error response for request {}: {}", requestString, ex.getMessage(), ex);
            contentProducer = new EmptyProducer();
            contentEncoder = new IdentityEncoder();
            actualResponse = new InternalServerError();
        }
        if (contentProducer == null || contentEncoder == null) {
            // If content negotiation failed, we need an empty 406 response.
            actualResponse = new NotAcceptable();
            contentProducer = new EmptyProducer();
            contentEncoder = new IdentityEncoder();
        }

        // Send the response.
        try {
            actualResponse.send(servletResponse, contentProducer, contentEncoder);
        } catch (IOException | RuntimeException ex) {
            LOG.error("Unable to send response for request {}: {}", requestString, ex.getMessage(), ex);
            actualResponse = new InternalServerError();
            contentProducer = new EmptyProducer();
            contentEncoder = new IdentityEncoder();
            try {
                actualResponse.send(servletResponse, contentProducer, contentEncoder);
            } catch (Exception fallbackEx) {
                LOG.error("Unable to send fallback error message for request {}: {}", requestString, fallbackEx.getMessage(), fallbackEx);
                actualResponse = new InternalServerError();
            }
        }

        // Apply the "finish" filters.
        iterator = filterInstances.iterator();
        while (response.isPresent() && iterator.hasNext()) {
            Filter filter = iterator.next();
            try {
                filter.finish(Optional.ofNullable(request), actualResponse);
            } catch (RuntimeException ex) {
                LOG.error("Unable to apply \"finish\" filter {} to request {}: {}", filter.getClass().getName(), requestString, ex.getMessage(), ex);
            }
        }

    }

    /**
     * Returns an optional instance of the input object. This method is meant to
     * receive either an instance of {@link Filter} or a class which implements
     * {@link Filter}. It returns the instance itself in the first case, a new
     * instance of the class in the second case.
     * <p>
     * If the instantiation of the class fails, the class does not implement
     * {@code Filter}, the instance is no {@code Filter} or if {@code null} is
     * passed as parameter, an empty Optional is returned which results in the
     * filter to be skipped by the router.
     * <p>
     * The same filter instance is used throughout the whole lifecycle of a
     * request.
     * <p>
     * @param input the object the {@code Filter} instance is used as or created
     * from.
     * @return A possibly empty instance of {@code Filter} representing the
     * input object.
     */
    protected Optional<Filter> createFilterInstance(Object input) {
        if (Filter.class
                .isInstance(input)) {
            return Optional.ofNullable((Filter) input);

        }
        if (Filter.class
                .isAssignableFrom((Class<?>) input)) {
            Class<? extends Filter> filterClass = (Class<? extends Filter>) input;
            try {
                Filter filter = filterClass.newInstance();
                return Optional.ofNullable(filter);
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error("Unable to create instance of filter {}: {}", filterClass, ex.getMessage(), ex);
                return Optional.empty();
            }
        }
        LOG.error("Filter {} is no filter. Skipping", input);
        return Optional.empty();
    }

    /**
     * Returns all registered path routers.
     *
     * @return A list of all registered path routers.
     */
    protected List<PathRouter> getRouters() {
        return routers;
    }

    /**
     * Returns a list of all endpoints registered to the root resource ("/") of
     * this router.
     *
     * @return A list of all registered endpoints of the root resource.
     */
    public Map<String, Endpoint> getEndpoints() {
        return new LinkedHashMap<>(endpoints);
    }

    protected ContentProducer negotiateContent(Request request, Response response) throws APIException {
        AcceptHeader accept = new AcceptHeader("Accept");
        AcceptHeader acceptCharset = new AcceptHeader("Accept-Charset");
        if (request != null && request.header("Accept").isPresent()) {
            try {
                accept.add(request.header("Accept").get());
                response.header("Vary", "Accept");
            } catch (IllegalArgumentException ex) {
                throw new APIException(new BadRequest(), "Invalid \"Accept\" header: " + request.header("Accept").get().combined(), ex);
            }
        } else {
            accept.add("*/*; q=1");
        }
        if (request != null && request.header("Accept-Charset").isPresent()) {
            try {
                acceptCharset.add(request.header("Accept-Charset").get());
                response.header("Vary", "Accept-Charset");
            } catch (IllegalArgumentException ex) {
                throw new APIException(new BadRequest(), "Invalid \"Accept-Charset\" header: " + request.header("Accept-Charset").get().combined(), ex);
            }
        } else {
            acceptCharset.add("*; q=1");
        }
        
        Iterator<ContentType> iterator = contentTypes.iterator();
        while (iterator.hasNext()) {
            ContentType contentType = iterator.next();
            Optional<ContentProducer> optionalProducer = contentType.producerFor(accept, acceptCharset, request, response);
            if (optionalProducer != null && optionalProducer.isPresent()) {
                return optionalProducer.get();
            }
        }
        return null;
    }

    protected ContentEncoder negotiateEncoding(Request request, Response response) throws APIException {
        AcceptHeader acceptEncoding = new AcceptHeader("Accept-Encoding");
        if (request.header("Accept-Encoding").isPresent()) {
            acceptEncoding.add(request.header("Accept-Encoding").get());
            response.header("Vary", "Accept-Encoding");
            boolean explicitIdentity = false;
            for (AcceptHeader.Value value : acceptEncoding.values()) {
                if (value.matches("identity")) {
                    explicitIdentity = true;
                    break;
                }
            }
            if (!explicitIdentity) {
                // If identity is not mentioned explicitly, add it as acceptable
                // with a rather low priority to emulate a fallback decision.
                // This also includes "*" as encoding value.
                acceptEncoding.add("identity; q=0.1");
            }
        } else {
            acceptEncoding.add("identity");
        }
        Iterator<ContentEncoding> iterator = contentEncodings.iterator();
        while (iterator.hasNext()) {
            ContentEncoding contentEncoding = iterator.next();
            Optional<ContentEncoder> optionalEncoder = contentEncoding.encoderFor(acceptEncoding, request, response);
            if (optionalEncoder.isPresent()) {
                return optionalEncoder.get();
            }
        }
        return null;
    }
}
