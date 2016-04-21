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
package io.datenwelt.cargo.rest.test.utils;

import com.meterware.httpunit.HeaderOnlyWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import io.datenwelt.cargo.rest.examples.TestAPI;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author job
 */
public class ServletUtils {

    public static HttpServletRequest createRequest(String method, String path) throws IOException {
        ServletRunner servletRunner = new ServletRunner();
        servletRunner.registerServlet("/*", TestAPI.class.getName());
        ServletUnitClient client = servletRunner.newClient();
        WebRequest request = new TestWebRequest(method, path);
        InvocationContext ic = client.newInvocation(request);
       
        HttpServletRequest servletRequest = ic.getRequest();
        return servletRequest;
    }
    
    public static class TestWebRequest extends HeaderOnlyWebRequest {

        public TestWebRequest(String method, String path) {
            super("http://localhost" + path);
            this.method = method;
        }
    
        
        
    }
    
}


