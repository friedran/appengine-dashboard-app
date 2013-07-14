package com.friedran.appengine.dashboard.utils;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Responsible for saving and managing the app-specific preferences
 */
public class DashboardPreferences {

    private static final String KEY_LOGIN_ACCOUNT = "KEY_LOGIN_ACCOUNT";

    private SharedPreferences mPreferences;


    public DashboardPreferences(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Account getSavedAccount() {
        String accountJson = mPreferences.getString(KEY_LOGIN_ACCOUNT, null);
        if (accountJson == null) {
            Log.i("LoginActivity", "No saved account found");
            return null;
        }

        try {
            Account savedAccount = (new Gson()).fromJson(accountJson, Account.class);
            Log.i("LoginActivity", "Got a saved account: " + savedAccount.name);
            return savedAccount;

        } catch (JsonSyntaxException e) {
            Log.e("LoginActivity", "Saved account is corrupted, resetting the repository");
            resetSavedAccount();
            return null;
        }
    }

    public void saveAccount(Account account) {
        Gson gson = new Gson();
        String accountJson = gson.toJson(account);
        mPreferences.edit().putString(KEY_LOGIN_ACCOUNT, accountJson).commit();
    }

    public void resetSavedAccount() {
        mPreferences.edit().remove(KEY_LOGIN_ACCOUNT).commit();
    }

}
