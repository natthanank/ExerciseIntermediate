package com.example.admin.exercise_intermediate.exercise7;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.admin.exercise_intermediate.R;

public class Ex7ViewHolder extends RecyclerView.ViewHolder {

    TextView name, id;

    public Ex7ViewHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        id = itemView.findViewById(R.id.id);
    }
}
