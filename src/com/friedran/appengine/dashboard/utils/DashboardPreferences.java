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
            LogUtils.i("LoginActivity", "No saved account found");
            return null;
        }

        try {
            Account savedAccount = (new Gson()).fromJson(accountJson, Account.class);
            LogUtils.i("LoginActivity", "Got a saved account: " + savedAccount.name);
            return savedAccount;

        } catch (JsonSyntaxException e) {
            LogUtils.e("LoginActivity", "Saved account is corrupted, resetting the repository");
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
