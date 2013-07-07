package com.friedran.appengine.dashboard.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Some utility methods to parse AppEngine data and returns various details from it.
 */
public class AppEngineParserUtils {
    private static final Pattern APPLICATIONS_PATTERN = Pattern.compile("<a\\s+([^>]*\\s+)?href=\"/dashboard\\?\\&app_id=s\\~([^>\"]+)\"");

    public static List<String> getApplicationIDs(InputStream in) {
        return getMatches(in, APPLICATIONS_PATTERN);
    }

    private static List<String> getMatches(InputStream in, Pattern pattern) {
        List<String> matches = new ArrayList<String>();
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
