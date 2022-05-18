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
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.iwgang.countdownview.CountdownView;
import io.paperdb.Paper;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.RunActivity;
import ua.kpi.comsys.androidrunner.models.User;
import ua.kpi.comsys.androidrunner.service.LocationService;

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

public class TimerFragment extends Fragment {

    private DatabaseReference databaseReferenceUser;

    private static final String IS_START_KEY = "IS_START";
    private static final String LAST_TIME_SAVE_TIME = "LAST_TIME_SAVE";
    private static final String TIME_REMAIN_KEY = "TIME_REMAIN";

    private String input = "";

    private boolean isStart;

    private CountdownView countdownView;
    private AppCompatButton startButton;
    private AppCompatButton runButton;

    static Handler handler;
    public static Polyline route = null;

    private double previousDistance;
    private double previousTime;
    private int previousRuns;
    private int previousPoints;

    private int gainedPoints;
    private String message;
    private MediaPlayer timerSignal;
    Animation animationClose;
    Animation animationAlpha;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        animationAlpha = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.alpha_run);
        animationAlpha.setDuration(500);
        animationClose = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.set_left);
        animationClose.setDuration(1200);

        timerSignal = MediaPlayer.create((RunActivity)getActivity(), R.raw.completed_sound);

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        distanceDifference = 0;

        init();
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                closeTimer((RunActivity)getActivity());
            }
        });
        setupView();
    }

    private void showDialog(){
        AlertDialog.Builder timeBuilder = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
        final View timePickerDialog = getLayoutInflater().inflate(R.layout.pick_time_dialog, null);

        timeBuilder.setView(timePickerDialog);
        timeBuilder.setTitle("Enter time");
        timeBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                EditText editText = timePickerDialog.findViewById(R.id.set_time);
                input = editText.getText().toString();
                dialog.dismiss();
            }
        });
        timeBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        timeBuilder.show();
    }

    public void closeTimer(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
        builder.setTitle("Closing");
        builder.setMessage("Are you sure you wanna close the timer? Your time will be reset");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runButton.startAnimation(animationClose);
                countdownView.startAnimation(animationClose);
                startButton.startAnimation(animationClose);
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

    private void init(){
        startButton = getView().findViewById(R.id.start_timer_btn);
        countdownView = getView().findViewById(R.id.countdown);
        runButton = getView().findViewById(R.id.right_side_run_button);

        startButton.startAnimation(animationAlpha);
        countdownView.startAnimation(animationAlpha);
        runButton.startAnimation(animationAlpha);

        handler = new Handler();

        Paper.init(getView().getContext());
        isStart = Paper.book().read(IS_START_KEY, false);

        //Check time
        if(isStart){
            startButton.setEnabled(false);
            checkTime();
        }
        else{
            startButton.setEnabled(true);
        }
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

    private void setupView() {
        input = RunButtonFragment.EnteredTime;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
                builder.setTitle("Start running");
                builder.setMessage("Start your run?");

                builder.setPositiveButton("Damn yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!isStart){
                            if(input.length() == 0){
                                Toast.makeText((RunActivity)getActivity(), "You didn't enter time", Toast.LENGTH_SHORT).show();
                                showDialog();
                            }
                            else{
                                long millisInput = Long.parseLong(input) * 1000;
                                if(millisInput == 0){
                                    Toast.makeText((RunActivity)getActivity(), "Please enter positive number", Toast.LENGTH_SHORT).show();
                                    showDialog();
                                }
                                else{
                                    startTracking();
                                    countdownView.start(millisInput);
                                    Paper.book().write(IS_START_KEY, true);
                                    startButton.setEnabled(false);
                                    clearAll();
                                }
                            }
                        }
                    }
                });
                builder.setNegativeButton("No, it was an accident", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                stopTracking();
                timerSignal.start();
                //Toast.makeText(getView().getContext(), "Finish", Toast.LENGTH_SHORT).show();
                reset();
                if(route != null) route.remove();
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(coordinates).clickable(false);
                route = mMap.addPolyline(polylineOptions);
                route.setColor(Color.parseColor("#FF8E00"));
                route.setWidth(4);
                for(int i = 1; i < coordinates.size(); i++){
                    distanceDifference += countDistance(coordinates.get(i).latitude, coordinates.get(i - 1).latitude,
                            coordinates.get(i).longitude, coordinates.get(i - 1).longitude);
                }
                double seconds = Long.parseLong(input) * 1000.0;
                postDistance = Math.round(distanceDifference*100)/100;
                postTime = (double) Math.round(seconds*10)/10000.0;
                gainedPoints = (int)(postDistance/postTime);
                if(gainedPoints == 1){
                    message = "You are a beginner? Well, you will be better soon!";
                }else if(gainedPoints == 0) {
                    message = "You were just walking? Oh, you will be better, I promise!";
                }else if(gainedPoints == 2){
                    message = "Well done! But you can run faster, can't you! I believe in you, sportsman";
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
                                        message, Toast.LENGTH_SHORT).show();
                                Toast.makeText((RunActivity)getActivity(),
                                        "You just gained " + gainedPoints + " points!",
                                        Toast.LENGTH_SHORT).show();
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
            }
        });

        countdownView.setOnCountdownIntervalListener(1000, new CountdownView.OnCountdownIntervalListener() {
            @Override
            public void onInterval(CountdownView cv, long remainTime) {
                Log.d("Timer", "" + remainTime);
            }
        });
    }

    public void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getActivity().getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_START_LOCATION_SERVICE);
            getActivity().startService(intent);
            //Toast.makeText((RunActivity)getActivity(), "Location service started", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getActivity().getApplicationContext(), LocationService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            getActivity().startService(intent);
            //Toast.makeText((RunActivity)getActivity(), "Location service stopped", Toast.LENGTH_SHORT).show();
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

    private void reset(){
        startButton.setEnabled(false);
        Paper.book().delete(IS_START_KEY);
        Paper.book().delete(LAST_TIME_SAVE_TIME);
        Paper.book().delete(TIME_REMAIN_KEY);

        isStart = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }
}