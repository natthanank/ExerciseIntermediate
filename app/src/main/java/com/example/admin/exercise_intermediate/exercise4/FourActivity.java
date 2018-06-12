package com.example.admin.exercise_intermediate.exercise4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.example.admin.exercise_intermediate.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class FourActivity extends AppCompatActivity {

    Button shareBtn;
    private final String TAG = FourActivity.class.getSimpleName();

    private ArcGISMapImageLayer mMapImageLayer;
    private MapView mMapView;

    double lat;
    double lon;

    String location;

    File file;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);

        shareBtn = findViewById(R.id.shareButton);
        mMapView = findViewById(R.id.mapView);


        // create an instance of a map
        final ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 10);

        mMapView.setMap(map);
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                // get the point that was clicked and convert it to a point in map coordinates
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
                        Math.round(motionEvent.getY()));
                // create a map point from screen point
                Point mapPoint = mMapView.screenToLocation(screenPoint);
                // convert to WGS84 for lat/lon format
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                lat = wgs84Point.getY();
                lon = wgs84Point.getX();
                // center on tapped point
                mMapView.setViewpointCenterAsync(mapPoint);


                location = "latitude: " + Double.toString(lat) + "\nlongitude: " + Double.toString(lon);
                Toast.makeText(this.mMapView.getContext(), location, Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeToSDFile();
                Intent intent = new Intent(Intent.ACTION_SEND);
                String mimeType = "text/plain";
                intent.setType(mimeType);
                Log.i("uri", Uri.fromFile(file).getPath());
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                startActivity(Intent.createChooser(intent, "Share file via"));
            }
        });
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /** Method to write ascii text characters to file on SD card. Note that you must add a
     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     a FileNotFound Exception because you won't have write permission. */

    private void writeToSDFile(){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();
        file = new File(dir, "location.txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(location);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        readRaw();
    }

    /** Method to read in a text file placed in the res/raw directory of the application. The
     method reads in all lines of the file sequentially. */

    private void readRaw(){
        File root = android.os.Environment.getExternalStorageDirectory();
        String message = "";

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
        try {
            File dir = new File (root.getAbsolutePath() + "/download");
            FileInputStream fis = new FileInputStream(new File(dir, "location.txt"));
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // 2nd arg is buffer size

            // More efficient (less readable) implementation of above is the composite expression
            /*BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getResources().openRawResource(R.raw.textfile)), 8192);*/


            String test;
            while (true){
                test = br.readLine();
                message = message + test;
                // readLine() returns null if no more lines in the file
                if(test == null) break;
            }
            in.close();
            br.close();

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
