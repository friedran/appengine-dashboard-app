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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AppEngineDashboardClient {
    public static final String KEY_APPLICATIONS = "APPLICATIONS";
    public static final String KEY_CHART_URL = "CHART_URL";
    protected Account mAccount;
    protected DefaultHttpClient mHttpClient;
    protected Context mApplicationContext;
    protected AppEngineDashboardAuthenticator mAppEngineDashboardAuthenticator;

    protected PostExecuteCallback mPostAuthenticateCallback;

    protected ArrayList<String> mLastRetrievedApplications;

    public static final String KEY_RESULT = "RESULT";

    public interface PostExecuteCallback {
        public void run(Bundle result);
    }

    public AppEngineDashboardClient(Account account, Context context,
                                    PostExecuteCallback postAuthenticationCallback) {
        mAccount = account;
        mApplicationContext = context.getApplicationContext();
        mPostAuthenticateCallback = postAuthenticationCallback;

        mLastRetrievedApplications = new ArrayList<String>();
        mHttpClient = new DefaultHttpClient();

        mAppEngineDashboardAuthenticator = new AppEngineDashboardAuthenticator(
                mAccount, mHttpClient, mApplicationContext,
            new AppEngineDashboardAuthenticator.PostAuthenticateCallback() {
            @Override
            public void run(boolean result) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(KEY_RESULT, result);
                mPostAuthenticateCallback.run(bundle);
            }
        });
    }

    public void executeAuthentication() {
        mAppEngineDashboardAuthenticator.executeAuthentication();
    }

    /**
     * Send an authenticated GetApplications request asynchronously and return its results to the given callback.
     */
    public void executeGetApplications(final PostExecuteCallback postGetApplicationsCallback) {
        new AuthenticatedRequestTask("https://appengine.google.com/",
            new AuthenticatedRequestTaskCallback() {
                @Override
                public void run(final HttpEntity httpEntityResult) {
                    onPostExecuteGetApplications(httpEntityResult, postGetApplicationsCallback);
                }
        }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public List<String> getLastRetrievedApplications() {
        return mLastRetrievedApplications;
    }

    /**
     * Called when the GetApplication request is done, parses the result and returns the list of applications to the given callback.
     *
     * @param httpEntityResult Result of the GetApplication AuthenticatedRequest
     * @param postGetApplicationsCallback The callback that should be called with the results of GetApplications
     */
    private void onPostExecuteGetApplications(final HttpEntity httpEntityResult, final PostExecuteCallback postGetApplicationsCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // Read the content and parse it. That might take some time...
                    InputStream is = httpEntityResult.getContent();
                    mLastRetrievedApplications = AppEngineParserUtils.getApplicationIDs(is);
                    is.close();

                    Bundle result = new Bundle();
                    result.putBoolean(KEY_RESULT, true);
                    result.putStringArrayList(KEY_APPLICATIONS, mLastRetrievedApplications);
                    postGetApplicationsCallback.run(result);

                } catch (IOException e) {
                    Log.e("AppEngineDashboardClient#onPostExecuteGetApplications", "Exception caught when tried to parse result", e);
                    e.printStackTrace();
                    Bundle result = new Bundle();
                    result.putBoolean(KEY_RESULT, false);
                    postGetApplicationsCallback.run(result);
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Send an authenticated GetChart request asynchronously and return its results to the given callback.
     */
    public void executeGetChartUrl(
            String appID, int chartTypeID, int chartWindowID, final PostExecuteCallback postGetChartUrlCallback) {

        String url = String.format(
                "https://appengine.google.com/dashboard/stats?app_id=s~%s&type=%d&window=%d",
                appID, chartTypeID, chartWindowID);

        new AuthenticatedRequestTask(url,
                new AuthenticatedRequestTaskCallback() {
                    @Override
                    public void run(final HttpEntity httpEntityResult) {
                        onPostExecuteGetChartURL(httpEntityResult, postGetChartUrlCallback);
                    }
                }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    /**
     * Called when the GetChartURL request is done, parses the result and returns the chart url to the given callback.
     *
     * @param httpEntityResult Result of the GetChartURL AuthenticatedRequest
     * @param postGetChartUrlCallback The callback that should be called with the results of GetChartURL
     */
    private void onPostExecuteGetChartURL(final HttpEntity httpEntityResult, final PostExecuteCallback postGetChartUrlCallback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bundle result = new Bundle();

                try {
                    // Parse the result as a JSON object
                    String content = EntityUtils.toString(httpEntityResult);
                    JSONObject jsonData = new JSONObject(content);
                    String chart_url = jsonData.getString("chart_url");

                    result.putBoolean(KEY_RESULT, true);
                    result.putString(KEY_CHART_URL, chart_url);

                } catch (Exception e) {
                    Log.e("AppEngineDashboardClient#onPostExecuteGetChartURL", "Exception caught when tried to parse result", e);
                    e.printStackTrace();
                    result.putBoolean(KEY_RESULT, false);

                } finally {
                    postGetChartUrlCallback.run(result);
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Inner class responsible of sending authenticated requests to Google's AppEngine servers and returning its
     *  responses via asynchronous callbacks.
     */
    private class AuthenticatedRequestTask extends AsyncTask<String, Void, HttpResponse> {
        protected String mURL;
        protected AuthenticatedRequestTaskCallback mCallback;

        public AuthenticatedRequestTask(String url, AuthenticatedRequestTaskCallback callback) {
            mURL = url;
            mCallback = callback;
        }

        @Override
        protected HttpResponse doInBackground(String... params) {
            try {
                Log.i("AppEngineDashboardClient", "Executing authenticated request: " + mURL);
                HttpGet httpGet = new HttpGet(mURL);
                return mHttpClient.execute(httpGet);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final HttpResponse result) {
            mCallback.run(result.getEntity());
        }
    }

    private interface AuthenticatedRequestTaskCallback {
        public void run(HttpEntity result);
    }
}
