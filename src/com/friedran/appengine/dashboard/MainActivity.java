package com.friedran.appengine.dashboard;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("AppName");
        actionBar.setSubtitle("account@google.com");

        String[] appsList = {"AppName1", "AppName2", "AppName2"};
        ListView mNavDrawer = (ListView) findViewById(R.id.navigation_drawer);
        mNavDrawer.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, appsList));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
}
