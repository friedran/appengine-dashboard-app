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

import android.util.Log;

public class LogUtils {
    private static final boolean LOGGING = false;

    public static void d(String tag, String message) {
        if (LOGGING) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LOGGING) {
            Log.i(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (LOGGING) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (LOGGING) {
            Log.e(tag, message, throwable);
        }
    }
}
