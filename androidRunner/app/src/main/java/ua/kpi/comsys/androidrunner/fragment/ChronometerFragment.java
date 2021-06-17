package ua.kpi.comsys.androidrunner.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.RunActivity;
import ua.kpi.comsys.androidrunner.models.User;
import ua.kpi.comsys.androidrunner.service.LocationService;

import static ua.kpi.comsys.androidrunner.RunActivity.IS_LOCATION_SERVICE_RUNNING;
import static ua.kpi.comsys.androidrunner.RunActivity.LOCATION_PERMISSION_REQUEST_CODE;
import static ua.kpi.comsys.androidrunner.RunActivity.clearAll;
import static ua.kpi.comsys.androidrunner.RunActivity.countDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.distanceDifference;
import static ua.kpi.comsys.androidrunner.RunActivity.mMap;
import static ua.kpi.comsys.androidrunner.RunActivity.postDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.postTime;
import static ua.kpi.comsys.androidrunner.RunActivity.showDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.showTime;
import static ua.kpi.comsys.androidrunner.service.LocationService.ACTION_START_LOCATION_SERVICE;
import static ua.kpi.comsys.androidrunner.service.LocationService.ACTION_STOP_LOCATION_SERVICE;
import static ua.kpi.comsys.androidrunner.service.LocationService.coordinates;

public class ChronometerFragment extends Fragment implements View.OnClickListener {

    private DatabaseReference databaseReferenceUser;

    private static Chronometer chronometer;
    public static AppCompatButton startChronometer, stopChronometer;
    private AppCompatButton runButton;
    private static boolean running;

    private static long tMilliSec, tStart, tBuff, tUpdate;
    private static int sec, min, milliSec;
    private double previousDistance;
    private double previousTime;
    private int previousRuns;
    private int previousPoints;

    private int gainedPoints;
    private String message;

    static Handler handler;
    public static Polyline route = null;

