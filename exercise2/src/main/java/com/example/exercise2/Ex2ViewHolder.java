package com.example.exercise2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class Ex2ViewHolder extends RecyclerView.ViewHolder {

    TextView sportText;

    public Ex2ViewHolder(@NonNull View itemView) {
        super(itemView);

        sportText = itemView.findViewById(R.id.sportText);
    }
}
