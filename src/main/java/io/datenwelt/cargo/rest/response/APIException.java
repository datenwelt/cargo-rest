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
package io.datenwelt.cargo.rest.response;

import io.datenwelt.cargo.rest.Response;
import java.util.Optional;

/**
 *
 * @author job
 */
public class APIException extends Exception {

    private final Response response;

    public APIException(Response response) {
        super();
        this.response = Optional.ofNullable(response).orElse(new InternalServerError());
    }

    public APIException(Response response, String message) {
        super(message);
        this.response = Optional.ofNullable(response).orElse(new InternalServerError());
    }

    public APIException(Response response, String message, Throwable cause) {
        super(message, cause);
        this.response = Optional.ofNullable(response).orElse(new InternalServerError());
    }

    public APIException(Response response, Throwable cause) {
        super(cause);
        this.response = Optional.ofNullable(response).orElse(new InternalServerError());
    }

    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder();
        if ( response!=null ) {
            msg.append("(HTTP ").append(response.getStatus()).append(")");
        }
        msg.append(super.getMessage());
        return msg.toString();
    }

    public Response getResponse() {
        return response;
    }

    
}
