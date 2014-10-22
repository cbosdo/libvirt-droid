package org.libvirt.droid;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<String, Integer, Connect> {

    private MainActivity mDialog;
    private ConnectAuth mConnAuth;
    private Connect mConn;


    public ConnectTask(MainActivity dialog) {
        mDialog = dialog;
    }

    @Override
    protected Connect doInBackground(String... params) {
        mConn = null;

        try {
            mConnAuth = new ConnectAuth(mDialog);
            mConn = new Connect(params[0], mConnAuth, 0);
            System.out.println("Connection done");
        } catch (LibvirtException e) {
            System.out.println("exception caught: " + e);
            System.out.println(e.getError());
        }
        return mConn;
    }

    @Override
    protected void onPostExecute(Connect result) {
        mDialog.onConnectFinished(result);
    }
}
