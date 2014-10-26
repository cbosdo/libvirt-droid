package org.libvirt.droid;

import java.util.ArrayList;

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

        // TODO This requires to be moved to an AsyncTask
        Domain[] domains = null;
        try {
            domains = mConn.listAllDomains(0);
        } catch (LibvirtException e) {
            Log.e(MainActivity.class.getSimpleName(),
                  getString(R.string.get_defined_domains_error) + e.getError(), e);
            return;
        }

        int domCount = domains.length;
        System.out.println("Got domains: " + domCount); //$NON-NLS-1$
        ArrayList<DomainProxy> proxies = new ArrayList<DomainProxy>(domCount);
        for (Domain dom: domains) {
            try {
                DomainProxy proxy = new DomainProxy(dom);
                proxies.add(proxy);
            } catch (LibvirtException e) {
                Log.w(MainActivity.class.getSimpleName(),
                      getString(R.string.domain_details_error) + e.getError(), e);
            }
        }

        DomainsAdapter adapter = new DomainsAdapter(this,
                proxies.toArray(new DomainProxy[proxies.size()]));
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(PREF_CNX_URI)) {
            String uri = sharedPreferences.getString(key, new String());
            connect(uri);
        }
    }
}
