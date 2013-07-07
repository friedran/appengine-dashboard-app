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
package com.friedran.appengine.dashboard.gui;

import android.accounts.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

public class GoogleAuthenticationActivity extends Activity {

    ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Authenticating with your Google AppEngine account...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Context applicationContext = getApplicationContext();
        AccountManager accountManager = AccountManager.get(applicationContext);

        Account defaultAccount = accountManager.getAccounts()[0];

        AppEngineDashboardClient client = new AppEngineDashboardClient(defaultAccount, applicationContext,
            new AppEngineDashboardClient.PostAuthenticateCallback() {
            @Override
            public void run(boolean result) {
                mProgressDialog.dismiss();
                Log.i("GoogleAuthenticationActivity", "Authentication done, result = " + result);

                if (result) {
                    Intent intent = new Intent(GoogleAuthenticationActivity.this, DashboardActivity.class);
                    startActivity(intent);
                } else {
                    // TODO: Display an error message and offer to retry.
                }
            }
        });

        AppEngineDashboardAPI appEngineAPI = AppEngineDashboardAPI.getInstance();
        appEngineAPI.setClient(defaultAccount, client);

        client.executeAuthentication();
        mProgressDialog.show();
    }

}