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

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author job
 */
public class ByteCountingOutputStream extends OutputStream {

    private long count = 0;
    private final OutputStream destination; 

    public ByteCountingOutputStream(OutputStream destination) {
        this.destination = destination;
    }
    
    @Override
    public void write(int b) throws IOException {
        destination.write(b);
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        destination.write(b, off, len); 
        count += len;
    }

    @Override
    public void write(byte[] b) throws IOException {
        destination.write(b);
        count += b.length;
    }
    
    public long getCount() {
        return count;
    }
    
    
}
