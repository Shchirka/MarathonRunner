package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import cn.iwgang.countdownview.CountdownView;
import io.paperdb.Paper;
import ua.kpi.comsys.androidrunner.service.LocationService;

import static ua.kpi.comsys.androidrunner.RunActivity.clearAll;
import static ua.kpi.comsys.androidrunner.RunActivity.showDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.showTime;
import static ua.kpi.comsys.androidrunner.service.LocationService.ACTION_START_LOCATION_SERVICE;
import static ua.kpi.comsys.androidrunner.service.LocationService.ACTION_STOP_LOCATION_SERVICE;

public class SingleMarathonActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String FINE_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String IS_START_KEY = "IS_START";
    private static final String LAST_TIME_SAVE_TIME = "LAST_TIME_SAVE";
    private static final String TIME_REMAIN_KEY = "TIME_REMAIN";
    private static final String TAG = "SingleMarathonActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static boolean IS_LOCATION_SERVICE_RUNNING = false;
    private Boolean mLocationPermissionGranted = false;
    public String[] permissionsLocation;

    private ImageView refresh;
    private GoogleMap mMap;
    private ImageView runMarathon;
    ProgressDialog progressDialog;
    CountdownView countdownView;

    private double distanceDifference = 0;
    private double distance;
    private double time;

    private boolean isStart;
    static Handler handler;
    private MediaPlayer timerSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_marathon);

        refresh = findViewById(R.id.refresh);
        progressDialog = new ProgressDialog(SingleMarathonActivity.this);
        runMarathon = findViewById(R.id.start_marathon_button);
        timerSignal = MediaPlayer.create(this, R.raw.completed_sound);
        countdownView = findViewById(R.id.countdown_marathon);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        handler = new Handler();

        Paper.init(this);
        isStart = Paper.book().read(IS_START_KEY, false);

        //Check time
        if(isStart){
            runMarathon.setEnabled(false);
            checkTime();
        }
        else{
            runMarathon.setEnabled(true);
        }

        getLocationPermission();
    }

    public void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_START_LOCATION_SERVICE);
            startService(intent);
            //Toast.makeText((RunActivity)getActivity(), "Location service started", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            //Toast.makeText((RunActivity)getActivity(), "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTracking(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        else{
            startLocationService();
        }
    }

    private void stopTracking(){
        stopLocationService();
    }

    public boolean isLocationServiceRunning(){
        ActivityManager activityManager =  (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for(ActivityManager.RunningServiceInfo serviceInfo :
                    activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(serviceInfo.service.getClassName())){
                    if(serviceInfo.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.marathon_map);
        mapFragment.getMapAsync(SingleMarathonActivity.this);
    }

    private void getLocationPermission() {
        permissionsLocation = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissionsLocation, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsLocation, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //initialize map
                    initMap();
                }
                break;
        }
    }

    private void getDeviceLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(SingleMarathonActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(SingleMarathonActivity.this)
                                .removeLocationUpdates(this);
                        if(locationResult != null && locationResult.getLocations().size() > 0){
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM);
                            Toast.makeText(SingleMarathonActivity.this, "Turn on your geolocation to see where  you are", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void checkTime() {
        Paper.book().write(TIME_REMAIN_KEY, countdownView.getRemainTime());
        Paper.book().write(LAST_TIME_SAVE_TIME, System.currentTimeMillis());
        long currentTime = System.currentTimeMillis();
        long lastTimeSaved = Paper.book().read(LAST_TIME_SAVE_TIME);
        long timeRemain = Paper.book().read(TIME_REMAIN_KEY);
        long result = timeRemain + (lastTimeSaved - currentTime);
        if(result > 0){
            countdownView.start(result);
        }
        else{
            countdownView.stop();
            reset();
        }
    }

    public void closeTimer(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
        builder.setTitle("Closing");
        builder.setMessage("Are you sure you wanna close the marathon? Your time will be reset");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ReallyComeBack();
                dialog.dismiss();
                clearAll();
                showDistance.setText("");
                showTime.setText("");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void reset(){
        runMarathon.setEnabled(false);
        Paper.book().delete(IS_START_KEY);
        Paper.book().delete(LAST_TIME_SAVE_TIME);
        Paper.book().delete(TIME_REMAIN_KEY);

        isStart = false;
    }

    public static double countDistance(double lat1, double lat2, double long1, double long2){
        double longDifference = long1 - long2;
        double distance = Math.sin(degToRad(lat1)) * Math.sin(degToRad(lat2))
                + Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) * Math.cos(degToRad(longDifference));
        distance = Math.acos(distance);
        distance = radToDeg(distance);
        //distance in miles
        distance = distance * 60.0 * 1.1515;
        //distance in metres
        distance = distance * 1.609344 * 1000.0;
        return distance;
    }

    private static double degToRad(double coord){
        return (coord*Math.PI/180.0);
    }

    private static double radToDeg(double distance){
        return (distance * 180.0/Math.PI);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void ClickBack(View view)
    {
        closeTimer(SingleMarathonActivity.this);
    }

    public void ReallyComeBack(){
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        closeTimer(SingleMarathonActivity.this);
    }

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}