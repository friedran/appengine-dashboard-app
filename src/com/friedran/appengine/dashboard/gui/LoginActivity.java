package com.friedran.appengine.dashboard.gui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.friedran.appengine.dashboard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity implements View.OnClickListener {
    public static final String EXTRA_ACCOUNT = "EXTRA_ACCOUNT";
    protected Spinner mAccountSpinner;
    protected Map<String, Account> mAccounts;

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

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /** Happens when the login button is clicked */
    @Override
    public void onClick(View v) {
        String accountName = (String) mAccountSpinner.getSelectedItem();

        Intent intent = new Intent(LoginActivity.this, GoogleAuthenticationActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(EXTRA_ACCOUNT, mAccounts.get(accountName));

        startActivity(intent);
    }
}