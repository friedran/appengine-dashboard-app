package com.friedran.appengine.dashboard.client;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
