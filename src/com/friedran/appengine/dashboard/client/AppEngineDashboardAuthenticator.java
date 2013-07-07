package com.friedran.appengine.dashboard.client;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AppEngineDashboardAuthenticator {
    protected Account mAccount;
    protected DefaultHttpClient mHttpClient;
    protected Context mApplicationContext;
    protected PostAuthenticateCallback mPostAuthenticateCallback;

    public interface PostAuthenticateCallback {
        public void run(boolean result);
    }

    public AppEngineDashboardAuthenticator(Account account, DefaultHttpClient httpClient, Context applicationContext,
                                           PostAuthenticateCallback callback) {
        mAccount = account;
        mHttpClient = httpClient;
        mApplicationContext = applicationContext;
        mPostAuthenticateCallback = callback;
    }

    public void startAuthentication() {
        // Gets the auth token asynchronously, calling the callback with its result.
        AccountManager.get(mApplicationContext).getAuthToken(mAccount, "ah", false, new GetAuthTokenCallback(), null);
    }

    private class GetAuthTokenCallback implements AccountManagerCallback {
        public void run(AccountManagerFuture result) {
            Bundle bundle;
            try {
                bundle = (Bundle) result.getResult();
                Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
                if(intent != null) {
                    // User input required
                    mApplicationContext.startActivity(intent);
                } else {
                    onGetAuthToken(bundle);
                }
            } catch (OperationCanceledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    protected void onGetAuthToken(Bundle bundle) {
        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        Log.i("MainActivity", "onGetAuthToken: Got the auth token " + authToken + ", starting to load apps");
        new LoginToAppEngineTask().execute(authToken);
    }

    private class LoginToAppEngineTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String authToken = (String) params[0];

                // Don't follow redirects
                mHttpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

                String url = "https://appengine.google.com/_ah/login?continue=http://localhost/&auth=" + authToken;
                Log.i("LoginToAppEngineTask", "Executing GET request: " + url);
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response;
                response = mHttpClient.execute(httpGet);
                if(response.getStatusLine().getStatusCode() != 302)
                    // Response should be a redirect
                    return false;

                for(Cookie cookie : mHttpClient.getCookieStore().getCookies()) {
                    Log.i("LoginToAppEngineTask", "Cookie name: " + cookie.getName());
                    if(cookie.getName().equals("SACSID"))
                        return true;
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                mHttpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mPostAuthenticateCallback.run(result);
            //new AuthenticatedRequestTask().execute("https://appengine.google.com/");
        }
    }

    private class AuthenticatedRequestTask extends AsyncTask<String, Void, HttpResponse> {
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
            new Thread() {
                @Override
                public void run() {
                    Log.i("AuthenticatedRequestTask", entityToString(result.getEntity()));
                }
            }.start();
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
