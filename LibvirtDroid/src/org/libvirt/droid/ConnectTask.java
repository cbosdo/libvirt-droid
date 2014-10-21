package org.libvirt.droid;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<String, Integer, Connect> {

    private MainActivity mDialog;
    private ConnectAuth mConnAuth;


    public ConnectTask(MainActivity dialog) {
        mDialog = dialog;
    }

    @Override
    protected Connect doInBackground(String... params) {
        Connect conn = null;

        try {
            mConnAuth = new ConnectAuth(mDialog);
            conn = new Connect(params[0], mConnAuth, 0);
            System.out.println("Connection done");
        } catch (LibvirtException e) {
            System.out.println("exception caught: " + e);
            System.out.println(e.getError());
        }
        return conn;
    }

    @Override
    protected void onPostExecute(Connect result) {
        mDialog.onConnectFinished(result);
    }
}
