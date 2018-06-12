package com.example.admin.exercise_intermediate.exercise2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.admin.exercise_intermediate.R;

public class SecondActivity extends AppCompatActivity {


    IdCardEditText idCardEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        idCardEditText = findViewById(R.id.id_card);


    }
}
