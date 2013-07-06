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

public class GoogleAuthenticationActivity extends Activity {
    DefaultHttpClient mHttpClient = new DefaultHttpClient();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AccountManager accountManager = AccountManager.get(getApplicationContext());

        // Gets the auth token asynchronously, calling the callback with its result.
        //accountManager.getAuthToken(mDisplayedAccount, "ah", false, new GetAuthTokenCallback(), null);
    }

    private class GetAuthTokenCallback implements AccountManagerCallback {
        public void run(AccountManagerFuture result) {
            Bundle bundle;
            try {
                bundle = (Bundle) result.getResult();
                Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
                if(intent != null) {
                    // User input required
                    startActivity(intent);
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
        new LoadAppsTask().execute(authToken);
    }

    private class LoadAppsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String authToken = (String) params[0];

                // Don't follow redirects
                mHttpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

                String url = "https://appengine.google.com/_ah/login?continue=http://localhost/&auth=" + authToken;
                Log.i("LoadAppsTask", "Executing GET request: " + url);
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response;
                response = mHttpClient.execute(httpGet);
                if(response.getStatusLine().getStatusCode() != 302)
                    // Response should be a redirect
                    return false;

                for(Cookie cookie : mHttpClient.getCookieStore().getCookies()) {
                    Log.i("LoadAppsTask", "Cookie name: " + cookie.getName());
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
            new AuthenticatedRequestTask().execute("https://appengine.google.com/");
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