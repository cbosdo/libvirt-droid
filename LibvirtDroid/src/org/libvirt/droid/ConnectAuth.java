package org.libvirt.droid;

import java.util.Locale;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

public class ConnectAuth extends org.libvirt.ConnectAuth {

    private Activity mActivity;
    private String mResponse;
    private final Semaphore mDialogSemaphore = new Semaphore(0, true);

    private class DialogTask implements Runnable {

        private final Credential mCred;

        public DialogTask(Credential c) {
            mCred = c;
        }

        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(mCred.prompt);
            builder.setCancelable(false);
            mResponse = mCred.defresult;

            switch (mCred.type) {
            case VIR_CRED_ECHOPROMPT:
                String ynChoice = "(y/n)"; //$NON-NLS-1$
                if (mCred.prompt.contains(ynChoice)) {
                    String prompt = mCred.prompt.replace(ynChoice, new String());
                    builder.setMessage(prompt);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                            int which) {
                            mResponse = "y"; //$NON-NLS-1$
                            mDialogSemaphore.release();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                            int which) {
                            mResponse = "n"; //$NON-NLS-1$
                            mDialogSemaphore.release();
                        }
                    });
                }
                break;
            case VIR_CRED_PASSPHRASE:
            case VIR_CRED_NOECHOPROMPT:
                final EditText passTxt = new EditText(mActivity);
                passTxt.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(passTxt);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int which) {
                        mResponse = passTxt.getText().toString();
                        mDialogSemaphore.release();
                    }
                });
                break;
            default:
                String msg = String.format(Locale.US, "Unhandled cred: type=%d prompt='%s'", //$NON-NLS-1$
                                           mCred.type, mCred.prompt);
                Log.i(MainActivity.class.getName(), msg);
                break;
            }
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    public ConnectAuth(Activity activity) {
        mActivity = activity;

        credType = new CredentialType[] {
                CredentialType.VIR_CRED_AUTHNAME,
                CredentialType.VIR_CRED_ECHOPROMPT,
                CredentialType.VIR_CRED_REALM,
                CredentialType.VIR_CRED_PASSPHRASE,
                CredentialType.VIR_CRED_NOECHOPROMPT
        };
    }

    @Override
    public int callback(Credential[] cred) {
        try {
            for (Credential c : cred) {
                mActivity.runOnUiThread(new DialogTask(c));
                try
                {
                    mDialogSemaphore.acquire();
                }
                catch (InterruptedException e)
                {
                }
                c.result = mResponse;
            }
        } catch (Exception e) {
            return -1;
        }

        return 0;
    }
}
