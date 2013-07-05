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

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import com.friedran.appengine.dashboard.R;
import com.friedran.appengine.dashboard.model.Account;
import com.friedran.appengine.dashboard.model.App;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        List<Account> accounts = getAccounts();
        Account defaultAccount = accounts.get(0);
        App defaultApp = defaultAccount.apps().get(0);

        setActionBarTitle(defaultAccount);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar.setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, getAccountNames()));
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                setActionBarTitle(getAccount(item.getTitle()));
                return super.onOptionsItemSelected(item);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Mark the default account
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    private void setActionBarTitle(Account account) {
        getActionBar().setTitle(account.getName());
        getActionBar().setSubtitle(account.apps().get(0).getName());
    }

    private void selectItem(int position) {
        // update selected item, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setActionBarTitle(getAccounts().get(position));
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private List<Account> getAccounts() {
        return Arrays.asList(
            new Account("account1@google.com", Arrays.asList(new App("App1"), new App("App2"), new App("App3"))),
            new Account("account2@google.com", Arrays.asList(new App("App4"), new App("App5"), new App("App6")))
        );
    }

    private List<String> getAccountNames() {
        return Arrays.asList("account1@google.com", "account2@google.com");
    }

    private Account getAccount(CharSequence accountName) {
        List<Account> accounts = getAccounts();
        for (Account account : accounts) {
            if (account.getName().equals(accountName))
                return account;
        }

        return null;
    }
}
