package com.friedran.appengine.dashboard.client;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AppEngineDashboardClient {
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

    public void executeGetApplications() {
        new AuthenticatedRequestTask("https://appengine.google.com/",
            new AuthenticatedRequestTaskCallback() {
                @Override
                public void run(final HttpEntity result) {
                    new Thread() {
                        @Override
                        public void run() {
                            Log.i("AuthenticatedRequestTask", entityToString(result));
                        }
                    }.start();
                }
        }).execute();
    }

    private interface AuthenticatedRequestTaskCallback {
        public void run(HttpEntity result);
    }

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

    private static String entityToString(HttpEntity entity) {
        StringBuilder str = new StringBuilder();
        try {
            InputStream is = entity.getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                str.append(line + "\n");
            }

            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return str.toString();
    }
}
