package com.friedran.appengine.dashboard.utils;

import android.content.Context;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class AnalyticsUtils {

    private static final String TRACKER_ID = "UA-42449637-1";

    public static Tracker getTracker(Context context) {
        return GoogleAnalytics.getInstance(context).getTracker(TRACKER_ID);
    }
}
