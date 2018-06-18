package com.example.admin.exercise_intermediate.exercise2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.exercise_intermediate.R;

import java.util.ArrayList;

public class Ex2Adapter extends RecyclerView.Adapter {

    private ArrayList<String> sports;

    public Ex2Adapter(ArrayList<String> sports) {
        this.sports = sports;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ex2item, viewGroup, false);
        return new Ex2ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((Ex2ViewHolder) viewHolder).sportText.setText(sports.get(i));
    }

    @Override
    public int getItemCount() {
        return sports.size();
    }
}
