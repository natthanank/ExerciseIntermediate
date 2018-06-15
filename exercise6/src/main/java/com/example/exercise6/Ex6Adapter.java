package com.example.exercise6;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.esri.arcgisruntime.mapping.ArcGISMap;

import java.util.ArrayList;

public class Ex6Adapter extends RecyclerView.Adapter {

    private ArrayList<LayerForShow> layerForShows;
    private ArcGISMap map;

    public Ex6Adapter(ArrayList<LayerForShow> layerForShows, ArcGISMap map) {
        this.layerForShows = layerForShows;
        this.map = map;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (getItemViewType(i) == 0) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ex6head, viewGroup, false);
            return new Ex6HeadViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ex6item, viewGroup, false);
            return new Ex6ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        final LayerForShow layerForShow = layerForShows.get(i);
        if (getItemViewType(i) != 0) {
            ((Ex6ViewHolder) viewHolder).name.setText(layerForShow.getName());
            ((Ex6ViewHolder) viewHolder).aSwitch.setChecked(layerForShow.isShow());
            ((Ex6ViewHolder) viewHolder).aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    layerForShow.setShow(b);
                    map.getOperationalLayers().get(i - 1).setVisible(b);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return layerForShows.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
