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
package io.datenwelt.cargo.rest.content.gzip;

import io.datenwelt.cargo.rest.Request;
import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.content.ContentDecoder;
import io.datenwelt.cargo.rest.content.ContentEncoder;
import io.datenwelt.cargo.rest.content.ContentEncoding;
import io.datenwelt.cargo.rest.headers.AcceptHeader;
import java.util.Optional;

/**
 *
 * @author job
 */
public class GzipEncoding implements ContentEncoding {

    @Override
    public Optional<ContentDecoder> decoderFor(Request request) {
        if ( request.header("Content-Encoding").isPresent() ) {
            if ( request.header("Content-Encoding").get().asString().equalsIgnoreCase("gzip") ) {
                return Optional.of(new GzipDecoder());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ContentEncoder> encoderFor(AcceptHeader acceptedEncodings, Request request, Response response) {
        return ContentEncoding.super.encoderFor(acceptedEncodings, request, response); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
