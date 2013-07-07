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
package com.friedran.appengine.dashboard.client;

import android.accounts.Account;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class containing every AppEngineDashboardClients per account
 */
public class AppEngineDashboardAPI {

    private static volatile AppEngineDashboardAPI sInstance;
    private static final Object sInstanceLock = new Object();

    private Map<Account, AppEngineDashboardClient> mAppEngineClients;

    public static AppEngineDashboardAPI getInstance() {
        try {
            if (sInstance == null) {
                synchronized (sInstanceLock) {
                    if (sInstance == null)
                        sInstance = new AppEngineDashboardAPI();
                }
            }

            return sInstance;
        } catch (Exception e) {
            Log.e("AppEngineDashboardAPI", "Couldn't create the AppEngineDashboardAPI instance", e);
            return null;
        }
    }

    private AppEngineDashboardAPI() {
        mAppEngineClients = new HashMap<Account, AppEngineDashboardClient>();
    }


    public void setClient(Account account, AppEngineDashboardClient client) {
        mAppEngineClients.put(account, client);
    }

    public AppEngineDashboardClient getClient(Account account) {
        return mAppEngineClients.get(account);
    }
}
