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

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author job
 */
public class Strings {

    private static Random random = new SecureRandom();
    
    public static String token() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 100);
        for (int i = 0; i < 100; i++) {
            buffer.putLong(random.nextLong());
        }
        return DigestUtils.md5Hex(buffer.array());
    }

    private static final String UNIQID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String uniqid() {
        StringBuilder uniqid = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i == 6) {
                uniqid.append("-");
            }
            int idx = random.nextInt(UNIQID_CHARS.length());
            uniqid.append(UNIQID_CHARS.charAt(idx));
        }
        return uniqid.toString();
    }
    
    public static boolean isBlank(String input) {
        return input == null || input.isEmpty();
    }

}
