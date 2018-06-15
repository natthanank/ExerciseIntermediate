package com.example.exercise8;

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
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private ArcGISMap map;
    private ServiceFeatureTable mServiceFeatureTable;
    private FeatureLayer mFeaturelayer;
    private ArcGISTiledLayer tiledLayerBaseMap;

    private Button deleteBtn, editBtn, addBtn;
    private EditText typeEdit, confirmedEdit, commentEdit;

    private String type, confirmed, comments;

    private Point mapPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        typeEdit = findViewById(R.id.typeEdit);
        commentEdit = findViewById(R.id.commentsEdit);
        confirmedEdit = findViewById(R.id.confirmedEdit);

        createMap();
        createButton();

        confirmedEdit.setVisibility(View.GONE);
    }

    private void createButton() {

        deleteBtn = findViewById(R.id.deleteBtn);
        editBtn = findViewById(R.id.editBtn);
        addBtn = findViewById(R.id.addBtn);

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
                addBtn.setEnabled(true);
                editFeature();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFeature();
                addBtn.setEnabled(true);

            }
        });
    }

    private void createMap() {
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

        mMapView.setViewpointCenterAsync(new com.esri.arcgisruntime.geometry.Point(-120, 35, SpatialReference.create(4326)), 2000);

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPoint = new android.graphics.Point((int)e.getX(), (int)e.getY());

                // convert this to a map point
                mapPoint = mMapView.screenToLocation(screenPoint);

                Toast.makeText(mMapView.getContext(), "map point selected", Toast.LENGTH_SHORT).show();
                Log.i("mapPoint", "x: " + mapPoint.getX() + "\ny: " + mapPoint.getY());
                // add a feature at this point
                selectFeature();
                return true;
            }
        });

    }

    private void editFeature() {
        final FeatureLayer featureLayer = mServiceFeatureTable.getFeatureLayer();
        final ListenableFuture<FeatureQueryResult> selected = featureLayer.getSelectedFeaturesAsync();
        FeatureQueryResult features;
        try {
            features = selected.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        // check there is at least one selected feature
        if (!features.iterator().hasNext()) {
            Log.i("Update Feature", "No selected features");
            return;
        }

        // get the first selected feature and load it
        final ArcGISFeature feature = (ArcGISFeature) features.iterator().next();
        feature.loadAsync();
        feature.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                // now feature is loaded we can update it; change attribute and geometry (here the point geometry is moved North)
                try {
//                    feature.getAttributes().put("O", typeEdit.getText().toString());
//                    feature.getAttributes().put("Confirmed", confirmedEdit.getText().toString());
                    feature.getAttributes().put("Description", commentEdit.getText().toString());


                    // update the feature in the table
                    mServiceFeatureTable.updateFeatureAsync(feature).get();

                    // if dealing with ServiceFeatureTable, apply edits after making updates; if editing locally, then edits can
                    // be synchronized at some point using the SyncGeodatabaseTask.
                    if (mServiceFeatureTable instanceof ServiceFeatureTable) {
                        ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) mServiceFeatureTable;

                        // can call getUpdatedFeaturesCountAsync to verify number of updates to be applied before calling applyEditsAsync

                        final List<FeatureEditResult> featureEditResults = serviceFeatureTable.applyEditsAsync().get();
                        // if required, can check the edits applied in this operation by using returned FeatureEditResult
                        checkUpdateResults(featureEditResults);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
    }

    private void checkUpdateResults(List<FeatureEditResult> featureEditResults) {
        if (featureEditResults.iterator().hasNext()) {
            Log.i("Feature Edit Result", Integer.toString(featureEditResults.size()));
        }
    }

    private void addFeature() {
        type = typeEdit.getText().toString();
        confirmed = confirmedEdit.getText().toString();
        comments = commentEdit.getText().toString();
        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("confirmed", Integer.parseInt(confirmed));
        attributes.put("Description", comments);
//        attributes.put("SymbolID", "4");

        final Feature addedFeature = mServiceFeatureTable.createFeature(attributes, mapPoint);
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

                } catch (Exception e) {
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

    private void deleteFeature() {

        Log.i("is Editable", Boolean.toString(mServiceFeatureTable.isEditable()));
        Log.i("is CanAdd", Boolean.toString(mServiceFeatureTable.canAdd()));
        List<Field> fieldList = mServiceFeatureTable.getEditableAttributeFields();
        for (Field field: fieldList) {
            Log.i("Field", field.getName());
        }


//        Log.i("is C", Boolean.toString(mServiceFeatureTable.isEditable()));
//        Log.i("is Editable", Boolean.toString(mServiceFeatureTable.isEditable()));


        // get selected features from the layer for this ArcGISFeatureTable
        final FeatureLayer featureLayer = mServiceFeatureTable.getFeatureLayer();
        final ListenableFuture<FeatureQueryResult> selected = featureLayer.getSelectedFeaturesAsync();

        FeatureQueryResult features;
        try {
            features = selected.get();

            // get selected features
            if (!features.iterator().hasNext()) {
                Log.i("Delete", "Use Edit Features > Select features first");
                return;
            }

            // delete the selected features
            mServiceFeatureTable.deleteFeaturesAsync(features).get();

            //if dealing with ServiceFeatureTable, apply edits after making updates; if editing locally, then edits can
            // be synchronized at some point using the SyncGeodatabaseTask.
            if (mServiceFeatureTable instanceof ServiceFeatureTable) {
                ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) mServiceFeatureTable;

                // can call getDeletedFeaturesCountAsync() to verify number of deletes to be applied before calling applyEditsAsync

                final List<FeatureEditResult> featureEditResults = serviceFeatureTable.applyEditsAsync().get();
                // if required, can check the edits applied in this operation by using returned FeatureEditResult
            }
        } catch (ExecutionException | InterruptedException e) {

            if (e.getCause() instanceof ArcGISRuntimeException) {
                Log.e("deleteerror", ((ArcGISRuntimeException) e.getCause()).getErrorCode() + "code");
            }
            return;
        }
    }

    private void selectFeature() {
        // create a buffer from the point
        final Polygon searchGeometry = GeometryEngine.buffer(mapPoint, 5);

        // create a query to find which features are contained by the query geometry
        QueryParameters queryParams = new QueryParameters();
        queryParams.setGeometry(searchGeometry);
        queryParams.setSpatialRelationship(QueryParameters.SpatialRelationship.CONTAINS);

        // select based on the query
        final ListenableFuture<FeatureQueryResult> selectFuture =
                mServiceFeatureTable.getFeatureLayer().selectFeaturesAsync(queryParams, FeatureLayer.SelectionMode.NEW);
        // if required, can listen to the future to perform an action when features are selected
        selectFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                FeatureQueryResult features;
                try {
                    features = selectFuture.get();
                    if (features.iterator().hasNext()) {
                        final ArcGISFeature feature = (ArcGISFeature) features.iterator().next();
                        feature.loadAsync();
                        feature.addDoneLoadingListener(new Runnable() {
                            @Override
                            public void run() {
                                typeEdit.setText(feature.getAttributes().get("OBJECTID").toString());
//                                confirmedEdit.setText(feature.getAttributes().get("Confirmed").toString());
                                commentEdit.setText(feature.getAttributes().get("Description").toString());

                                Log.i("ObjectID", feature.getAttributes().get("OBJECTID").toString());
                                addBtn.setEnabled(false);
                            }
                        });

                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
