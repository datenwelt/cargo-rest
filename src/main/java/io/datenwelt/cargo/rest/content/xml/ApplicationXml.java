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
package io.datenwelt.cargo.rest.content.xml;

import io.datenwelt.cargo.rest.Request;
import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.content.ContentProducer;
import io.datenwelt.cargo.rest.content.ContentType;
import io.datenwelt.cargo.rest.headers.AcceptHeader;
import java.util.Optional;

/**
 *
 * @author job
 */
public class ApplicationXml implements ContentType {

    @Override
    public Optional<ContentProducer> producerFor(AcceptHeader acceptedMediaTypes, AcceptHeader acceptedCharsets, Request request, Response response) {
        if (request == null || !response.body().isPresent()) {
            return null;
        }
        if (!acceptedMediaTypes.accepts("application/xml") || !acceptedCharsets.accepts("utf-8")) {
            return null;
        }
        return Optional.of(new XmlProducer());
    }
    
}
