package com.example.exercise10;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private RouteTask mRouteTask;
    private RouteParameters mRouteParams;
    private Point mSourcePoint;
    private Point mDestinationPoint;
    private Route mRoute;
    private SimpleLineSymbol mRouteSymbol;
    private GraphicsOverlay mGraphicsOverlay;

    private ListView mDrawerList;

    private Button findRouteBtn;
    private Point mapPoint;
    private GraphicsOverlay graphicsOverlay;

    private ArrayList<Point> mapPoints;

    private BottomSheetBehavior sheetBehavior;
    private ConstraintLayout layoutBottomSheet;
    private TextView routeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud6527399236,none,HC5X0H4AH5E5LMZ59231");

        mapPoints = new ArrayList<>();
        mDrawerList = findViewById(R.id.left_drawer);
        routeText = findViewById(R.id.routeText);
        findRouteBtn = findViewById(R.id.findRouteBtn);
        createMap();
        setupDrawer();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createMap() {
        mMapView = findViewById(R.id.mapView);
        ArcGISTiledLayer arcGISVectorTiledLayer = new ArcGISTiledLayer(getString(R.string.world_topo_service));
        Basemap basemap = new Basemap(arcGISVectorTiledLayer);
        ArcGISMap map = new ArcGISMap(basemap);
        Viewpoint sanDiegoPoint = new Viewpoint(32.7157, -117.1611, 200000);
        // set initial map extent
        map.setInitialViewpoint(sanDiegoPoint);
        // set the map to be displayed in this view
        mMapView.setMap(map);

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPoint = new android.graphics.Point((int)e.getX(), (int)e.getY());

                // convert this to a map point
                mapPoint = mMapView.screenToLocation(screenPoint);
                mapPoints.add(mapPoint);

                Toast.makeText(mMapView.getContext(), "map point selected", Toast.LENGTH_SHORT).show();

                // create a graphics overlay to contain the buffered geometry graphics
                graphicsOverlay = new GraphicsOverlay();
                mMapView.getGraphicsOverlays().add(graphicsOverlay);

                // set up units to convert from miles to meters
                final LinearUnit miles = new LinearUnit(LinearUnitId.MILES);
                final LinearUnit meters = new LinearUnit(LinearUnitId.METERS);

                // create a semi-transparent green fill symbol for the buffer regions
                final SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,  0x8800FF00, new
                        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF057e15, 5));

                // show a red marker where clicked
                Graphic markerGraphic = new Graphic(mapPoint, new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                        0xFFFF0000, 5));
                graphicsOverlay.getGraphics().add(markerGraphic);

                return true;
            }
        });

        findRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setupSymbols();

                // create RouteTask instance
                mRouteTask = new RouteTask(getApplicationContext(), getString(R.string.routing_service));

                final ListenableFuture<RouteParameters> listenableFuture = mRouteTask.createDefaultParametersAsync();
                listenableFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (listenableFuture.isDone()) {
                                int i = 0;
                                mRouteParams = listenableFuture.get();

                                List<Stop> routeStops = new ArrayList<>();
                                // add stops
                                for (Point point: mapPoints) {
                                    routeStops.add(new Stop(point));
                                }
                                mRouteParams.setStops(routeStops);

                                // set return directions as true to return turn-by-turn directions in the result of
                                // getDirectionManeuvers().
                                mRouteParams.setReturnDirections(true);

                                // solve
                                RouteResult result = mRouteTask.solveRouteAsync(mRouteParams).get();
                                final List routes = result.getRoutes();
                                mRoute = (Route) routes.get(0);
                                // create a mRouteSymbol graphic
                                Graphic routeGraphic = new Graphic(mRoute.getRouteGeometry(), mRouteSymbol);
                                // add mRouteSymbol graphic to the map
                                mGraphicsOverlay.getGraphics().add(routeGraphic);
                                // get directions
                                // NOTE: to get turn-by-turn directions Route Parameters should set returnDirection flag as true
                                final List<DirectionManeuver> directions = mRoute.getDirectionManeuvers();

                                final String[] directionsArray = new String[directions.size()];

                                for (DirectionManeuver dm : directions) {
                                    directionsArray[i++] = dm.getDirectionText();
                                }


                                setRouteList(directions, directionsArray);


                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                });

                sheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

            }
        });


    }

    private void setRouteList(final List<DirectionManeuver> directions, final String[] directionsArray) {
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                R.layout.directions_layout, directionsArray));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (mGraphicsOverlay.getGraphics().size() > 3) {
                    mGraphicsOverlay.getGraphics().remove(mGraphicsOverlay.getGraphics().size() - 1);
                }
                routeText.setText(directionsArray[position]);
                routeText.setVisibility(View.VISIBLE);
                DirectionManeuver dm = directions.get(position);
                Geometry gm = dm.getGeometry();
                Viewpoint vp = new Viewpoint(gm.getExtent(), 20);
                mMapView.setViewpointAsync(vp, 3);
                SimpleLineSymbol selectedRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
                        Color.GREEN, 5);
                Graphic selectedRouteGraphic = new Graphic(directions.get(position).getGeometry(),
                        selectedRouteSymbol);
                mGraphicsOverlay.getGraphics().add(selectedRouteGraphic);
            }
        });
    }

    /**
     * set up the drawer
     */
    private void setupDrawer() {
        layoutBottomSheet = findViewById(R.id.bottom_sheet_7);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    private void setupSymbols() {

        mGraphicsOverlay = new GraphicsOverlay();

        //add the overlay to the map view
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.arcgisruntime_location_display_default_symbol);
        final PictureMarkerSymbol pinSourceSymbol;
        try {
            pinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get();
            pinSourceSymbol.loadAsync();
            pinSourceSymbol.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    //add a new graphic as start point
                    mSourcePoint = mapPoints.get(0);
                    Graphic pinSourceGraphic = new Graphic(mSourcePoint, pinSourceSymbol);
                    mGraphicsOverlay.getGraphics().add(pinSourceGraphic);
                }
            });
            pinSourceSymbol.setOffsetY(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //[DocRef: END]
        BitmapDrawable endDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.arcgisruntime_location_display_default_symbol);
        final PictureMarkerSymbol pinDestinationSymbol;
        try {
            pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get();
            pinDestinationSymbol.loadAsync();
            pinDestinationSymbol.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    //add a new graphic as end point
                    mDestinationPoint = mapPoints.get(mapPoints.size() - 1);
                    Graphic destinationGraphic = new Graphic(mDestinationPoint, pinDestinationSymbol);
                    mGraphicsOverlay.getGraphics().add(destinationGraphic);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //[DocRef: END]
        mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
    }
}
