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

import io.datenwelt.cargo.rest.path.errors.URITemplateMismatchException;
import io.datenwelt.cargo.rest.path.errors.InvalidURITemplateException;
import static io.datenwelt.cargo.rest.path.Segment.SEPARATOR;
import static io.datenwelt.cargo.rest.path.Segment.VALID_CHARS;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author job
 */
public class SegmentMatcher {

    public final static Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9]*");
    public final static String VALID_VARIABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWabcdefghijklmnopqrstuvwxyz0123456789";
    public final static char VARIABLE_OPEN = '{';
    public final static char VARIABLE_CLOSE = '}';

    public static final SegmentMatcher CURRENT_SEGMENT = new SegmentMatcher(Pattern.compile("\\Q/.\\E"), "/.", new ArrayList<>());
    public static final SegmentMatcher EMPTY_SEGMENT = new SegmentMatcher(Pattern.compile("\\Q/\\E"), "/.", new ArrayList<>());
    public static final SegmentMatcher ROOT_SEGMENT = EMPTY_SEGMENT;
    public static final SegmentMatcher PREVIOUS_SEGMENT = new SegmentMatcher(Pattern.compile("\\Q/..\\E"), "/.", new ArrayList<>());
    
    private final Pattern pattern;
    private final String definition;
    private final List<String> variableNames;

    protected SegmentMatcher(Pattern pattern, String definition, List<String> variableNames) {
        this.pattern = pattern;
        this.definition = definition;
        this.variableNames = new ArrayList<>(variableNames);
    }

    public static SegmentMatcher root() {
        try {
            return SegmentMatcher.parse("/");
        } catch (InvalidURITemplateException ex) {
            return null;
        }
    }
    
    public static SegmentMatcher parse(String segment) throws InvalidURITemplateException {
        if (segment == null) {
            segment = "";
        }
        try {
            int pos = 0;
            List<String> variables = new ArrayList<>();
            StringBuilder pattern = new StringBuilder("\\Q" + SEPARATOR.toString());
            StringBuilder definition = new StringBuilder(SEPARATOR.toString());
            if (segment.length() >= 1 && segment.charAt(0) == SEPARATOR) {
                pos++;
            }
            while (pos < segment.length()) {
                Character currentChar = segment.charAt(pos);
                if (VALID_CHARS.indexOf(currentChar) != -1) {
                    pattern.append(currentChar);
                    definition.append(currentChar);
                } else if (currentChar == VARIABLE_OPEN) {
                    definition.append(currentChar);
                    StringBuilder variableName = new StringBuilder();
                    while (currentChar != VARIABLE_CLOSE) {
                        pos++;
                        if (pos == segment.length()) {
                            throw new InvalidURITemplateException("Unclosed path variable in URI template: " + segment);
                        }
                        currentChar = segment.charAt(pos);
                        definition.append(currentChar);
                        if (currentChar == '/') {
                            throw new InvalidURITemplateException("Unclosed path variable in URI template: " + segment);
                        }
                        if (currentChar == VARIABLE_CLOSE) {
                            pattern.append("\\E(.+)\\Q");
                            Matcher m = VARIABLE_PATTERN.matcher(variableName);
                            if (!m.matches()) {
                                throw new InvalidURITemplateException("Invalid path variable name '" + variableName + "' in URI template: " + segment);
                            }
                            variables.add(variableName.toString());
                            break;
                        } else {
                            variableName.append(currentChar);
                        }
                    }
                } else {
                    byte[] bytes = currentChar.toString().getBytes("UTF-8");
                    pattern.append("%");
                    definition.append("%");
                    for (Byte b : bytes) {
                        pattern.append(Integer.toString(b.intValue(), 16).toUpperCase());
                        definition.append(Integer.toString(b.intValue(), 16).toUpperCase());
                    }
                }
                pos++;
            }
            pattern.append("\\E");
            return new SegmentMatcher(Pattern.compile(pattern.toString()), definition.toString(), variables);
        } catch (UnsupportedEncodingException ex) {
            throw new Error("Your runtime must support UTF-8 to run this module.", ex);
        }
    }

    public static SegmentMatcher[] parseSegments(String input) throws InvalidURITemplateException {
        String[] rawSegments = Segment.split(input);
        SegmentMatcher[] templates = new SegmentMatcher[rawSegments.length];
        try {
            for (int idx = 0; idx < rawSegments.length; idx++) {
                templates[idx] = SegmentMatcher.parse(rawSegments[idx]);
            }
        } catch (InvalidURITemplateException ex) {
            throw new InvalidURITemplateException("Unable to parse URI template '" + input + "': " + ex.getMessage(), ex);
        }
        return templates;
    }
    
    public List<PathParameter> match(String input) throws URITemplateMismatchException {
        Segment segment = Segment.parse(input);
        return match(segment);
    }

    public List<PathParameter> match(Segment input) throws URITemplateMismatchException {
        String segment = input.toString();
        Matcher m = pattern.matcher(segment);
        if ( !m.matches() ) {
            throw new URITemplateMismatchException("Segment '" + segment + "' does not match URI template '" + definition + "'.");
        }
        List<PathParameter> params = new ArrayList<>();
        for ( int idx=0; idx<variableNames.size(); idx++) {
            String variableValue = m.group(idx+1);
            PathParameter param = new PathParameter(variableNames.get(idx), variableValue);
            params.add(param);
        }
        return params;
    }
    
    public boolean mismatch(String input) {
        return mismatch(Segment.parse(input));
    }
    
    public boolean mismatch(Segment input) {
        try { 
            match(input);
            return false;
        } catch (URITemplateMismatchException ex) {
            return true;
        }
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getDefinition() {
        return definition;
    }

    public List<String> getVariableNames() {
        return new ArrayList<>(variableNames);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.pattern);
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
        final SegmentMatcher other = (SegmentMatcher) obj;
        return Objects.equals(this.pattern.pattern(), other.pattern.pattern());
    }

    @Override
    public String toString() {
        return definition;
    }

}
