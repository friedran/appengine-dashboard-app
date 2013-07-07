/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.friedran.appengine.dashboard.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Some utility methods to parse AppEngine data and returns various details from it.
 */
public class AppEngineParserUtils {
    private static final Pattern APPLICATIONS_PATTERN = Pattern.compile("<a\\s+([^>]*\\s+)?href=\"/dashboard\\?\\&app_id=s\\~([^>\"]+)\"");

    public static ArrayList<String> getApplicationIDs(InputStream in) {
        return getMatches(in, APPLICATIONS_PATTERN);
    }

    private static ArrayList<String> getMatches(InputStream in, Pattern pattern) {
        ArrayList<String> matches = new ArrayList<String>();
        Scanner scanner = new Scanner(in, "UTF-8");
        String match = "";
        while (match != null) {
            match = scanner.findWithinHorizon(pattern, 0);
            if (match != null) {
                matches.add(scanner.match().group(2));
            }
        }

        return matches;
    }
}
