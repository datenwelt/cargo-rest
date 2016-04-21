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

import io.datenwelt.cargo.rest.path.errors.InvalidURITemplateException;
import io.datenwelt.cargo.rest.Endpoint;
import io.datenwelt.cargo.rest.path.Segment.Scanner;
import io.datenwelt.cargo.rest.path.errors.URITemplateMismatchException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author job
 */
public class PathRouter {

    protected final SegmentMatcher template;

    protected final List<PathRouter> routers = new ArrayList<>();
    protected final Map<String, Endpoint> endpoints = new HashMap<>();

    public PathRouter(SegmentMatcher template) {
        this.template = template;
    }

    public Scanner register(Scanner scanner, String method, Endpoint endpoint) throws InvalidURITemplateException {
        while (scanner.hasNext()) {
            SegmentMatcher nextSegment = SegmentMatcher.parse(scanner.next());
            if (nextSegment.equals(SegmentMatcher.CURRENT_SEGMENT)) {
                continue;
            }
            if (nextSegment.equals(SegmentMatcher.PREVIOUS_SEGMENT)) {
                return new Scanner(scanner.getRemaining());
            }
            PathRouter router = new PathRouter(nextSegment);
            boolean isNewSegment = true;
            for (PathRouter r : routers) {
                if (r.template.equals(nextSegment)) {
                    router = r;
                    isNewSegment = false;
                    break;
                }
            }
            scanner = router.register(scanner, method, endpoint);
            if (!scanner.hasNext()) {
                if (isNewSegment) {
                    routers.add(router);
                }
                return scanner;
            }
        }
        endpoints.put(method, endpoint);
        return scanner;
    }

    public Map<String, Endpoint> route(Scanner scanner, List<PathParameter> parameters) throws URITemplateMismatchException {
        if (!scanner.hasNext()) {
            return new LinkedHashMap(endpoints);
        }
        Segment next = Segment.parse(scanner.next());
        for (int idx = 0; idx < routers.size(); idx++) {
            PathRouter router = routers.get(idx);
            int scannerPos = scanner.getPos();
            List<PathParameter> matchedParams;
            try {
                matchedParams = router.getTemplate().match(next);
            } catch (URITemplateMismatchException ex) {
                scanner.setPos(scannerPos);
                continue;
            }
            Map<String, Endpoint> endpointsFound = router.route(scanner, matchedParams);
            parameters.addAll(matchedParams);
            return endpointsFound;
        }
        return new HashMap<>();
    }

    public SegmentMatcher getTemplate() {
        return template;
    }

    public Map<String, Endpoint> getEndpoints() {
        return new LinkedHashMap<>(endpoints);
    }

    public List<PathRouter> getRouters() {
        return routers;
    }

    @Override
    public String toString() {
        return template.toString();
    }

}
