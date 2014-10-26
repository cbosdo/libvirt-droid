package org.libvirt.droid;

import java.util.ArrayList;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import android.os.AsyncTask;
import android.util.Log;

public class GetDomainTask extends AsyncTask<Connect, Integer, DomainProxy[]> {

    private MainActivity mDialog;

    public GetDomainTask(MainActivity dialog) {
        mDialog = dialog;
    }

    @Override
    protected DomainProxy[] doInBackground(Connect... params) {
        Domain[] domains = null;
        try {
            domains = params[0].listAllDomains(0);
        } catch (LibvirtException e) {
            Log.e(MainActivity.class.getSimpleName(),
                  mDialog.getString(R.string.get_defined_domains_error) + e.getError(), e);
            return new DomainProxy[0];
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
                      mDialog.getString(R.string.domain_details_error) + e.getError(), e);
            }
        }

        return proxies.toArray(new DomainProxy[proxies.size()]);
    }

    @Override
    protected void onPostExecute(DomainProxy[] result) {
        mDialog.onGetDomainsFinished(result);
    }
}
