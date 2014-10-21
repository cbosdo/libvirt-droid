package org.libvirt.droid;

import org.libvirt.Connect;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
    private Connect mConn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        System.setProperty("jna.debug_load.jna", "true");
        System.setProperty("jna.debug_load", "true");


        Button connectBtn = (Button)findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Try to connect to libvirt
                String uri = ((EditText)findViewById(R.id.uriTxt)).getText().toString();
                new ConnectTask(MainActivity.this).execute(uri);
            }
        });
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
}
