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
package io.datenwelt.cargo.rest.content.empty;

import io.datenwelt.cargo.rest.Response;
import io.datenwelt.cargo.rest.content.ContentProducer;
import java.io.OutputStream;

/**
 *
 * @author job
 */
public class EmptyProducer implements ContentProducer {

    @Override
    public void prepare(Response response) {
        response.headers().remove("Content-Type");
        response.removeBody();
    }

    @Override
    public void produce(OutputStream outputStream) {}
    
}
