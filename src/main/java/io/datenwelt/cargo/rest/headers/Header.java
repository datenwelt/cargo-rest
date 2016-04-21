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
package io.datenwelt.cargo.rest.headers;

import io.datenwelt.cargo.rest.utils.Rfc2047;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author job
 */
public class Header {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                .withZoneUTC().withLocale(Locale.US);
    
    private final String name;
    private final List<String> values = new ArrayList<>();
    
    public Header(String name) {
        this.name = normalizeName(name);
    }
    
    public static String normalizeName(String name) {
        String parts[] = name.split("\\-");
        String normalized = Arrays.stream(parts)
                .map((el) -> {
                    return (el.length() > 1)
                            ? (el.substring(0, 1).toUpperCase() + el.substring(1).toLowerCase())
                            : el.toUpperCase();
                })
                .reduce("", (identity, el) -> {
                    return identity += (identity.isEmpty() ? el : ("-" + el));                    
                });
        return normalized;
    }
    
    public static Header decode(String name, String value) {
        if (value == null || value.isEmpty()) {
            return new Header(name);
        }
        Header header = new Header(name);
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String decoded = Rfc2047.decodeHeader(token.trim());
            header.values.add(decoded);
        }
        return header;
    }
    
    public static Header create(String name, String value) {
        if (value == null || value.isEmpty()) {
            return new Header(name);
        }
        Header header = new Header(name);
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            header.values.add(token.trim());
        }
        return header;
    }
    
    public String getName() {
        return name;
    }
    
    public void set(Header header) {
        values.clear();
        add(header);
    }
    
    public void set(String value) {
        values.clear();
        add(value);
    }

    public void set(int value) {
        values.clear();
        add(value);
    }
    
    public void set(DateTime timeValue) {
        values.clear();
        add(timeValue);
    }
    
    public void add(String input) {
        Header newHeader = Header.create(name, input);
        values.addAll(newHeader.values);
    }
    
    public void add(Header input) {
        values.addAll(input.values);
    }
    
    public void add(int value) {
        values.add(Integer.toString(value));
    }
    
    public void add(DateTime timeValue) {
        String value = DATE_FORMAT.print(timeValue);
        values.add(value);
    }
    
    public List<String> asList() {
        return values;
    }
    
    public String get() {
        if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }
    
    public String combined() {
        return values.stream()
                .reduce("", (identity, el) -> {
                    return identity += identity.isEmpty() ? el : (", " + el);
                });
    }
    
    public String encoded() {
        StringBuilder encoded = new StringBuilder();
        Iterator<String> it = values.iterator();
        while ( it.hasNext() ) {
            String value = it.next();
            encoded.append(Rfc2047.encodeHeader(value));
            if ( it.hasNext() ) {
                encoded.append(", ");
            }
        }
        return encoded.toString();
    }
    
    public String asString() {
        if ( values.isEmpty() ) {
            return "";
        }
        return values.get(0);
    }
    
    public int asInt() {
        if ( values.isEmpty() ) {
            return 0;
        } else {
            try {
                return Integer.parseInt(values.get(0));
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }
    
    public DateTime asDate() {
        if ( values.isEmpty()) {
            return null;
        }
        try {
            return DATE_FORMAT.parseDateTime(values.get(0));
        } catch (Exception ex) {
            return null;
        }
    }
    
}
