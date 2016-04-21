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
package io.datenwelt.cargo.rest.query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author job
 */
public class Query {

    private final static Logger LOG = LoggerFactory.getLogger(Query.class);

    public static final Pattern PCT_ENCODED = Pattern.compile("^%([A-Fa-f0-9]{2}).*");

    private final String key;
    private Optional<String> value;

    public Query(String key) {
        this.key = key;
        this.value = Optional.empty();
    }

    public Query(String key, String value) {
        this.key = key;
        if (value == null || value.isEmpty()) {
            this.value = Optional.empty();
        } else {
            this.value = Optional.of(value);
        }
    }

    public String getKey() {
        return key;
    }

    public Optional<String> getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = Optional.ofNullable(value);
    }

    public static List<Query> parseQueryString(String input) {
        try {
            List<Query> queries = new ArrayList<>();
            if (input == null) {
                return queries;
            }
            int pos = 0;
            String currentKey = null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while (pos < input.length()) {
                char currentChar = input.charAt(pos);
                // Handle terminals. Note that '=' within the value part is no terminal.
                if (currentKey == null && currentChar == '=') {
                    // We are parsing a key and a value follows.
                    currentKey = buffer.toString("UTF-8");
                    buffer = new ByteArrayOutputStream();
                    pos++;
                    continue;
                } else if (currentKey == null && currentChar == '&') {
                    // We are parsing a key, but no value follows.
                    currentKey = buffer.toString("UTF-8");
                    Query q = new Query(currentKey, null);
                    queries.add(q);
                    buffer = new ByteArrayOutputStream();
                    currentKey = null;
                    pos++;
                    continue;
                } else if (currentChar == '&') {
                    // We are parsing a value and another query starts.
                    String currentValue = buffer.toString("UTF-8");
                    Query q = new Query(currentKey, currentValue);
                    queries.add(q);
                    currentKey = null;
                    buffer = new ByteArrayOutputStream();
                    pos++;
                    continue;
                }

                // Decode pct encoded stuff and write to buffer.
                Matcher m = PCT_ENCODED.matcher(input.substring(pos));
                if (m.matches()) {
                    String hexStr = m.group(1);
                    int byteValue = Integer.parseInt(hexStr, 16);
                    buffer.write(byteValue);
                    pos += 3;
                    continue;
                }

                // Write any other character to buffer as it is.
                String charStr = Character.toString(currentChar);
                try {
                    buffer.write(charStr.getBytes("UTF-8"));
                } catch (IOException ex) {
                    LOG.error("Unable to write character from query string to output buffer: {}", ex.getMessage(), ex);
                    return queries;
                }
                pos++;
            }
            if (currentKey != null) {
                if (buffer.size() == 0) {
                    Query q = new Query(currentKey);
                    queries.add(q);
                } else {
                    String currentValue = buffer.toString("UTF-8");
                    Query q = new Query(currentKey, currentValue);
                    queries.add(q);
                }
            } else {
                currentKey = buffer.toString("UTF-8");
                if (currentKey != null && !currentKey.isEmpty()) {
                    Query q = new Query(currentKey);
                    queries.add(q);
                }
            }
            return queries;
        } catch (UnsupportedEncodingException ex) {
            throw new Error("Your platform does not support UTF-8.");
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(key);
        if (value.isPresent()) {
            str.append("=").append(value.get());
        }
        return str.toString();
    }

}
