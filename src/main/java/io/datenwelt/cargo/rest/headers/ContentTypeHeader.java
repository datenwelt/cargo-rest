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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;

/**
 *
 * @author job
 */
public class ContentTypeHeader extends Header {

    private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("^(\\S+\\/[^\\s;,]+)");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([^\\s;,]+)");

    private String mediaType;
    private Optional<String> charset;

    public ContentTypeHeader() {
        super("Content-Type");
    }

    @Override
    public void add(DateTime timeValue) {
        throw new UnsupportedOperationException("Content type header supports only string values.");
    }

    @Override
    public void add(int value) {
        throw new UnsupportedOperationException("Content type header supports only string values.");
    }

    @Override
    public void add(Header input) {
        set(input.get());
    }

    @Override
    public void add(String input) {
        super.add(input);
        if ( input == null ) { return; }
        Matcher m = MEDIA_TYPE_PATTERN.matcher(input);
        if ( !m.find() ) {
            throw new IllegalArgumentException("Content-Type header needs at least one media-type value.");
        }
        mediaType = m.group(1).toLowerCase();
        m = CHARSET_PATTERN.matcher(input);
        if ( m.find() ) {
            charset=Optional.of(m.group(1).trim().toLowerCase());
        } else {
            charset=Optional.empty();
        }
    }

    @Override
    public void set(DateTime timeValue) {
        throw new UnsupportedOperationException("Content type header supports only string values.");
    }

    @Override
    public void set(int value) {
        throw new UnsupportedOperationException("Content type header supports only string values.");
    }

    @Override
    public void set(String value) {
        super.set(value);
    }

    @Override
    public void set(Header header) {
        set(header.get());
    }

    public String mediaType() {
        return mediaType;
    }

    public Optional<String> charset() {
        return charset;
    }
    
    public String normalized() {
        return mediaType + (charset.isPresent() ? ("; charset=" + charset.get()) : "");
    }

    public void change(String mediaType, String charset) {
        this.mediaType = mediaType.toLowerCase().trim();
        this.charset = charset == null || charset.isEmpty() ? Optional.empty() : Optional.of(charset);
        set(normalized());
    }

    public void change(String mediaType) {
        change(mediaType, null);
    }

    public boolean matches(String mediaRange) {
        String[] parts = mediaRange.split("/");
        if ( parts.length != 2 ) {
            throw new IllegalArgumentException("Not a media range. Media ranges are have a format of \"*/*\" e.g. \"text/plain\".");
        }
        String type = parts[0];
        String subType = parts[1];
        if ( !type.equals("*") && !mediaType.startsWith(type + "/") ) {
            return false;
        }
        if ( !subType.equals("*") && !mediaType.endsWith("/" + subType) ) {
            return false;
        }
        return true;
    }
    
}
