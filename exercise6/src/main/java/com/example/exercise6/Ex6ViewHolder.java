package com.example.exercise6;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class Ex6ViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    Switch aSwitch;

    public Ex6ViewHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.name);
        aSwitch = itemView.findViewById(R.id.aSwitch);
    }
}
