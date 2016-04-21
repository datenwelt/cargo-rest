package io.datenwelt.cargo.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.datenwelt.cargo.rest.content.ContentEncoder;
import io.datenwelt.cargo.rest.content.ContentProducer;
import io.datenwelt.cargo.rest.content.empty.EmptyProducer;
import io.datenwelt.cargo.rest.content.identity.IdentityEncoder;
import io.datenwelt.cargo.rest.headers.Header;
import io.datenwelt.cargo.rest.response.APIError;
import io.datenwelt.cargo.rest.response.InternalServerError;
import io.datenwelt.cargo.rest.serialization.Json;
import io.datenwelt.cargo.rest.utils.ByteCountingOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an HTTP response sent to the client. HTTP responses are messages
 * sent from the server to the client in reply to an request. A response consists of
 * 
 * <ul>
 * <li>An HTTP status code</li>
 * <li>An HTTP message header with meta information about the response.</li>
 * <li>An optional message body containing some representation of a data object.</li>
 * </ul>
 * The HTTP status code is a three digit number indicating the outcome of the
 * request being processed by the server. There are various classes of responses
 * distinguished by the first digit.
 * <ul>
 * <li>1xx - Informational</li>
 * <li>2xx - Success</li>
 * <li>3xx - Redirection</li>
 * <li>4xx - Client error</li>
 * <li>5xx - Server error</li>
 * </ul>
 * See the Wikipedia article <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes">here</a> for further details.
 * <p>
 * The response header is formally the same as in requests, see {@link Request} for
 * more details.
 * <p>
 * The response body is optional and can be any serializable object. See the 
 * package info at {@link io.datenwelt.api.serialization} for details on how
 * to serialize Java objects into HTTP message bodies. Basically, it is just
 * passing an object to the message constructor or using the {@code body()} accessor
 * method.
 * <p>
 * The actual serialization takes place whenever an object is passed as new body 
 * of the response. The serialized representation of the body object is kept
 * in a ByteBuffer in memory at the moment. Other forms of buffering are planned
 * for the future but not implemented yet. Keep this in mind with respect to
 * message size and memory limitations.
 * <p>
 * The public {@code send()} method prepares the response to be send with the 
 * HttpServletResponse provided by the servlet container. It is called
 * by the router once per request. In most cases, this method can be ignored
 * but you may override it at some occasions.
 * 
 * @author job
 */
public class Response {

    private static final Logger LOG = LoggerFactory.getLogger(Response.class);

    private int status;
    private final Map<String, Header> headers = new LinkedHashMap<>();
    private Optional body;
    private long contentLength;

    public Response(int status) {
        this.status = status;
        this.body = Optional.empty();
    }

    public Response(int status, Object body) {
        this.status = status;
        if (status >= 200 && status < 400) {
            if (body != null && APIError.class.isInstance(body)) {
                body = new APIError(status, body.toString());
            }
        }
        this.body = Optional.ofNullable(body);
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

    public static Optional serializeBody(Optional body, OutputStream outputStream) {
        if (!body.isPresent()) {
            return body;
        }
        ObjectMapper om = Json.getDefaultObjectMapper();
        try {
            om.writeValue(outputStream, body.get());
            return body;
        } catch (IOException ex) {
            LOG.error("Serialization error: {}", ex.getMessage(), ex);
            InternalServerError error = new InternalServerError();
            try {
                om.writeValue(outputStream, error);
                return Optional.of(error);
            } catch (IOException ex1) {
                LOG.error("Serialization error: {}", ex1.getMessage(), ex1);
                return Optional.empty();
            }
        }
    }

    public Optional body() {
        return body;
    }

    public void body(Object body) {
        this.body = Optional.ofNullable(body);
    }

    public void removeBody() {
        this.body = Optional.empty();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void send(HttpServletResponse servletResponse, ContentProducer contentProducer, ContentEncoder contentEncoder) throws IOException {
        servletResponse.setStatus(status);
        headers.values().stream().forEach((header) -> {
            String name = header.getName();
            String value = header.encoded();
            servletResponse.addHeader(name, value);
        });
        if (body.isPresent()) {
            if ( contentProducer == null ) {
                contentProducer = new EmptyProducer();
            }
            if ( contentEncoder == null ) {
                contentEncoder = new IdentityEncoder();
            }
            OutputStream encodedStream = contentEncoder.encode(servletResponse.getOutputStream());
            ByteCountingOutputStream counter = new ByteCountingOutputStream(encodedStream);
            contentEncoder.prepare(this);
            contentProducer.prepare(this);
            contentProducer.produce(counter);
            servletResponse.flushBuffer();
            contentLength = counter.getCount();
        } else {
            servletResponse.flushBuffer();
            contentLength = 0;
        }
    }

    public long getContentLength() {
        if ( contentLength == -1 ) {
            throw new IllegalStateException("Content length unknown until response has been sent.");
        }
        return contentLength;
    }
    
}
