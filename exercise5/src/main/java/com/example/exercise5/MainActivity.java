package com.example.exercise5;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView message, message1, message2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        message1 = findViewById(R.id.message1);
        message2 = findViewById(R.id.message2);

        String m = getIntent().getStringExtra("message");
        String m1 = getIntent().getStringExtra("message1");
        String m2 = getIntent().getStringExtra("message2");

        if (m != null && m1 != null && m2 != null) {
            message.setText(m);
            message1.setText(m1);
            message2.setText(m2);

        }

    }
}
