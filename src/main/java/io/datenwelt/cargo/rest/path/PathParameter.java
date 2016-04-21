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
package io.datenwelt.cargo.rest.path;

import io.datenwelt.cargo.rest.response.APIException;
import io.datenwelt.cargo.rest.response.BadRequest;

/**
 *
 * @author job
 */
public class PathParameter {
    
    private final String name;
    private final String value;

    public PathParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return name + "=" + value;
    }

    public String name() {
        return name;
    }
    
    public String get() throws APIException {
        return value;
    }

    public int getInt() throws APIException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new APIException(new BadRequest("Illegal input value for path parameter " + name  + ": Unable to read value '" + value + "' as integer." ));
        }
    }
    
}

