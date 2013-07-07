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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AppEngineDashboardClient {
    public static final String KEY_APPLICATIONS = "APPLICATIONS";
    protected Account mAccount;
    protected DefaultHttpClient mHttpClient;
    protected Context mApplicationContext;
    protected AppEngineDashboardAuthenticator mAppEngineDashboardAuthenticator;

    protected PostExecuteCallback mPostAuthenticateCallback;

    public static final String KEY_RESULT = "RESULT";

    public interface PostExecuteCallback {
        public void run(Bundle result);
    }

    public AppEngineDashboardClient(Account account, Context context,
                                    PostExecuteCallback postAuthenticationCallback) {
        mAccount = account;
        mApplicationContext = context.getApplicationContext();
        mPostAuthenticateCallback = postAuthenticationCallback;

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
        }).execute();
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
                    ArrayList<String> applicationIds = AppEngineParserUtils.getApplicationIDs(is);
                    is.close();

                    Bundle result = new Bundle();
                    result.putBoolean(KEY_RESULT, true);
                    result.putStringArrayList(KEY_APPLICATIONS, applicationIds);
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
                HttpGet http_get = new HttpGet((String) params[0]);
                return mHttpClient.execute(http_get);
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