    Animation animationClose;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Animation animationAlpha = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.alpha_run);
        animationAlpha.setDuration(500);
        animationClose = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.set_right);
        animationClose.setDuration(1200);

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chronometer = getView().findViewById(R.id.chronometer);
        startChronometer = getView().findViewById(R.id.start_chronometer_btn);
        stopChronometer = getView().findViewById(R.id.stop_chronometer_btn);
        stopChronometer.setEnabled(false);
        runButton = getView().findViewById(R.id.left_side_run_button);

        startChronometer.startAnimation(animationAlpha);
        stopChronometer.startAnimation(animationAlpha);
        runButton.startAnimation(animationAlpha);
        chronometer.startAnimation(animationAlpha);

        handler = new Handler();

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer c) {

            }
        });
        startChronometer.setOnClickListener(this);
        stopChronometer.setOnClickListener(this);

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeChronometer((RunActivity)getActivity());
            }
        });
    }

    public static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tMilliSec = SystemClock.uptimeMillis() - tStart;
            tUpdate = tBuff + tMilliSec;
            sec = (int) (tUpdate/1000);
            min = sec/60;
            sec = sec%60;
            milliSec = (int)(tUpdate%100);
            chronometer.setText(String.format("%02d:%02d:%02d", min, sec, milliSec));
            handler.postDelayed(this, 60);
        }
    };

    public static void startChronometer(){
        if(!running){
            tMilliSec = 0L;
            tStart = 0L;
            tBuff = 0L;
            tUpdate = 0L;
            sec = 0;
            min = 0;
            chronometer.setText("00:00:00");
            tStart = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }
    public static void stopChronometer(){
        if(running){
            tBuff += tMilliSec;
            handler.removeCallbacks(runnable);
            chronometer.stop();
            running = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_chronometer_btn:
                AlertDialog.Builder builder = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
                builder.setTitle("Start running");
                builder.setMessage("Start your run?");

                builder.setPositiveButton("Damn yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startTracking();
                        startChronometer();
                        clearAll();
                        startChronometer.setEnabled(false);
                        stopChronometer.setEnabled(true);
                    }
                });
                builder.setNegativeButton("No, it was an accident", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
            case R.id.stop_chronometer_btn:
                AlertDialog.Builder builderStop = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
                builderStop.setTitle("Stop running");
                builderStop.setMessage("Stop your run?");

                builderStop.setPositiveButton("Yeah", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopTracking();
                        stopChronometer();
                        if(route != null) route.remove();
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(coordinates).clickable(false);
                        route = mMap.addPolyline(polylineOptions);
                        route.setColor(Color.parseColor("#FF8E00"));
                        route.setWidth(2);
                        for(int i = 1; i < coordinates.size(); i++){
                            distanceDifference += countDistance(coordinates.get(i).latitude, coordinates.get(i - 1).latitude,
                                    coordinates.get(i).longitude, coordinates.get(i - 1).longitude);
                        }
                        double seconds = tBuff * 1.0;
                        postDistance = Math.round(distanceDifference*100)/100;
                        postTime = (double) Math.round(seconds*10)/10000.0;
                        gainedPoints = (int)(postDistance/postTime);
                        if(gainedPoints == 1){
                            message = "You are beginner? Well, you will be better soon!";
                        }else if(gainedPoints == 0) {
                            message = "You was just walking? Oh, you will be better, I promise!";
                        }else if(gainedPoints == 2){
                            message = "Well done! But you can run faster, can't you? I believe in you, sportsman";
                        }else if(gainedPoints == 3){
                            message = "You are pretty good in it! Are you sure you are not a professional?";
                        }else if(gainedPoints == 4){
                            message = "You run like a real sportsmen do! Wow!";
                        }else if(gainedPoints < 7){
                            message = "I think you have a great abilities! Well done!";
                        }else{
                            message = "You definitely used some kind of transport. I don't believe you...";
                            gainedPoints = 0;
                        }
                        databaseReferenceUser
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User userProfile = snapshot.getValue(User.class);
                                        previousDistance = userProfile.distance;
                                        previousTime = userProfile.spendTime;
                                        previousRuns = userProfile.runs;
                                        previousPoints = userProfile.points;
                                        databaseReferenceUser.child("points").setValue(previousPoints + gainedPoints);
                                        Toast.makeText((RunActivity)getActivity(),
                                                message, Toast.LENGTH_LONG).show();
                                        Toast.makeText((RunActivity)getActivity(),
                                                "You just gained " + gainedPoints + " points!",
                                                Toast.LENGTH_LONG).show();
                                        databaseReferenceUser.child("runs").setValue(previousRuns + 1);
                                        databaseReferenceUser.child("distance").setValue(previousDistance + postDistance);
                                        databaseReferenceUser.child("spendTime").setValue(previousTime + postTime);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        RunActivity.showDistance.setText("Distance: " + postDistance + " metres");
                        RunActivity.showTime.setText("Time: " + postTime + " seconds");
                        startChronometer.setEnabled(true);
                        stopChronometer.setEnabled(false);
                    }
                });
                builderStop.setNegativeButton("No, I'll run more", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderStop.show();
                break;
        }
    }

    public void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getActivity().getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_START_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText((RunActivity)getActivity(), "Location service started", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getActivity().getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText((RunActivity)getActivity(), "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTracking(){
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((RunActivity)getActivity(),
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
        ActivityManager activityManager =  (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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

    public void closeChronometer(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
        builder.setTitle("Closing");
        builder.setMessage("Are you sure you wanna close the stopwatch? Your time will be reset");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runButton.startAnimation(animationClose);
                startChronometer.startAnimation(animationClose);
                stopChronometer.startAnimation(animationClose);
                chronometer.startAnimation(animationClose);
                showDistance.startAnimation(animationClose);
                showTime.startAnimation(animationClose);
                new CountDownTimer(1100, 100){
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }
                    @Override
                    public void onFinish() {
                        Fragment fragment = new RunButtonFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.hide(getActivity().getSupportFragmentManager().findFragmentById(R.id.run_fragment))
                                .add(R.id.run_fragment, fragment);
                        fragmentTransaction.commit();
                        dialog.dismiss();
                        clearAll();
                        showDistance.setText("");
                        showTime.setText("");
                    }
                }.start();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chronometer, container, false);
    }
}