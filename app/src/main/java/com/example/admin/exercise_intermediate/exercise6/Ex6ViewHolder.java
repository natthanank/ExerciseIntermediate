package com.example.admin.exercise_intermediate.exercise6;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.example.admin.exercise_intermediate.R;

public class Ex6ViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    Switch aSwitch;

    public Ex6ViewHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        aSwitch = itemView.findViewById(R.id.aSwitch);
    }
}
