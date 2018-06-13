package com.example.admin.exercise_intermediate.exercise8;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.example.admin.exercise_intermediate.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EightActivity extends AppCompatActivity {

    private MapView mMapView;
    private ArcGISMap map;
    private ServiceFeatureTable mServiceFeatureTable, mServiceFeatureTable2;
    private FeatureLayer mFeaturelayer, mFeaturelayer2;
    private ArcGISTiledLayer tiledLayerBaseMap;

    private Button deleteBtn, editBtn, addBtn;
    private EditText typeEdit, confirmedEdit, commentEdit;

    private String type, confirmed, comments;

    private Point mapPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eight);

        deleteBtn = findViewById(R.id.deleteBtn);
        editBtn = findViewById(R.id.editBtn);
        addBtn = findViewById(R.id.addBtn);

        typeEdit = findViewById(R.id.typeEdit);
        commentEdit = findViewById(R.id.commentsEdit);
        confirmedEdit = findViewById(R.id.confirmedEdit);

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

        // add feature layer to operational layer
        map.getOperationalLayers().add(mFeaturelayer);

        mMapView.setMap(map);

        mMapView.setViewpointCenterAsync(new com.esri.arcgisruntime.geometry.Point(0, 50, SpatialReference.create(4326)), 10);

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(EightActivity.this, mMapView) {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPoint = new android.graphics.Point((int)e.getX(), (int)e.getY());

                // convert this to a map point
                mapPoint = mMapView.screenToLocation(screenPoint);

                Toast.makeText(mMapView.getContext(), "map point selected", Toast.LENGTH_SHORT).show();
                // add a feature at this point
//                addFeature(mapPoint);
                return true;
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteBtn.isEnabled()) {
                    deleteBtn.setEnabled(false);
                    editBtn.setEnabled(false);
                } else {
                    addFeature();
                    deleteBtn.setEnabled(true);
                    editBtn.setEnabled(true);
                }


            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addBtn.isEnabled()) {
                    addBtn.setEnabled(false);
                } else {
                    addBtn.setEnabled(true);
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addBtn.isEnabled()) {
                    addBtn.setEnabled(false);
                } else {
                    addBtn.setEnabled(true);
                }
            }
        });
    }

    private void showData() {

    }

    private void addFeature() {
        type = typeEdit.getText().toString();
        confirmed = confirmedEdit.getText().toString();
        comments = commentEdit.getText().toString();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("type", Integer.parseInt(type));
        attributes.put("confirmed", Integer.parseInt(confirmed));
        attributes.put("comments", comments);

        Feature addedFeature = mServiceFeatureTable.createFeature(attributes, mapPoint);
        final ListenableFuture<Void> addFeatureFuture = mServiceFeatureTable.addFeatureAsync(addedFeature);
        addFeatureFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // check the result of the future to find out if/when the addFeatureAsync call succeeded - exception will be
                    // thrown if the edit failed
                    addFeatureFuture.get();

                    // if using an ArcGISFeatureTable, call getAddedFeaturesCountAsync to check the total number of features
                    // that have been added since last sync

                    // if dealing with ServiceFeatureTable, apply edits after making updates; if editing locally, then edits can
                    // be synchronized at some point using the SyncGeodatabaseTask.
                    if (mServiceFeatureTable  instanceof ServiceFeatureTable) {
                        ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) mServiceFeatureTable;
                        // apply the edits
                        final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = serviceFeatureTable.applyEditsAsync();
                        applyEditsFuture.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final List<FeatureEditResult> featureEditResults = applyEditsFuture.get();
                                    // if required, can check the edits applied in this operation
                                    Log.i("add feature", (String.format("Number of edits: %d", featureEditResults.size())));
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                } catch (InterruptedException | ExecutionException e) {
                    // executionException may contain an ArcGISRuntimeException with edit error information.
                    if (e.getCause() instanceof ArcGISRuntimeException) {
                        ArcGISRuntimeException agsEx = (ArcGISRuntimeException)e.getCause();
                        Log.i("add feature", String.format("Add Feature Error %d\n=%s", agsEx.getErrorCode(), agsEx.getMessage()));
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
