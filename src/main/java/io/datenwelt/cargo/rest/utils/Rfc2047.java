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
package io.datenwelt.cargo.rest.utils;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.StringTokenizer;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.codec.net.QCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes and decodes header values conforming to RFC2047 (aka "Encoded-Word").
 * The HTTP/1.1 protocol definition states that header values should be in
 * ISO-8895-1 by default. RFC2047 should be used to encode header values which
 * use characters that cannot be encoded by ISO-8895-1.
 * <p>
 * Most servlet containers expect headers to be in ISO-8859-1 and all official
 * HTTP headers follow this expectation. However, to support custom headers we
 * decode incoming headers conforming to RFC2047 to the internal string
 * representation of Java. When decoding we assume that the header values
 * provided by HttpServletRequest are already converted from ISO-8859-1 to the
 * internal string representation of Java (which is Unicode).
 * <p>
 * This would leave all occurences of RFC2047 encoded values in place because
 * these values are plain ASCII. It is safe to decode them to the internal
 * string representation as long as the charset is supported by Java. The
 * decoder scans the input string for occurences of words with an opening part {@code =?}
 * and a closing part {@code ?=} which indicates a possible RFC2047 encoded word. 
 * It "tries its best" to decode it by extracting the charset and the encoding
 * (which can be "B" for Base64 or "Q" for Q encoding) and decoding the payload
 * according to that information. If anything fails - i.e. malformed encoded
 * strings or unsupported character sets, it skips the decoding of the entire
 * encoded word and uses it verbatim in its encoded form instead. A debugging
 * log message is written when this happens.
 * <p>
 * During encoding, the input string is split into words. Each word is tried to
 * encode in ISO-8859-1. If the encoding is possible, the word is used verbatim.
 * If the word cannot be represented as ISO-8859-1, it is encoded using Q
 * encoding. To preserve common separators the following characters are
 * considered to be word boundaries and never part of an encoded word:
 * <p>
 * ,;:-/=+#
 *
 * <p>
 * Q encoding has been preferred over BASE64 for better readability. If you try
 * to encode binary or e.g. JSON in a header value, encode it to BASE64
 * <b>before</b>
 * setting the header value. Since BASE64 is encodeable in ISO-8859-1, it is
 * untouched by this implementation.
 *
 * @author job
 */
public class Rfc2047 {

    private static final Logger LOG = LoggerFactory.getLogger(Rfc2047.class);

    public static String decodeHeader(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input, " \t", true);
        StringBuilder decoded = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.startsWith("=?") || !token.endsWith("?=")) {
                decoded.append(token);
                continue;
            }
            String encodedWord = token;
            String[] parts = encodedWord.substring(2, encodedWord.length() - 2).split("\\?", 3);
            if (parts.length != 3) {
                decoded.append(token);
                continue;
            }
            Charset charset;
            try {
                charset = Charset.forName(parts[0]);
            } catch (Exception ex) {
                LOG.debug("Skipping the decoding of a header value due to an unknown charset \"{}\" found in: ", parts[1], input, ex);
                decoded.append(token);
                continue;
            }
            String encoding = parts[1].toUpperCase();
            switch (encoding) {
                case "B":
                    BCodec bcodec = new BCodec(charset);
                    try {
                        decoded.append(bcodec.decode(encodedWord));
                    } catch (Exception ex) {
                        LOG.debug("Skipping the decoding of BASE64 value from string \"{}\" found in: ", encodedWord, input, ex);
                        decoded.append(token);
                    }
                    break;
                case "Q":
                    QCodec qcodec = new QCodec(charset);
                    try {
                        decoded.append(qcodec.decode(encodedWord));
                    } catch (Exception ex) {
                        LOG.debug("Skipping the decoding of Q encoded value from string \"{}\" found in: ", encodedWord, input, ex);
                        decoded.append(token);
                    }
                    break;
                default:
                    LOG.debug("Skipping the decoding of value from unknown encoding \"{}\" found in: ", encodedWord, input);
                    decoded.append(token);
            }
        }
        return decoded.toString();
    }

    public static String encodeHeader(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input, "\t ,;:-/=+#*", true);
        CharsetEncoder charsetEncoder = Charset.forName(CharEncoding.ISO_8859_1).newEncoder();
        QCodec qcodec = new QCodec(Charset.forName(CharEncoding.UTF_8));
        StringBuilder encoded = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!charsetEncoder.canEncode(token)) {
                try {
                    encoded.append(qcodec.encode(token));
                } catch (EncoderException ex) {
                    LOG.debug("Skipping the Q encoding of header value for non ISO-8859-1 string: {}", input, ex);
                    encoded.append(token);
                }
            } else {
                encoded.append(token);
            }
        }
        return encoded.toString();
    }

}
