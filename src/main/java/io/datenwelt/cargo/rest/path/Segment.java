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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author job
 */
public class Segment {

    private static final Logger LOG = LoggerFactory.getLogger(Segment.class);

    public static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~!$&'()*+,;=:@";
    public static final Pattern PCT_ENCODED = Pattern.compile("^(%[A-Fa-f0-9]{2}).*");
    public static final Character SEPARATOR = '/';
    
    public static final Segment CURRENT_SEGMENT = new Segment("/.");
    public static final Segment EMPTY_SEGMENT = new Segment("/");
    public static final Segment PREVIOUS_SEGMENT = new Segment("/..");

    private final String segment;

    protected Segment(String segment) {
        this.segment = segment;
    }

    public static Segment root() {
        return new Segment(SEPARATOR.toString());
    }
    
    public static Segment current() {
        return new Segment("/.");
    }

    public static Segment up() {
        return new Segment("/..");
    }
    
    
    public static Segment parse(String segment) {
        if (segment == null) {
            segment = "";
        }
        try {
            int pos = 0;
            StringBuilder seg = new StringBuilder(SEPARATOR.toString());
            if (segment.length() >= 1 && segment.charAt(0) == SEPARATOR) {
                pos++;
            }
            while (pos < segment.length()) {
                Character currentChar = segment.charAt(pos);
                if (VALID_CHARS.indexOf(currentChar) != -1) {
                    seg.append(currentChar);
                } else {
                    if (currentChar == '%' && pos + 3 <= segment.length()) {
                        Matcher m = PCT_ENCODED.matcher(segment.substring(pos));
                        if (m.matches()) {
                            seg.append(m.group(1).toUpperCase());
                            pos += 3;
                            continue;
                        }
                    }
                    byte[] bytes = currentChar.toString().getBytes("UTF-8");
                    seg.append("%");
                    for (Byte b : bytes) {
                        seg.append(Integer.toString(b.intValue(), 16).toUpperCase());
                    }
                }
                pos++;
            }
            return new Segment(seg.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new Error("Your runtime must support UTF-8 to run this module.", ex);
        }
    }

    public static Segment[] parseSegments(String input) {
        List<Segment> parts = new ArrayList<>();
        Scanner scanner = new Scanner(input);
        while (scanner.hasNext()) {
            String part = scanner.next();
            Segment segment = Segment.parse(part);
            parts.add(segment);
        }
        return parts.toArray(new Segment[0]);
    }

    public static String[] split(String input) {
        List<String> parts = new ArrayList<>();
        Scanner scanner = new Scanner(input);
        while (scanner.hasNext()) {
            String part = scanner.next();
            parts.add(part);
        }
        return parts.toArray(new String[0]);
    }
    
    public static Segment[] normalize(Segment[] segments) {
        List<Segment> segmentList = new ArrayList<>();
        if ( segments == null || segments.length == 0 ) {
            return new Segment[] { Segment.root() };
        }
        Stream<Segment> stream = Arrays.stream(segments);
        
        stream.forEachOrdered((Segment seg) -> {
            if ( seg.equals(CURRENT_SEGMENT) ) return;
            if ( seg.equals(PREVIOUS_SEGMENT)) {
                if ( !segmentList.isEmpty() )
                    segmentList.remove(segmentList.size()-1);
            } else {
                segmentList.add(seg);
            }
        });
        return segmentList.toArray(new Segment[0]);
    }

    public static String normalize(String path) {
        Segment[] segments = Segment.parseSegments(path);
        segments = Segment.normalize(segments);
        StringBuilder normalized = new StringBuilder();
        for ( Segment seg  : segments ) {
            normalized.append(seg);
        }
        return normalized.toString();
    }

    @Override
    public String toString() {
        return segment;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.segment);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Segment other = (Segment) obj;
        return Objects.equals(this.segment, other.segment);
    }

    public static class Scanner implements Iterator<String> {

        private final String input;
        private int pos = 0;

        public Scanner(String input) {     
            if ( input == null ) {
                input = "/";
            } else if ( !input.startsWith("/") ) {
                input = "/" +input;
            }
            this.input = input;
        }

        @Override
        public String next() {
            if ( !hasNext() ) {
                throw new NoSuchElementException("Unable to call next(). Already beyond end of scanner input.");
            }
            StringBuilder part = new StringBuilder("/");
            for (++pos; pos < input.length(); pos++) {
                char currentChar = input.charAt(pos);
                if (currentChar == SEPARATOR && pos != (input.length() - 1)) {
                    break;
                } else if (currentChar != SEPARATOR) {
                    part.append(currentChar);
                } else {
                    pos = input.length();
                }
            }
            return part.toString();
        }

        public Segment lookAhead() {
            if ( !hasNext() ) {
                return null;
            }
            int oldPos = pos;
            Segment segment = Segment.parse(next());
            pos = oldPos;
            return segment;
        }
        
        public String getInput() {
            return input;
        }

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }
        
        public Scanner terminate() {
            pos = input.length();
            return this;
        }
        
        public String getRemaining() {
            return pos < input.length() ? input.substring(pos) : "";
        }

        @Override
        public boolean hasNext() {
            return pos < input.length() && input.charAt(pos) == '/';
        }

    }

}
