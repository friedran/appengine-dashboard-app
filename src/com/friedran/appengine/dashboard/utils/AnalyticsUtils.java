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

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class AnalyticsUtils {

    public static final String ANONYMOUS_IDENTIFIER = "Anonymous";

    private static final String GA_TRACKER_ID = "UA-42449637-1";
    private static final String BUGSENSE_API_KEY = "a95edbf9";

    public static Tracker getTracker(Context context) {
        return GoogleAnalytics.getInstance(context).getTracker(GA_TRACKER_ID);
    }

    public static void sendEvent(Tracker tracker, String category, String event, String value, Long timing) {
        tracker.sendEvent(category, event, value, timing);
        BugSenseHandler.leaveBreadcrumb(String.format("%s - %s (%s)", category, event, value));
    }

    public static void initBugSense(Context context) {
        BugSenseHandler.initAndStartSession(context, BUGSENSE_API_KEY);
    }

    public static void setBugSenseUserIdentifier(String userIdentifier) {
        BugSenseHandler.setUserIdentifier(userIdentifier);
    }

}
