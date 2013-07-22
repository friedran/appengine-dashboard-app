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
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.friedran.appengine.dashboard.utils.AppEngineParserUtils;
import com.friedran.appengine.dashboard.utils.LogUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppEngineDashboardClient {
    public static final String KEY_APPLICATIONS = "APPLICATIONS";
    public static final String KEY_CHART_URL = "CHART_URL";
    protected Account mAccount;
    protected DefaultHttpClient mHttpClient;
    protected Context mApplicationContext;
    protected AppEngineDashboardAuthenticator mAuthenticator;

    protected PostExecuteCallback mPostAuthenticateCallback;

    protected ArrayList<String> mLastRetrievedApplications;

    public static final String KEY_RESULT = "RESULT";

    public interface PostExecuteCallback {
        public void onPostExecute(Bundle result);
    }

    public AppEngineDashboardClient(Account account, Context context,
                                    AppEngineDashboardAuthenticator.OnUserInputRequiredCallback onUserInputRequiredCallback,
                                    PostExecuteCallback postAuthenticationCallback) {
        mAccount = account;
        mApplicationContext = context.getApplicationContext();
        mPostAuthenticateCallback = postAuthenticationCallback;

        mLastRetrievedApplications = new ArrayList<String>();
        mHttpClient = new DefaultHttpClient();

        mAuthenticator = new AppEngineDashboardAuthenticator(
                mAccount, mHttpClient, mApplicationContext,
                onUserInputRequiredCallback,
                new AppEngineDashboardAuthenticator.PostAuthenticateCallback() {
                    @Override
                    public void run(boolean result) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(KEY_RESULT, result);
                        mPostAuthenticateCallback.onPostExecute(bundle);
                    }
                });
    }

    public Account getAccount() {
        return mAccount;
    }

    public void executeAuthentication() {
        mAuthenticator.executeAuthentication();
    }

    public void invalidateAuthenticationToken() {
        mAuthenticator.invalidateAuthToken();
    }

    /**
     * Send an authenticated GetApplications request asynchronously and return its results to the given callback.
     */
    public void executeGetApplications(final PostExecuteCallback postGetApplicationsCallback) {
        new AuthenticatedRequestTask("https://appengine.google.com/",
            new AuthenticatedRequestTaskBackgroundCallback() {
                @Override
                public Bundle run(final HttpEntity httpResponse) {
                    Bundle result = new Bundle();
                    try {

                        mLastRetrievedApplications = AppEngineParserUtils.getApplicationIDs(httpResponse.getContent());
                        result.putStringArrayList(KEY_APPLICATIONS, mLastRetrievedApplications);
                        result.putBoolean(KEY_RESULT, true);

                    } catch (IOException e) {
                        LogUtils.e("AppEngineDashboardClient", "Failed parsing the GetApplications response", e);
                        result.putBoolean(KEY_RESULT, false);
                    }
                    return result;
                }
            }, postGetApplicationsCallback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public List<String> getLastRetrievedApplications() {
        return mLastRetrievedApplications;
    }

    /**
     * Send an authenticated GetChart request asynchronously and return its results to the given callback.
     */
    public void executeGetChartUrl(
            String appID, int chartTypeID, int chartWindowID, final PostExecuteCallback postGetChartUrlCallback) {

        String url = String.format(
                "https://appengine.google.com/dashboard/stats?app_id=s~%s&type=%d&window=%d",
                appID, chartTypeID, chartWindowID);

        new AuthenticatedRequestTask(url, new AuthenticatedRequestTaskBackgroundCallback() {
                @Override
                public Bundle run(final HttpEntity httpResponseEntity) {
                    Bundle result = new Bundle();

                    try {
                        JSONObject jsonData = new JSONObject(EntityUtils.toString(httpResponseEntity));
                        String chart_url = jsonData.getString("chart_url");

                        result.putBoolean(KEY_RESULT, true);
                        result.putString(KEY_CHART_URL, chart_url);

                    } catch (Exception e) {
                        LogUtils.e("AppEngineDashboardClient#onPostExecuteGetChartURL", "Exception caught when tried to parse result", e);
                        e.printStackTrace();
                        result.putBoolean(KEY_RESULT, false);
                    }

                    return result;
                }
            }, postGetChartUrlCallback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    /**
     * Inner class responsible of sending authenticated requests to Google's AppEngine servers and returning its
     *  response content via asynchronous callbacks.
     */
    private class AuthenticatedRequestTask extends AsyncTask<String, Void, Bundle> {
        protected String mURL;
        protected AuthenticatedRequestTaskBackgroundCallback mBackgroundCallback;
        protected AppEngineDashboardClient.PostExecuteCallback mPostExecuteCallback;

        public AuthenticatedRequestTask(String url,
                                        AuthenticatedRequestTaskBackgroundCallback backgroundCallback,
                                        AppEngineDashboardClient.PostExecuteCallback postExecuteCallback) {
            mURL = url;
            mBackgroundCallback = backgroundCallback;
            mPostExecuteCallback = postExecuteCallback;
        }

        @Override
        protected Bundle doInBackground(String... params) {
            Bundle result = new Bundle();

            try {
                LogUtils.i("AppEngineDashboardClient", "Executing authenticated request: " + mURL);
                HttpGet httpGet = new HttpGet(mURL);
                HttpResponse response = mHttpClient.execute(httpGet);

                HttpEntity responseEntity = response.getEntity();
                result = mBackgroundCallback.run(responseEntity);

                // Finalizes the connection
                responseEntity.consumeContent();

            } catch (Exception e) {
                LogUtils.e("AuthenticatedRequestTask", "Exception raised while handling request for " + mURL, e);
                result.putBoolean(KEY_RESULT, false);
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Bundle result) {
            mPostExecuteCallback.onPostExecute(result);
        }
    }

    private interface AuthenticatedRequestTaskBackgroundCallback {
        public Bundle run(HttpEntity responseEntity);
    }
}
