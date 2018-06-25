package com.example.exercise2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    SearchListView searchListView;
    ArrayList<String> sports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        searchListView = findViewById(R.id.search_list);
        sports = new ArrayList<>();
        sports.add("Badminton");
        sports.add("Football");
        sports.add("Basketball");
        sports.add("Golf");
        searchListView.setSports(sports);
    }
}
