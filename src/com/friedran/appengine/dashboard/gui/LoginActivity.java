package com.friedran.appengine.dashboard.gui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.friedran.appengine.dashboard.R;
import com.friedran.appengine.dashboard.client.AppEngineDashboardAPI;
import com.friedran.appengine.dashboard.client.AppEngineDashboardAuthenticator;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity implements View.OnClickListener {
    public static final String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
    protected Spinner mAccountSpinner;
    protected Button mLoginButton;
    protected Map<String, Account> mAccounts;
    protected Account mSelectedAccount;

    ProgressDialog mProgressDialog;
    AppEngineDashboardClient mAppEngineClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAccounts = new HashMap<String, Account>();
        for (Account account : AccountManager.get(this).getAccounts()) {
            mAccounts.put(account.name, account);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, new ArrayList<String>(mAccounts.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAccountSpinner = (Spinner) findViewById(R.id.login_account_spinner);
        mAccountSpinner.setAdapter(adapter);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /** Happens when the login button is clicked */
    @Override
    public void onClick(View v) {
        mProgressDialog.setMessage("Authenticating with your Google AppEngine account...");
        mProgressDialog.show();
        mLoginButton.setClickable(false);

        String accountName = (String) mAccountSpinner.getSelectedItem();
        mSelectedAccount = mAccounts.get(accountName);
        mAppEngineClient = new AppEngineDashboardClient(mSelectedAccount, this,
                // Called when the user approval is required to authorize us
                new AppEngineDashboardAuthenticator.OnUserInputRequiredCallback() {
                    @Override
                    public void onUserInputRequired(Intent accountManagerIntent) {
                        startActivity(accountManagerIntent);
                    }
                },
                // Called when the authentication is completed
                new AppEngineDashboardClient.PostExecuteCallback() {
                    @Override
                    public void run(Bundle resultBundle) {
                        boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
                        Log.i("GoogleAuthenticationActivity", "Authentication done, result = " + result);

                        if (result) {
                            onSuccessfulAuthentication();
                        } else {
                            safelyDismissProgress();
                            Toast.makeText(LoginActivity.this, "Authentication failed, please try again later", 2000);
                        }
                    }
                });

        AppEngineDashboardAPI appEngineAPI = AppEngineDashboardAPI.getInstance();
        appEngineAPI.setClient(mSelectedAccount, mAppEngineClient);

        mAppEngineClient.executeAuthentication();
    }

    private void onSuccessfulAuthentication() {
        mProgressDialog.setMessage("Retrieving list of AppEngine applications...");

        mAppEngineClient.executeGetApplications(new AppEngineDashboardClient.PostExecuteCallback() {
            @Override
            public void run(Bundle resultBundle) {
                safelyDismissProgress();

                boolean result = resultBundle.getBoolean(AppEngineDashboardClient.KEY_RESULT);
                Log.i("GoogleAuthenticationActivity", "GetApplications done, result = " + result);

                if (result) {
                    for (String application : resultBundle.getStringArrayList(AppEngineDashboardClient.KEY_APPLICATIONS)) {
                        Log.i("GoogleAuthenticationActivity", "Application: " + application);
                    }
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(LoginActivity.EXTRA_ACCOUNT, mSelectedAccount);
                    startActivity(intent);

                } else {
                    Toast.makeText(LoginActivity.this, "Failed retrieving list of applications, please try again later", 2000);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        safelyDismissProgress();
    }

    @Override
    protected void onStop() {
        super.onStop();
        safelyDismissProgress();
    }

    private void safelyDismissProgress() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        mLoginButton.setClickable(true);
    }
}