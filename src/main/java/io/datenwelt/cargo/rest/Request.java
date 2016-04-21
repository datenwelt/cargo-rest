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

import io.datenwelt.cargo.rest.content.ContentConsumer;
import io.datenwelt.cargo.rest.content.ContentDecoder;
import io.datenwelt.cargo.rest.content.ContentEncoding;
import io.datenwelt.cargo.rest.content.ContentType;
import io.datenwelt.cargo.rest.headers.ContentTypeHeader;
import io.datenwelt.cargo.rest.headers.Header;
import io.datenwelt.cargo.rest.path.PathParameter;
import io.datenwelt.cargo.rest.path.Segment;
import io.datenwelt.cargo.rest.query.Query;
import io.datenwelt.cargo.rest.response.APIException;
import io.datenwelt.cargo.rest.response.BadRequest;
import io.datenwelt.cargo.rest.response.InternalServerError;
import io.datenwelt.cargo.rest.response.LengthRequired;
import io.datenwelt.cargo.rest.response.UnprocessableEntity;
import io.datenwelt.cargo.rest.response.UnsupportedMediaType;
import io.datenwelt.cargo.rest.utils.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an incoming HTTP request. This class is created by the router or
 * by a filter in the "prepare" stage from an incoming request. It provides a
 * comprehensive way to deal with the trickier parts of HTTP requests like paths
 * and queries.
 * <p>
 * A HTTP request consists of the following parts:
 * <ul>
 * <li>The URI of the requested resource.</li>
 * <li>The HTTP method like GET, PUT, POST or DELETE to access this
 * resource.</li>
 * <li>The message header containing meta information about the request.</li>
 * <li>The optional message body carrying some kind of representation of a data
 * object.</li>
 * </ul>
 * <h2>Dealing with request URIs</h2>
 * <p>
 * The URI of the request is a unique address that clearly identifies the
 * requested resource among other resources. Regarding the HTTP protocol this
 * includes one of the two scheme values "http" or "https", the hostname of the
 * system from which the resource is requested, a hierarchical path separated by
 * "/" and a query which sometimes consists of key/value pairs but more
 * generally is a collection of encoded values wich can be in the form of
 * key/value pairs. Query values are separated by the "&" character and in case
 * of key/value pairs have the form of "key=value". (Other forms of key/value
 * pairs like "xyz=123;abc=456" or not supported by this API).
 * <p>
 * The path identifies the resources within the target system and the query part
 * modifies the meaning of request. If an URI contains identical paths but
 * different queries the request goes to the same resource but the way the
 * request works may be intended in different ways.
 * <p>
 * The tricky part about URIs is to get the information out of them. It usually
 * involves some more or less complicated parsing steps and can go wrong in many
 * ways. For instance, the query part may contain the path separator "/" in one
 * of its values or even in the keys. Sloppy usage of regular expressions can
 * mistake the part before this "/" as part of the path and screw up everything
 * after the hostname. Another common problem is the hierarchical nature of the
 * path. A path can contain ".." and "." parts as well as multiple consecutive
 * appearances of slashes like "/////". Paths have to be normalized before being
 * compared, because two syntactically different paths can reference the same
 * resource. And this does not even cover the issue of invalid URIs which
 * definitely will occur in incoming HTTP requests when exposing an API over the
 * internet.
 * <h3>The request URI</h3>
 * The URI of the requested resource is provided in the {@code requestURI}
 * property. For compatibility and flexibility reasons the standard Java class
 * {@code java.net.URI} is used. This property contains the full URI including
 * every path component including the deployment context, the path of the
 * servlet and the path information after the context. In terms of the servlet
 * API this is the result of:
 * <ul>
 * {@code ServletRequest.getRequestURL() + "?" + ServletRequest.getQueryString()}
 * </ul>
 * This gets as near as possible to the original URI from the incoming request.
 * No normalization or sanitizing is applied at this step.
 * <h3>The base URI</h3>
 * For link construction the base URI is derived from the current servlet
 * context and the request URI. It is provided through the property of
 * {@code baseURI} in the form of {@code java.net.URI}. This is the URI under
 * which the whole API is exposed from the perspective of the client.
 *
 * <h3>Paths</h3>
 * The hierarchical path part of the request URI is stored in the {@code path}
 * property in string form. The path contains only the part after the servlet's
 * path, is normalized but not decoded. Use the
 * {@link io.datenwelt.cargo.rest.path.Segment} class to decode the path if needed. If
 * the request contains any path parameters they are stored in the property
 * {@code parameters}. This property is not filled by the router before the
 * routing process completes because the parameter names are required to do so.
 * <h3>Query</h3>
 * The query part of the URI is a list of {@link io.datenwelt.cargo.rest.query.Query}
 * objects. This object has a {@code name} property and an optional
 * {@code value} property. The same query object can appear multiple times in
 * the collection to support shennanigans like:
 * <ul><code><pre>http://...?v&v</pre></code></ul>
 * <p>
 * Some rare implementations may use this to indicate a verbose (and even more
 * verbose) output. More useful examples of identical query values may exist,
 * and this is meant to support them. However this class supports the more
 * common cases of unique key/value pairs by providing suitable accessor
 * methods.
 * <h3>Request headers</h3>
 * The request headers can be found in the {@code headers} properties and can be
 * accessed through the {@code header()} accessor methods. Headers in HTTP
 * requests have a name and a value part. The value part may consist of multiple
 * values. Multi-value headers appear in two forms - as a comma separated list
 * and as multiple occurence of the same header. Both forms can even be mixed.
 * Example:
 * <p>
 * <code><pre>
 *  Cache-Control: private, max-age=0, no-cache
 * </pre></code>
 * <p>
 * or<p>
 * <code><pre>
 *  Cache-Control: private
 *  Cache-Control: max-age=0
 *  Cache-Control: no-cache
 * </pre></code>
 * <p>
 * or even<p>
 * <code><pre>
 *  Cache-Control: private, max-age=0
 *  Cache-Control: no-cache
 * </pre></code>
 *
 * Since all these forms are considered as semantically identical, this class
 * abstracts these forms into a map with a list as value:
 * <p>
 * <code><pre>
 *  "Cache-Control" -> List("private", "max-age=0", "no-cache")
 * </pre></code>
 * <p>
 * <h3>Request bodies</h3>
 * Parsing and deserializing the body part of a request is another tedious task
 * which can get very boring over time. To spare you some time this class lets
 * you access the body with the method {@code getBodyAs()} which takes a target
 * class as an input parameter and returns the deserialized form of the body as
 * an instance of that class.
 * <p>
 * See the package info about serialization for details at
 * {@link io.datenwelt.api.serialization}.
 *
 * @author job
 */
