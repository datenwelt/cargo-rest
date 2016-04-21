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
package io.datenwelt.cargo.rest.content.yaml;

import io.datenwelt.cargo.rest.Response;
import java.io.OutputStream;
import io.datenwelt.cargo.rest.content.ContentProducer;
import io.datenwelt.cargo.rest.serialization.Yaml;
import java.io.IOException;

/**
 *
 * @author job
 */
public class YamlProducer implements ContentProducer {

    public static final String CONTENT_TYPE = "application/x-yaml";
    public static final String CHARSET = "utf-8";
    
    private Object body;

    @Override
    public void prepare(Response response) {
        response.header("Content-Type", CONTENT_TYPE + "; charset=" + CHARSET);
        body = response.body().orElse(null);
    }

    @Override
    public void produce(OutputStream outputStream) throws IOException {
        Yaml.serialize(outputStream, body);
    }
    
    
    
}
