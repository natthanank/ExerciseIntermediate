package com.example.admin.exercise_intermediate.exercise7;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.example.admin.exercise_intermediate.R;

import java.util.ArrayList;

public class Ex7Adapter extends RecyclerView.Adapter {

    ArrayList<Feature> features;
    AppCompatActivity activity;
    ArcGISMap map;
    GraphicsOverlay graphicsOverlay;

    public Ex7Adapter(ArrayList<Feature> features, AppCompatActivity activity, ArcGISMap map) {
        this.features = features;
        this.activity = activity;
        this.map = map;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ex7item, viewGroup, false);
        return new Ex7ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        ((Ex7ViewHolder) viewHolder).name.setText(features.get(i).getAttributes().get("OWNER_NAME").toString());
//        ((Ex7ViewHolder) viewHolder).id.setText(features.get(i).getAttributes().get("OWN_ID ").toString());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SevenActivity) activity).mMapView.getGraphicsOverlays().clear();
                ((SevenActivity) activity).selectedPosition = i;
                if (((SevenActivity) activity).sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    ((SevenActivity) activity).sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                double xmax = features.get(i).getGeometry().getExtent().getXMax();
                double xmin = features.get(i).getGeometry().getExtent().getXMin();
                double ymax = features.get(i).getGeometry().getExtent().getYMax();
                double ymin = features.get(i).getGeometry().getExtent().getYMin();

                PolygonBuilder coloradoCorners = new PolygonBuilder(SpatialReference.create(26729));
                coloradoCorners.addPoint(xmax, ymax);
                coloradoCorners.addPoint(xmax, ymax);
                coloradoCorners.addPoint(xmin, ymin);
                coloradoCorners.addPoint(xmin, ymax);

                SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.MAGENTA, null);

                // create the graphic with polyline and symbol
                Graphic graphic = new Graphic(features.get(i).getGeometry());
                // add graphic to the graphics overlay
                // create a graphics overlay
                graphicsOverlay = new GraphicsOverlay();

                SimpleRenderer simpleRenderer = new SimpleRenderer(polygonSymbol);
                graphicsOverlay.setRenderer(simpleRenderer);
                graphicsOverlay.getGraphics().add(graphic);
                // add graphics overlay to the map view
                ((SevenActivity) activity).mMapView.getGraphicsOverlays().add(graphicsOverlay);
                ((SevenActivity) activity).mMapView.setViewpointCenterAsync(features.get(i).getGeometry().getExtent().getCenter());
            }
        });
    }

    @Override
    public int getItemCount() {
        return features.size();
    }
}
