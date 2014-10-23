package org.libvirt.droid;

import org.libvirt.Connect;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {
    private Connect mConn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        System.setProperty("jna.debug_load.jna", "true");
        System.setProperty("jna.debug_load", "true");

        // Try to connect to libvirt
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        String uri = prefs.getString("cnx_uri", new String());
        connect(uri);
    }

    public void connect(String uri) {
        if (!uri.isEmpty()) {
            new ConnectTask(MainActivity.this).execute(uri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void onConnectFinished(Connect conn) {

        mConn = conn;
        TextView resultText = (TextView)findViewById(R.id.text);

        if (conn != null) {
            resultText.setText("Connected!");
        } else {
            resultText.setText("Failed!");
        }
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals("cnx_uri")) {
            String uri = sharedPreferences.getString(key, new String());
            connect(uri);
        }
    }
}