public class Request {

    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    private final String requestId = Strings.uniqid();

    private String method;
    private String path;

    private final List<PathParameter> parameters = new ArrayList<>();
    private final List<Query> queries = new ArrayList<>();
    private final Map<String, Header> headers = new LinkedHashMap<>();

    private final HttpServletRequest servletRequest;
    private final List<ContentType> supportedContentTypes;
    private final List<ContentEncoding> supportedContentEncodings;

    private URI requestURI = null;
    private URI baseURI = null;

    private String remoteHost;
    private String remoteAddress;
    private int remotePort;

    public Request(HttpServletRequest servletRequest, List<ContentType> supportedContentTypes, List<ContentEncoding> supportedContentEncodings) throws APIException {
        this.servletRequest = servletRequest;
        this.supportedContentTypes = supportedContentTypes;
        this.supportedContentEncodings = supportedContentEncodings;
        this.method = servletRequest.getMethod();
        this.path = Segment.normalize(servletRequest.getPathInfo());

        StringBuffer url = servletRequest.getRequestURL();
        String query = servletRequest.getQueryString();
        if (query != null && !query.isEmpty()) {
            url.append("?").append(query);
        }

        // Parse request URI and construct the base URI.
        try {
            requestURI = new URI(url.toString());
            String basePath
                    = (servletRequest.getContextPath() == null
                            ? "" : servletRequest.getContextPath())
                    + (servletRequest.getServletPath() == null ? "" : servletRequest.getServletPath());
            baseURI = URI.create(new StringBuffer()
                    .append(requestURI.getScheme())
                    .append("://")
                    .append(requestURI.getRawAuthority())
                    .append("/")
                    .append(basePath)
                    .toString());
            path = Segment.normalize(requestURI.getPath());
            if (path.startsWith(basePath)) {
                path = path.substring(basePath.length());
            }
        } catch (URISyntaxException ex) {
            throw new APIException(new InternalServerError(), "Unable to parse request URI from string '" + requestURI + "'. Using defaut value for base URI. Error: " + ex.getMessage(), ex);
        }

        // Parse query string.
        String queryString = servletRequest.getQueryString();
        this.queries.addAll(Query.parseQueryString(queryString));

        // Parse header values
        Enumeration headerNames = servletRequest.getHeaderNames();
        QuotedPrintableCodec qp = new QuotedPrintableCodec();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement().toString();
            Enumeration values = servletRequest.getHeaders(name);
            while (values.hasMoreElements()) {
                Header header = headers.get(name);
                if (header == null) {
                    header = new Header(name);
                    headers.put(header.getName(), header);
                }
                String value = values.nextElement().toString();
                try {
                    value = qp.decode(value, "UTF-8");
                } catch (DecoderException ex) {
                    throw new APIException(new BadRequest(), "Request contains bad header value: " + value, ex);
                } catch (UnsupportedEncodingException ex) {
                    throw new Error("Your platform does not support UTF-8.");
                }
                header.add(Header.decode(name, value));
            }
        }

