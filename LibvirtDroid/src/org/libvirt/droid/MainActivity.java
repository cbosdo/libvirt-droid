package org.libvirt.droid;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ListActivity implements OnSharedPreferenceChangeListener {

    private static String PREF_CNX_URI = "cnx_uri"; //$NON-NLS-1$

    private Connect mConn;

    @SuppressWarnings("nls")
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
        String uri = prefs.getString(PREF_CNX_URI, new String());
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

        try {
            Domain[] domains = mConn.listAllDomains(0);
            System.out.println("Got domains: " + domains.length); //$NON-NLS-1$

            // TODO This requires to be moved to an AsyncTask
            DomainsAdapter adapter = new DomainsAdapter(this, domains);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (LibvirtException e) {
            Log.e(MainActivity.class.getSimpleName(),
                  getString(R.string.get_defined_domains_error), e);
        }
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(PREF_CNX_URI)) {
            String uri = sharedPreferences.getString(key, new String());
            connect(uri);
        }
    }
}
