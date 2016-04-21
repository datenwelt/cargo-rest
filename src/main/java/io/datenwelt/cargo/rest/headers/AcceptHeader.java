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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;

/**
 *
 * @author job
 */
public class AcceptHeader extends Header {

    private final TreeSet<Value> values = new TreeSet<>();

    public AcceptHeader(String name) {
        super(name);
    }
    
    @Override
    public DateTime asDate() {
        throw new UnsupportedOperationException("Number values not supported for Accept headers.");
    }

    @Override
    public int asInt() {
        throw new UnsupportedOperationException("Number values not supported for Accept headers.");
    }

    @Override
    public void add(DateTime timeValue) {
        throw new UnsupportedOperationException("Date values not supported for Accept headers.");
    }

    @Override
    public void add(int value) {
        throw new UnsupportedOperationException("Number values not supported for Accept headers.");
    }

    @Override
    public void set(DateTime timeValue) {
        throw new UnsupportedOperationException("Date values not supported for Accept headers.");
    }

    @Override
    public void set(int value) {
        throw new UnsupportedOperationException("Number values not supported for Accept headers.");
    }

    @Override
    public void add(Header input) {
        super.add(input);
        updateValueList();
    }

    @Override
    public void add(String input) {
        super.add(input);
        values.clear();
        updateValueList();
    }

    private void updateValueList() {
        values.clear();
        List<String> headerValues = asList();
        headerValues.stream().map((value) -> Value.parse(value)).forEach((v) -> {
            values.add(v);
        });

    }

    @Override
    public void set(String value) {
        super.set(value);
        updateValueList();
    }

    @Override
    public void set(Header header) {
        super.set(header);
        updateValueList();
    }

    public TreeSet<Value> values() {
        return values;
    }
    
    public List<Value> accepts() {
        List<Value> accepted = new ArrayList<>();
        values.forEach((v) -> {
            if ( v.q > 0.0f ) 
                accepted.add(v);
        });
        return accepted;
    }
    
    public List<Value> rejects() {
        List<Value> rejected = new ArrayList<>();
        values.forEach((v) -> {
            if ( v.q == 0.0f ) 
                rejected.add(v);
        });
        return rejected;
    }
    
    public boolean accepts(String input) {
        Value accepted = null;
        for ( Value checkValue : accepts() ) {
            if ( checkValue.matches(input) ) {
                if ( accepted == null ) {
                    accepted = checkValue;
                } else {
                    if ( checkValue.precedence < accepted.precedence ) {
                        accepted = checkValue;
                    }
                }
            }
        }
        if ( accepted == null ) {
            return false;
        }
        for ( Value checkValue : rejects() ) {
            if ( checkValue.matches(input) && checkValue.precedence < accepted.precedence ) {
                return false;
            }
        }
        return true;
    }

    
    public static class Value implements Comparable<Value> {

        public static final Pattern VALUE_PATTERN = Pattern.compile("^\\s*([^\\s,;]+)");
        public static final Pattern Q_PATTERN = Pattern.compile("q=([^;,]+)");

        public final float q;
        public final int precedence;
        public final String value;
        public final Pattern pattern;

        public Value(String value, float q, int relevance) {
            this.q = q;
            this.precedence = relevance;
            this.value = value;
            this.pattern = compilePattern(value);
        }

        public static Value parse(String input) {
            Matcher m = VALUE_PATTERN.matcher(input);
            if (!m.find()) {
                throw new IllegalArgumentException("Header value cannot be parsed as an Accept header: " + input);
            }
            String value = m.group(1).trim();
            char[] chars = value.toCharArray();
            int stars = 0;
            for (int idx = 0; idx < chars.length; idx++) {
                if (chars[idx] == '*') {
                    stars++;
                }
            }
            m = Q_PATTERN.matcher(input);
            float q;
            if (m.find()) {
                try {
                    q = Float.parseFloat(m.group(1));
                    if (q < 0.0f) {
                        q = 0;
                    }
                    if (q > 1.0f) {
                        q = 1;
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Unable to parse quality factor (q=...) from string: " + input);
                }
            } else {
                q = 1.0f;
            }
            return new Value(value, q, stars);
        }
        
        private static Pattern compilePattern(String input) {
            StringBuilder patternBuilder = new StringBuilder("\\Q");
            for ( int idx=0; idx<input.length(); idx++) {
                char c = input.charAt(idx);
                if ( c == '*' ) {
                    patternBuilder.append("\\E.+\\Q");
                } else {
                    patternBuilder.append(c);
                }
            }
            patternBuilder.append("\\E");
            return Pattern.compile(patternBuilder.toString());
        }

        public float getQ() {
            return q;
        }

        public int getPrecedence() {
            return precedence;
        }

        public String getValue() {
            return value;
        }
        
        public boolean matches(String input) {
            input = input.trim().toLowerCase();
            return pattern.matcher(input).matches();
        }

        @Override
        public int compareTo(Value o) {
            if (o == null) {
                throw new NullPointerException();
            }
            if (q > o.q) {
                return -1;
            }
            if (q < o.q) {
                return 1;
            }
            if (precedence > o.precedence) {
                return -1;
            }
            if (precedence < o.precedence) {
                return 1;
            }
            return 0;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + Objects.hashCode(this.value);
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
            final Value other = (Value) obj;
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }
        
        

        @Override
        public String toString() {
            return value + (q == 1.0f ? "" : "; q=" + q);
        }
        
    }

}