        // Collect infos about the remote end.
        remoteAddress = servletRequest.getRemoteAddr();
        remoteHost = servletRequest.getRemoteHost();
        remotePort = servletRequest.getRemotePort();

    }

    public String getRequestId() {
        return requestId;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public Map<String, Header> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<PathParameter> getParameters() {
        return parameters;
    }

    public URI getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(URI requestURI) {
        this.requestURI = requestURI;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(URI baseURI) {
        this.baseURI = baseURI;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public Optional<ContentTypeHeader> contentType() {
        Optional<Header> header = header("Content-Type");
        if (!header.isPresent()) {
            return Optional.empty();
        }
        ContentTypeHeader contentType = new ContentTypeHeader();
        contentType.set(header.get());
        return Optional.of(contentType);
    }

    public Header header(String name, String value) {
        name = Header.normalizeName(name);
        Header h = headers.get(name);
        if (h == null) {
            h = new Header(name);
            headers.put(name, h);
        }
        h.add(value);
        return h;
    }

    public Header header(String name, int value) {
        name = Header.normalizeName(name);
        Header h = headers.get(name);
        if (h == null) {
            h = new Header(name);
            headers.put(name, h);
        }
        h.add(value);
        return h;
    }

    public Header header(String name, DateTime value) {
        name = Header.normalizeName(name);
        Header h = headers.get(name);
        if (h == null) {
            h = new Header(name);
            headers.put(name, h);
        }
        h.add(value);
        return h;
    }

    public Optional<Header> header(String name) {
        name = Header.normalizeName(name);
        Header h = headers.get(name);
        return Optional.ofNullable(h);
    }

    public Map<String, Header> headers() {
        return headers;
    }

    public String queryString() {
        StringBuilder str = new StringBuilder();
        for (Iterator<Query> it = queries.iterator(); it.hasNext();) {
            Query query = it.next();
            str.append(query.toString());
            if (it.hasNext()) {
                str.append("&");
            }
        }
        return str.toString();
    }

    public Optional<Query> query(String key) {
        for (Query query : queries) {
            if (query.getKey().equals(key)) {
                return Optional.of(query);
            }
        }
        return Optional.empty();
    }

    public void query(String key, String value) {
        Query q = new Query(key, value);
        queries.add(q);
    }

    public List<Query> queries(String key) {
        Query[] array = queries
                .stream()
                .filter((q) -> (q.getKey().equals(key)))
                .toArray((size) -> (new Query[size]));
        return Arrays.asList(array);
    }

    public List<Query> queries() {
        return queries;
    }

    public void removeQuery(String key) {
        List<Query> oldQueries = new ArrayList<>(queries);
        queries.clear();
        oldQueries.stream().filter((q) -> (!q.getKey().equals(key))).forEach((q) -> {
            queries.add(q);
        });
    }

    public PathParameter param(String name) throws APIException {
        for (PathParameter param : parameters) {
            if (param.name().equals(name)) {
                return param;
            }
        }
        throw new APIException(new BadRequest("Missing value for path parameter '" + name + "'."));
    }

    public List<PathParameter> params(String name) throws APIException {
        List<PathParameter> params = new ArrayList<>();
        parameters.stream().filter((param) -> (param.name().equals(name))).forEach((param) -> {
            params.add(param);
        });
        if (params.isEmpty()) {
            throw new APIException(new BadRequest("Missing value for path parameter '" + name + "'."));
        }
        return params;
    }

    public <T> Optional<T> getBodyAs(Class<? extends T> targetClass) throws APIException {
        Optional<ContentTypeHeader> contentTypeHeader = contentType();
        if (!contentTypeHeader.isPresent()) {
            return Optional.empty();
        }
        String contentLengthString = header("Content-Length").orElseThrow(() -> (new APIException(new LengthRequired()))).asString();
        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthString);
        } catch (NumberFormatException ex) {
            throw new APIException(new BadRequest("Unparseable number in Content-Length header."));
        }
        if (contentLength < 0) {
            throw new APIException(new BadRequest("Negative number value in Content-Length header."));
        }
        if (contentLength == 0) {
            return Optional.empty();
        }
        ContentConsumer consumer = null;
        for (ContentType contentType : supportedContentTypes) {
            try {
                Optional<ContentConsumer> optionalConsumer = contentType.consumerFrom(contentTypeHeader.get(), this, targetClass);
                if (optionalConsumer != null && optionalConsumer.isPresent()) {
                    consumer = optionalConsumer.get();
                    break;
                }
            } catch (Exception ex) {
                LOG.debug("Error trying to retrieve consumer from content type {} for \"{}\" - skipping: ", contentType, contentTypeHeader.get().combined(), ex);
            }
        }
        if (consumer == null) {
            throw new APIException(new UnsupportedMediaType());
        }
        InputStream encodingInputStream = null;
        for ( ContentEncoding encoding : supportedContentEncodings) {
            try {
                Optional<ContentDecoder> decoder = encoding.decoderFor(this);
                if ( decoder.isPresent() ) {
                    encodingInputStream = decoder.get().decode(servletRequest.getInputStream(), this);
                }
            } catch (IOException | RuntimeException ex) {
                LOG.debug("Error trying to retrieve decoder from content encoding {} for \"{}\" - skipping: {}", encoding, this, ex.getMessage(), ex);
            }
        }
        if ( encodingInputStream == null ) {
            throw new APIException(new UnprocessableEntity());
        }
        try {
            return Optional.ofNullable(consumer.consume(encodingInputStream, this, targetClass));
        } catch (IOException | RuntimeException ex) {
            LOG.debug("Unable to deserialize body from request {} as {}: {}", this, targetClass.getName(), ex.getMessage(), ex);
            throw new APIException(new InternalServerError());
        }
    }

    @Override
    public String toString() {
        return method + " " + path + (queries.isEmpty() ? "" : "?" + queryString());
    }

}
