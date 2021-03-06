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

/**
 *
 * @author job
 */
public class NotFound extends Response {
    
    public static final int CODE = 404;
    public static final String REASON = "Not Found";
    
    public NotFound() {
        super(CODE);
    }

    public NotFound(Object body) {
        super(CODE, body);
    }
    
}
