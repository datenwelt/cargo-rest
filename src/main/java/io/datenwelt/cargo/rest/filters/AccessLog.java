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

import io.datenwelt.cargo.rest.Filter;
import io.datenwelt.cargo.rest.Request;
import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.headers.Header;
import java.util.Locale;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author job
 */
public class AccessLog implements Filter {

    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.US);

    private final Logger logger;

    public AccessLog(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    public AccessLog() {
        this(AccessLog.class.getName());
    }

    @Override
    public void finish(Optional<Request> optionalRequest, Response response) {
        if (!optionalRequest.isPresent()) {
            return;
        }
        Request request = optionalRequest.get();
        StringBuilder logline = new StringBuilder();
        String requestId = "REQ:" + request.getBaseURI().getPath() + ":" + request.getRequestId();
        logline.append("[").append(requestId).append("]").append(" ");
        logline.append(request.getRemoteHost()).append(" ");
        logline.append("- - ");
        logline.append("[").append(LOG_TIME_FORMAT.print(DateTime.now())).append("] ");
        logline.append("\"").append(request.toString()).append(" ")
                .append("HTTP/1.1")
                .append("\" ");
        logline.append(response.getStatus()).append(" ");
        String contentLength;
        try {
            contentLength = Long.toString(response.getContentLength());
        } catch (RuntimeException ex) {
            contentLength = "-";
        }
        logline.append(contentLength).append(" ");
        Optional<Header> referer = request.header("Referer");
        if (referer.isPresent()) {
            logline.append("\"").append(referer.get().asString()).append("\" ");
        } else {
            logline.append("- ");
        }
        Optional<Header> ua = request.header("User-Agent");
        if (ua.isPresent()) {
            logline.append("\"").append(ua.get().asString()).append("\" ");
        } else {
            logline.append("-");
        }
        this.logger.info(logline.toString());
    }

}
