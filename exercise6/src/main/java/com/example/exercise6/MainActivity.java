package com.example.exercise6;

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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private RecyclerView recyclerView;
    private Ex6Adapter adapter;
    private ArcGISMap map;
    private ArcGISTiledLayer tiledLayerBaseMap;

    private ServiceFeatureTable mServiceFeatureTable, mServiceFeatureTable2;
    private FeatureLayer mFeaturelayer, mFeaturelayer2;
    private ArrayList<LayerForShow> layerForShows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createMap();
        createRecyclerView();
    }

    private void createMap() {
        // MapView
        mMapView = findViewById(R.id.mapView);
        // create tile layer
        tiledLayerBaseMap = new ArcGISTiledLayer(getResources().getString(R.string.world_topo_service));
        // set tiled layer as basemap
        Basemap basemap = new Basemap(tiledLayerBaseMap);
        // create an map instance and pass basemap as argument
        map = new ArcGISMap(basemap);

        addFeatureLayer();

        mMapView.setMap(map);

        // setViewPoint to map
        mMapView.setViewpointCenterAsync(new com.esri.arcgisruntime.geometry.Point(506594.5624499284, 685595.3751017811, SpatialReference.create(26729)), 16);

    }

    private void addFeatureLayer() {
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
    }

    private void createRecyclerView() {
        // Create List of LayerForShow
        layerForShows = new ArrayList<>();
        layerForShows.add(new LayerForShow());
        layerForShows.add(new LayerForShow("MontgomeryBlocks", true));
        layerForShows.add(new LayerForShow("MontgomeryParcelsOwners", true));

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new Ex6Adapter(layerForShows, map);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }
}
