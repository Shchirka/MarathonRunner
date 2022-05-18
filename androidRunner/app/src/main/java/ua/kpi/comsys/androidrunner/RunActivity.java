package ua.kpi.comsys.androidrunner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.kpi.comsys.androidrunner.fragment.ChronometerFragment;
import ua.kpi.comsys.androidrunner.fragment.TimerFragment;
import ua.kpi.comsys.androidrunner.fragment.TrainingFragment;
import ua.kpi.comsys.androidrunner.models.UploadPost;
import ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity;

import static ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity.trainingIsChosen;
import static ua.kpi.comsys.androidrunner.service.LocationService.coordinates;


public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {

    private StorageReference storageReference;
    private DatabaseReference databaseReferencePost;
    private DatabaseReference databaseReferenceHistory;
    private FirebaseUser firebaseUser;

    DrawerLayout drawerLayout;
    private static final String TAG = "RunActivity";
    private static final String FINE_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int WRITE_REQUEST_CODE = 2;
    public static boolean IS_LOCATION_SERVICE_RUNNING = false;
    private ImageView refresh;
    private AppCompatButton clearButton, postButton;
    private Boolean mLocationPermissionGranted = false;
    private Boolean mStoragePermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public static GoogleMap mMap;
    public static TextView showDistance, showTime;

    public static double distanceDifference = 0;
    public static double postDistance;
    public static double postTime;

    ProgressDialog progressDialog;
    private String userID;
    public Bitmap bitmap;
    public String[] permissionsLocation;
    public String[] permissionsStorage;

    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReferencePost = FirebaseDatabase.getInstance().getReference("Posts");
        databaseReferenceHistory = FirebaseDatabase.getInstance().getReference("History");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = firebaseUser.getUid();

        drawerLayout = findViewById(R.id.android_runner_navigation);
        refresh = findViewById(R.id.refresh);
        clearButton = findViewById(R.id.clear_btn);
        postButton = findViewById(R.id.post_btn);
        showDistance = findViewById(R.id.distance);
        showTime = findViewById(R.id.time);
        progressDialog = new ProgressDialog(RunActivity.this);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RunActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("Clearing");
                builder.setMessage("You wanna remove your result?");

                builder.setPositiveButton("Yep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearAll();
                        showDistance.setText("");
                        showTime.setText("");
                    }
                });
                builder.setNegativeButton("Noooo!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!showTime.getText().toString().trim().equals("")
                        && !showDistance.getText().toString().trim().equals("")){
                    uploadPost(RunActivity.this);
                }
                else{
                    Toast.makeText(RunActivity.this, "You haven't run yet!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getLocationPermission();
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(RunActivity.this);
    }

    private void getLocationPermission() {
        permissionsLocation = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                getStoragePermission();
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissionsLocation, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsLocation, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getStoragePermission(){
        permissionsStorage = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), permissionsStorage[0]) == PackageManager.PERMISSION_GRANTED){
            mStoragePermissionGranted = true;
        }
        else{
            ActivityCompat.requestPermissions(this, permissionsStorage, WRITE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        mStoragePermissionGranted = false;
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
            case WRITE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mStoragePermissionGranted = true;
                }
                else{
                    mStoragePermissionGranted = false;
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
        LocationServices.getFusedLocationProviderClient(RunActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(RunActivity.this)
                                .removeLocationUpdates(this);
                        if(locationResult != null && locationResult.getLocations().size() > 0){
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM);
                            Toast.makeText(RunActivity.this, "Turn on your geolocation to see where  you are", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public static void clearAll(){
        if(ChronometerFragment.route != null) ChronometerFragment.route.remove();
        if(TimerFragment.route != null) TimerFragment.route.remove();
        if(TrainingFragment.route != null) TrainingFragment.route.remove();
        coordinates.clear();
        distanceDifference = 0;
        mMap.clear();
        TrainingFragment.tableIsEmpty = true;
        TrainingFragment.trainingTable.removeAllViews();
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

    private void uploadPost(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
        builder.setTitle("Posting");
        builder.setMessage("You wanna post your result?");

        builder.setPositiveButton("Of course!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(RunActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ){
                    return;
                }
                GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(@Nullable Bitmap snapshot) {
                        bitmap = snapshot;
                        if(mStoragePermissionGranted){
                            try {
                                String currentTime = new SimpleDateFormat("-yyyy-MM-dd_HH_mm",
                                        Locale.getDefault()).format(new Date());
                                String mapName = "map" + currentTime
                                        + ".jpg";
                                FileOutputStream out = new FileOutputStream("/sdcard/Download/" + mapName);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                Uri uri = getImageUri(RunActivity.this, bitmap, mapName);
                                progressDialog.setTitle("Posting your result...");
                                progressDialog.show();
                                StorageReference mStorageReference = storageReference.child("Users")
                                        .child(userID).child("PostImages").child(mapName);
                                mStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri downloadUrl) {
                                                double timeOfRun = postTime;
                                                double distanceOfRun = postDistance;
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Post Uploaded Successfully ",
                                                        Toast.LENGTH_LONG).show();
                                                UploadPost uploadPost = new UploadPost(currentTime.substring(1), downloadUrl.toString(),
                                                        timeOfRun, distanceOfRun);
                                                String postID = databaseReferencePost.child(userID).push().getKey();
                                                databaseReferencePost.child(userID).child(postID).setValue(uploadPost);
                                            }
                                        });
                                    }
                                });
                                StorageReference mStorageReferenceHistory = storageReference.child("Users")
                                        .child(userID).child("HistoryImages").child(mapName);
                                mStorageReferenceHistory.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mStorageReferenceHistory.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri downloadUrl) {
                                                double timeOfRun = postTime;
                                                double distanceOfRun = postDistance;
                                                progressDialog.dismiss();
                                                UploadPost uploadPost = new UploadPost(currentTime.substring(1), downloadUrl.toString(),
                                                        timeOfRun, distanceOfRun);
                                                String historyPostID = databaseReferenceHistory.child(userID).push().getKey();
                                                databaseReferenceHistory.child(userID).child(historyPostID).setValue(uploadPost);
                                                clearAll();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(RunActivity.this, "You didn't give your permission to store your data." +
                                            "Please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                if(mStoragePermissionGranted){
                    mMap.snapshot(callback);
                    showDistance.setText("");
                    showTime.setText("");
                }
            }
        });
        builder.setNegativeButton("Emm... Nope", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rememberPost(RunActivity.this);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void rememberPost(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
        builder.setTitle("For history");
        builder.setMessage("You wanna remember your result?");

        builder.setPositiveButton("Yes, I guess", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(RunActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ){
                    return;
                }
                GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(@Nullable Bitmap snapshot) {
                        bitmap = snapshot;
                        if(mStoragePermissionGranted){
                            try {
                                String currentTime = new SimpleDateFormat("-yyyy-MM-dd_HH_mm",
                                        Locale.getDefault()).format(new Date());
                                String mapName = "map" + currentTime
                                        + ".jpg";
                                FileOutputStream out = new FileOutputStream("/sdcard/Download/" + mapName);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                Uri uri = getImageUri(RunActivity.this, bitmap, mapName);
                                progressDialog.setTitle("Remembering your result...");
                                progressDialog.show();
                                StorageReference mStorageReference = storageReference.child("Users")
                                        .child(userID).child("HistoryImages").child(mapName);
                                mStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                double timeOfRun = postTime;
                                                double distanceOfRun = postDistance;
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Result remembered successfully ",
                                                        Toast.LENGTH_LONG).show();
                                                UploadPost uploadPost = new UploadPost(currentTime.substring(1), uri.toString(),
                                                        timeOfRun, distanceOfRun);
                                                String postID = databaseReferenceHistory.child(userID).push().getKey();
                                                databaseReferenceHistory.child(userID).child(postID).setValue(uploadPost);
                                                clearAll();
                                            }
                                        });
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(RunActivity.this, "You didn't give your permission to store your data." +
                                            "Please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                if(mStoragePermissionGranted){
                    mMap.snapshot(callback);
                    showDistance.setText("");
                    showTime.setText("");
                    //HomeActivity.redirectActivity(RunActivity.this, UsersPostsActivity.class);
                }
            }
        });
        builder.setNegativeButton("No, definitely not", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(RunActivity.this, "You didn't save your result anywhere -_-", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage, String title) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, title, null);
        return Uri.parse(path);
    }

    public void ClickMenu(View view){
        HomeActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view){
        HomeActivity.closeDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        HomeActivity.redirectActivity(this, HomeActivity.class);
    }

    public void ClickRun(View view){
        HomeActivity.redirectActivity(this, MapsPermissionActivity.class);
    }

    public void ClickFriends(View view){
        HomeActivity.redirectActivity(this, FriendsActivity.class);
    }

    public void ClickSettings(View view){
        HomeActivity.redirectActivity(this, SettingsActivity.class);
    }

    public void ClickAboutUs(View view){
        HomeActivity.redirectActivity(this, AboutUsActivity.class);
    }

    public void ClickLogout(View view){
        HomeActivity.logout(this);
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

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}