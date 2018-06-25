package com.example.exercise4_get_shared_file;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetIntentActivity extends AppCompatActivity {

    TextView locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_intent);

        locationText = findViewById(R.id.locationText);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_SEND) && type != null) {


            if (type.equals("text/plain")) {
                String text = intent.getStringExtra(Intent.EXTRA_STREAM);
                Toast.makeText(this, "action = " + action + " \ntype = " + type + " \ntext = " + text, Toast.LENGTH_LONG).show();


                String message = "";
                Uri androidURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                File f = new File(androidURI.getPath());

                // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
                try {
                    FileInputStream fis = new FileInputStream(f);
                    DataInputStream in = new DataInputStream(fis);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    // 2nd arg is buffer size

                    // More efficient (less readable) implementation of above is the composite expression
            /*BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getResources().openRawResource(R.raw.textfile)), 8192);*/

                    String test;
                    while (true){
                        test = br.readLine();
                        // readLine() returns null if no more lines in the file
                        if(test == null) break;
                        message = message + test + "\n";

                    }
                    in.close();
                    br.close();

                    locationText.setText(message);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
