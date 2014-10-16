package org.libvirt.droid;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
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
                Connect conn=null;
                try{
                    String uri = ((EditText)findViewById(R.id.uriTxt)).getText().toString();
                    conn = new Connect(uri, true);
                    ((TextView)findViewById(R.id.text)).setText("Connected!");

                } catch (LibvirtException e) {
                    System.out.println("exception caught:"+e);
                    System.out.println(e.getError());
                    ((TextView)findViewById(R.id.text)).setText("Failed!");
                }
            }
        });
    }
}
