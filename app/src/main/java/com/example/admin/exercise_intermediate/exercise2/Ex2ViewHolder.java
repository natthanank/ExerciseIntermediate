package com.example.admin.exercise_intermediate.exercise2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.admin.exercise_intermediate.R;

public class Ex2ViewHolder extends RecyclerView.ViewHolder {

    TextView sportText;

    public Ex2ViewHolder(@NonNull View itemView) {
        super(itemView);

        sportText = itemView.findViewById(R.id.sportText);
    }
}
