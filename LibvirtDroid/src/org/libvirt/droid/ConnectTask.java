package org.libvirt.droid;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import android.os.AsyncTask;
import android.util.Log;

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
            System.out.println("Connection done"); //$NON-NLS-1$
        } catch (LibvirtException e) {
            Log.e(MainActivity.class.getSimpleName(),
                  mDialog.getString(R.string.connection_error) + e.getError(), e);
        }
        return mConn;
    }

    @Override
    protected void onPostExecute(Connect result) {
        mDialog.onConnectFinished(result);
    }
}
