package com.example.admin.exercise_intermediate.exercise10;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
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
import com.example.admin.exercise_intermediate.R;
import com.example.admin.exercise_intermediate.exercise9.NineActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TenActivity extends AppCompatActivity {

    private MapView mMapView;
    private RouteTask mRouteTask;
    private RouteParameters mRouteParams;
    private Point mSourcePoint;
    private Point mDestinationPoint;
    private Route mRoute;
    private SimpleLineSymbol mRouteSymbol;
    private GraphicsOverlay mGraphicsOverlay;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private Button findRouteBtn;
    private Point mapPoint;
    private GraphicsOverlay graphicsOverlay;

    private ArrayList<Point> mapPoints;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ten);

        mapPoints = new ArrayList<>();

        mMapView = findViewById(R.id.mapView);
        ArcGISTiledLayer arcGISVectorTiledLayer = new ArcGISTiledLayer(getString(R.string.world_topo_service));
        Basemap basemap = new Basemap(arcGISVectorTiledLayer);
        ArcGISMap map = new ArcGISMap(basemap);
        Viewpoint sanDiegoPoint = new Viewpoint(32.7157, -117.1611, 200000);
        // set initial map extent
        map.setInitialViewpoint(sanDiegoPoint);
        // set the map to be displayed in this view
        mMapView.setMap(map);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        findRouteBtn = findViewById(R.id.findRouteBtn);
        final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findRouteBtn.getLayoutParams();
        mMapView.addAttributionViewLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View view, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int heightDelta = (bottom - oldBottom);
                params.bottomMargin += heightDelta;
            }
        });

        setupDrawer();

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(TenActivity.this, mMapView) {
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

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);
                    setTitle(getString(R.string.app_name));
                }
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

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

                                String[] directionsArray = new String[directions.size()];

                                for (DirectionManeuver dm : directions) {
                                    directionsArray[i++] = dm.getDirectionText();
                                }
                                Log.d("Direction", directions.get(0).getGeometry().getExtent().getXMin() + "");
                                Log.d("Direction", directions.get(0).getGeometry().getExtent().getYMin() + "");

                                // Set the adapter for the list view
                                mDrawerList.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                                        R.layout.directions_layout, directionsArray));

                                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        if (mGraphicsOverlay.getGraphics().size() > 3) {
                                            mGraphicsOverlay.getGraphics().remove(mGraphicsOverlay.getGraphics().size() - 1);
                                        }
                                        mDrawerLayout.closeDrawers();
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
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                });
            }
        });


    }

    /**
     * Set up the Source, Destination and mRouteSymbol graphics symbol
     */
    private void setupSymbols() {

        mGraphicsOverlay = new GraphicsOverlay();

        //add the overlay to the map view
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable startDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.arcgisruntime_location_display_compass_symbol);
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
            pinDestinationSymbol.setOffsetY(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //[DocRef: END]
        mRouteSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    /**
     * set up the drawer
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Activate the navigation drawer toggle
        return (mDrawerToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }
}
