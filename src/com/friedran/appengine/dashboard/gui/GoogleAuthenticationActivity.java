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
import android.widget.Toast;
import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

public class GoogleAuthenticationActivity extends Activity {

    ProgressDialog mProgressDialog;
    AppEngineDashboardClient mAppEngineClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Authenticating with your Google AppEngine account...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Context applicationContext = getApplicationContext();
        AccountManager accountManager = AccountManager.get(applicationContext);

        Account defaultAccount = accountManager.getAccounts()[0];

        mAppEngineClient = new AppEngineDashboardClient(defaultAccount, applicationContext,
            new AppEngineDashboardClient.PostExecuteCallback() {
            @Override
            public void run(Bundle resultBundle) {
                boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
                Log.i("GoogleAuthenticationActivity", "Authentication done, result = " + result);

                if (result) {
                    onSuccessfulAuthentication();
                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(GoogleAuthenticationActivity.this, "Authentication failed, please try again later", 2000);
                }
            }
        });

        AppEngineDashboardAPI appEngineAPI = AppEngineDashboardAPI.getInstance();
        appEngineAPI.setClient(defaultAccount, mAppEngineClient);

        mAppEngineClient.executeAuthentication();
        mProgressDialog.show();
    }

    private void onSuccessfulAuthentication() {
        mProgressDialog.setMessage("Retrieving list of AppEngine applications...");

        mAppEngineClient.executeGetApplications(new AppEngineDashboardClient.PostExecuteCallback() {
            @Override
            public void run(Bundle resultBundle) {
                mProgressDialog.dismiss();

                boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
                Log.i("GoogleAuthenticationActivity", "GetApplications done, result = " + result);

                if (result) {
                    for (String application : resultBundle.getStringArrayList(AppEngineDashboardClient.KEY_APPLICATIONS)) {
                        Log.i("GoogleAuthenticationActivity", "Application: " + application);
                    }
                    Intent intent = new Intent(GoogleAuthenticationActivity.this, DashboardActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(GoogleAuthenticationActivity.this, "Failed retrieving list of applications, please try again later", 2000);
                }
            }
        });
    }

}