package com.example.admin.exercise_intermediate.exercise7;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.example.admin.exercise_intermediate.R;

import java.util.ArrayList;

public class SevenActivity extends AppCompatActivity {

    public MapView mMapView;
    private RecyclerView recyclerView;
    private Ex7Adapter adapter;
    public static ArcGISMap map;
    private ArcGISTiledLayer tiledLayerBaseMap;
    private Point screenPoint;

    private ServiceFeatureTable mServiceFeatureTable, mServiceFeatureTable2;
    private FeatureLayer mFeaturelayer, mFeaturelayer2;

    private EditText searchBar;

    private ArrayList<Feature> features;
    BottomSheetBehavior sheetBehavior;
    ConstraintLayout layoutBottomSheet;

    public int selectedPosition = 0;
    TextView name, id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seven);

        mMapView = findViewById(R.id.mapView);
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

        mMapView.setMap(map);


        recyclerView = findViewById(R.id.recyclerView);
        features = new ArrayList<>();
        adapter = new Ex7Adapter(features, this, map);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        name = findViewById(R.id.name);
        id = findViewById(R.id.id);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        name.setText(features.get(selectedPosition).getAttributes().get("OWNER_NAME").toString());
//                        id.setText(features.get(selectedPosition).getAttributes().get("OWNER_ID").toString());
                    }
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 3) {
                    searchForState(editable.toString());
                }
            }
        });


        mMapView.setViewpointCenterAsync(new com.esri.arcgisruntime.geometry.Point(506594.5624499284, 685595.3751017811, SpatialReference.create(26729)), 16);


        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

//        searchForState("gar");

    }

    public void searchForState(final String searchString) {

        // clear any previous selections
        mFeaturelayer2.clearSelection();

        // create objects required to do a selection with a query
        QueryParameters query = new QueryParameters();
        //make search case insensitive
        query.setWhereClause("upper(OWNER_NAME) LIKE '%" + searchString.toUpperCase() + "%'");


        // call select features
        final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable2.queryFeaturesAsync(query);
        // add done loading listener to fire when the selection returns
        future.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // call get on the future to get the result
                    FeatureQueryResult result = future.get();
                    // check there are some results
                    if (result.iterator().hasNext()) {

                        // get the extend of the first feature in the result to zoom to
                        Feature feature = result.iterator().next();
                        Log.i("Feature", feature.getAttributes().get("OWNER_NAME").toString());
//                        Envelope envelope = feature.getGeometry().getExtent();
//                        mMapView.setViewpointGeometryAsync(envelope, 10);

                        //Select the feature
                        mFeaturelayer2.selectFeature(feature);
                        features.clear();
                        features.add(feature);
                        recyclerView.setAdapter(new Ex7Adapter(features, SevenActivity.this, map));

                    }
                    else {
                        Toast.makeText(SevenActivity.this, "No states found with name: " + searchString, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SevenActivity.this, "Feature search failed for: " + searchString + ". Error=" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(getResources().getString(R.string.app_name),
                            "Feature search failed for: " + searchString + ". Error=" + e.getMessage());
                }
            }
        });
    }
}
