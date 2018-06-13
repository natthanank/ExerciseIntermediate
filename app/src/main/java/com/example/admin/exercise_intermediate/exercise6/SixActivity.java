package com.example.admin.exercise_intermediate.exercise6;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.example.admin.exercise_intermediate.R;

import java.util.ArrayList;

public class SixActivity extends AppCompatActivity {

    private MapView mMapView;
    private RecyclerView recyclerView;
    private Ex6Adapter adapter;
    private ArcGISMap map;
    private ArcGISTiledLayer tiledLayerBaseMap;
    private Point screenPoint;

    private ServiceFeatureTable mServiceFeatureTable, mServiceFeatureTable2;
    private FeatureLayer mFeaturelayer, mFeaturelayer2;
    private ArrayList<LayerForShow> layerForShows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_six);

        recyclerView = findViewById(R.id.recyclerView);
        layerForShows = new ArrayList<>();

        mMapView = findViewById(R.id.mapView);
        map = new ArcGISMap();
        mMapView.setMap(map);

        // create tile layer
        tiledLayerBaseMap = new ArcGISTiledLayer(getResources().getString(R.string.world_topo_service));
        // set tiled layer as basemap
        Basemap basemap = new Basemap(tiledLayerBaseMap);
        // create an map instance and pass basemap as argument
        ArcGISMap map = new ArcGISMap(basemap);

        // create service feature table
        mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.sample_service_url));
        // create feature layer from service feature table
        mFeaturelayer = new FeatureLayer(mServiceFeatureTable);
        // create service feature table
        mServiceFeatureTable2 = new ServiceFeatureTable(getString(R.string.sample_service_url_2));
        // create feature layer from service feature table
        mFeaturelayer2 = new FeatureLayer(mServiceFeatureTable2);

        // add feature layer to operational layer
        map.getOperationalLayers().add(mFeaturelayer);
        map.getOperationalLayers().add(mFeaturelayer2);

        layerForShows.add(new LayerForShow());
        layerForShows.add(new LayerForShow("MontgomeryBlocks", true));
        layerForShows.add(new LayerForShow("MontgomeryParcelsOwners", true));

        adapter = new Ex6Adapter(layerForShows, map);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // set map to mapView
        mMapView.setMap(map);

        mMapView.setViewpointCenterAsync(new com.esri.arcgisruntime.geometry.Point(506594.5624499284, 685595.3751017811, SpatialReference.create(26729)), 16);
    }
}
