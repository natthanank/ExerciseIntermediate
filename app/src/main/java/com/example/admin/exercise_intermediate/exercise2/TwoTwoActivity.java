package com.example.admin.exercise_intermediate.exercise2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.admin.exercise_intermediate.R;

import java.util.ArrayList;

public class TwoTwoActivity extends AppCompatActivity {

    SearchListView searchListView;
    ArrayList<String> sports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_two);

        searchListView = findViewById(R.id.search_list);
        sports = new ArrayList<>();
        sports.add("Badminton");
        sports.add("Football");
        sports.add("Basketball");
        sports.add("Golf");
        searchListView.setSports(sports);
    }
}
