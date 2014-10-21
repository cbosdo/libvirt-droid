package org.libvirt.droid;

import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
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
                if (mCred.prompt.contains("(y/n)")) {
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                            int which) {
                            mResponse = "y";
                            mDialogSemaphore.release();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                            int which) {
                            mResponse = "n";
                            mDialogSemaphore.release();
                        }
                    });
                }
                break;
            case VIR_CRED_PASSPHRASE:
            case VIR_CRED_NOECHOPROMPT:
                System.out.println("Passphrase type");
                final EditText passTxt = new EditText(mActivity);
                passTxt.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(passTxt);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                                        int which) {
                        mResponse = passTxt.getText().toString();
                        mDialogSemaphore.release();
                    }
                });
                break;
            default:
                System.out.println("Unhandled cred: type=" + mCred.type + " prompt='" + mCred.prompt + "'");
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
