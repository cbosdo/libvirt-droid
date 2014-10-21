package org.libvirt.droid;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<String, Integer, Connect> {

    private MainActivity mDialog;


    public ConnectTask(MainActivity dialog) {
        mDialog = dialog;
    }

    @Override
    protected Connect doInBackground(String... params) {
        Connect conn = null;

        try {
            ConnectAuth connAuth = new ConnectAuth(mDialog);
            conn = new Connect(params[0], connAuth, 0);
        } catch (LibvirtException e) {
            System.out.println("exception caught: " + e);
            System.out.println(e.getError());
        }
        return conn;
    }
}
