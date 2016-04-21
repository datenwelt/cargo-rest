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
package io.datenwelt.cargo.rest.examples;

import io.datenwelt.cargo.rest.Endpoint;
import io.datenwelt.cargo.rest.response.NotFound;
import io.datenwelt.cargo.rest.response.OK;

/**
 *
 * @author job
 */
public class TestPersonEndpoints {

    public static final Endpoint GET_PERSON = (req) -> {
        String firstname = req.param("firstname").get();
        String lastname = req.param("lastname").get();
        TestPerson person = TestPerson.sample();
        return (person.getFirstname().equals(firstname) && person.getLastname().equals(lastname))
                ? new OK(person)
                : new NotFound();
    };
    
}
