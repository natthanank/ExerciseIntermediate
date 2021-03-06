package com.example.exercise9;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.common.collect.Iterators;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    public MapView mMapView;
    private ArcGISTiledLayer tiledLayerBaseMap;
    private com.esri.arcgisruntime.geometry.Point mapPoint;

    private ServiceFeatureTable mServiceFeatureTable, mServiceFeatureTable2, mServiceFeatureTable3;
    private FeatureLayer mFeaturelayer, mFeaturelayer2, mFeaturelayer3;

    private EditText searchBar;
    private Feature myFeature;
    private GraphicsOverlay graphicsOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createMap();
        searchBar = findViewById(R.id.search_bar);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createMap() {
        mMapView = findViewById(R.id.mapView);
        // create tile layer
        tiledLayerBaseMap = new ArcGISTiledLayer(getResources().getString(R.string.world_topo_service));
        // set tiled layer as basemap
        Basemap basemap = new Basemap(tiledLayerBaseMap);
        // create an map instance and pass basemap as argument
        ArcGISMap map = new ArcGISMap(basemap);

        // create service feature table
        mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.service_url));
        // create feature layer from service feature table
        mFeaturelayer = new FeatureLayer(mServiceFeatureTable);
        // create service feature table
        mServiceFeatureTable2 = new ServiceFeatureTable(getString(R.string.service_url_1));
        // create feature layer from service feature table
        mFeaturelayer2 = new FeatureLayer(mServiceFeatureTable2);
        // create service feature table
        mServiceFeatureTable3 = new ServiceFeatureTable(getString(R.string.service_url_2));
        // create feature layer from service feature table
        mFeaturelayer3 = new FeatureLayer(mServiceFeatureTable3);

        // add feature layer to operational layer
        map.getOperationalLayers().add(mFeaturelayer);
        map.getOperationalLayers().add(mFeaturelayer2);
        map.getOperationalLayers().add(mFeaturelayer3);

        mMapView.setMap(map);
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPoint = new android.graphics.Point((int)e.getX(), (int)e.getY());

                // convert this to a map point
                mapPoint = mMapView.screenToLocation(screenPoint);

                Toast.makeText(mMapView.getContext(), "map point selected", Toast.LENGTH_SHORT).show();

                drawBuffer();

                return true;
            }
        });

    }

    private void drawBuffer() {
        // create a graphics overlay to contain the buffered geometry graphics
        graphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(graphicsOverlay);

        // set up units to convert from miles to meters
        final LinearUnit miles = new LinearUnit(LinearUnitId.MILES);
        final LinearUnit meters = new LinearUnit(LinearUnitId.METERS);

        // create a semi-transparent green fill symbol for the buffer regions
        final SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,  0x8800FF00, new
                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF057e15, 5));

        Polygon bufferGeometry = GeometryEngine
                .buffer(mapPoint,miles.convertTo(meters,Double.valueOf(searchBar.getText().toString())));
        // show the buffered region as a green graphic
        Graphic bufferGraphic = new Graphic(bufferGeometry,fillSymbol);
        graphicsOverlay.getGraphics().add(bufferGraphic);
//        // show a red marker where clicked
//        Graphic markerGraphic = new Graphic(mapPoint, new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
//                0xFFFF0000, 5));
//        graphicsOverlay.getGraphics().add(markerGraphic);

        search("Definitely a manatee", bufferGeometry);
    }

    private void search(final String searchString, final Polygon buffer) {

        // clear any previous selections
        mFeaturelayer.clearSelection();

        // create objects required to do a selection with a query
        final QueryParameters query = new QueryParameters();
        //make search case insensitive
//        query.setWhereClause("Comments LIKE '%Definitely a manatee%'");
        query.setWhereClause("1=1");
        query.setGeometry(buffer);
        // call select features
        final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query);
        // add done loading listener to fire when the selection returns
        future.addDoneListener(new Runnable() {


            @Override
            public void run() {
                try {
                    // call get on the future to get the result
                    FeatureQueryResult result = future.get();

                    // check there are some results
                    Iterator<Feature> featureIterator = result.iterator();
                    Feature[] features = Iterators.toArray(featureIterator, Feature.class);
                    for (Feature feature: features){

                        myFeature = feature;

                        Log.i("Feature", myFeature.getAttributes().get("Comments").toString());

                        //Select the feature
                        mFeaturelayer.selectFeature(myFeature);

                        drawFoundSymbol(myFeature);


                    }
//                    else {
//                        Toast.makeText(NineActivity.this, "No states found with name: " + searchString, Toast.LENGTH_SHORT).show();
//                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Feature search failed for: " + searchString + ". Error=" + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(getResources().getString(R.string.app_name),
                            "Feature search failed for: " + searchString + ". Error=" + e.getMessage());
                }
            }

        });
    }

    private void drawFoundSymbol(Feature myFeature) {
        SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, 0xFFFF0000, 10);
        Graphic buoyGraphic1 = new Graphic(myFeature.getGeometry(), redCircleSymbol);
        graphicsOverlay.getGraphics().add(buoyGraphic1);
    }
}
