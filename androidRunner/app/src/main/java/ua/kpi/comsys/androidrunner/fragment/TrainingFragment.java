package ua.kpi.comsys.androidrunner.fragment;

import android.Manifest;
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

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Table;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cn.iwgang.countdownview.CountdownView;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.RunActivity;
import static ua.kpi.comsys.androidrunner.RunActivity.countDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.distanceDifference;
import static ua.kpi.comsys.androidrunner.RunActivity.mMap;
import static ua.kpi.comsys.androidrunner.RunActivity.postDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.postTime;
import static ua.kpi.comsys.androidrunner.RunActivity.showDistance;
import static ua.kpi.comsys.androidrunner.RunActivity.showTime;

import ua.kpi.comsys.androidrunner.models.User;
import ua.kpi.comsys.androidrunner.service.LocationService;

import static ua.kpi.comsys.androidrunner.RunActivity.LOCATION_PERMISSION_REQUEST_CODE;
import static ua.kpi.comsys.androidrunner.RunActivity.clearAll;
import static ua.kpi.comsys.androidrunner.RunActivity.mMap;
import static ua.kpi.comsys.androidrunner.service.LocationService.ACTION_START_LOCATION_SERVICE;
import static ua.kpi.comsys.androidrunner.service.LocationService.ACTION_STOP_LOCATION_SERVICE;
import static ua.kpi.comsys.androidrunner.service.LocationService.coordinates;


public class TrainingFragment extends Fragment{

    private DatabaseReference databaseReferenceUser;

    public static TableLayout trainingTable;
    private AppCompatButton startTrainingButton, setUpTrainingButton, continueTrainingButton, finishTrainingButton;

    public static int enteredTracks;
    private int index;

    public static boolean tableIsEmpty = true;
    public static Polyline route = null;

    private ArrayList<EditText> values;
    private ArrayList<TableRow> rows;
    private  ArrayList<Double> speedValues, distanceValues, timeValues;

    private int gainedPoints;
    private double speed;

    private double previousDistance;
    private double previousTime;
    private int previousRuns;
    private int previousPoints;

    private MediaPlayer timerSignal;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startTrainingButton = getView().findViewById(R.id.training_start_button);
        continueTrainingButton = getView().findViewById(R.id.training_continue_button);
        finishTrainingButton = getView().findViewById(R.id.training_finish_button);
        setUpTrainingButton = getView().findViewById(R.id.training_setup_button);
        trainingTable = getView().findViewById(R.id.training_table);

        rows = new ArrayList<>();
        speedValues = new ArrayList<>();
        distanceValues = new ArrayList<>();
        timeValues = new ArrayList<>();

        index = 0;

        distanceDifference = 0;

        timerSignal = MediaPlayer.create((RunActivity)getActivity(), R.raw.completed_sound);

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        startTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTraining();
            }
        });
        setUpTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpTraining();
            }
        });
        finishTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTraining();
            }
        });
        continueTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueTraining();
            }
        });

    }

    private void setUpTraining() {
        AlertDialog.Builder setUp = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
        final View setUpDialog_1 = getLayoutInflater().inflate(R.layout.set_up_training_dialog_1, null);

        setUp.setView(setUpDialog_1);
        setUp.setTitle("Tracks");
        setUp.setPositiveButton("Next", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                clearAll();
                continueTrainingButton.setEnabled(false);
                continueTrainingButton.setVisibility(View.INVISIBLE);
                finishTrainingButton.setVisibility(View.INVISIBLE);
                finishTrainingButton.setEnabled(false);
                startTrainingButton.setEnabled(true);
                startTrainingButton.setVisibility(View.VISIBLE);
                EditText editText = setUpDialog_1.findViewById(R.id.set_amount_of_races);
                if(editText.getText().toString().equals("")){
                    Toast.makeText((RunActivity)getActivity(), "You haven't written the number!", Toast.LENGTH_SHORT).show();
                    setUpTraining();
                }else {
                    enteredTracks = Integer.valueOf(editText.getText().toString());
                }

                values = new ArrayList<>();
                rows = new ArrayList<>();

                AlertDialog.Builder setUpValues = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
                final View setUpDialog_2 = getLayoutInflater().inflate(R.layout.set_up_training_dialog_2, null);

                setUpValues.setView(setUpDialog_2);
                TableLayout enteredValues = setUpDialog_2.findViewById(R.id.values_training);
                for(int i = 0; i < enteredTracks; i++){
                    String index = Integer.toString(i + 1);
                    TableRow row = new TableRow(getView().getContext());
                    TextView num = new TextView(getView().getContext());
                    EditText value = new EditText(getView().getContext());

                    num.setText(index);
                    num.setTextAppearance(R.style.marathon_runner);
                    num.setMinWidth(16);

                    value.setInputType(InputType.TYPE_CLASS_NUMBER);
                    value.setHint("Enter a value");
                    value.setHintTextColor(Color.parseColor("#FFDBAE"));
                    value.setTextColor(Color.parseColor("#FFC172"));

                    values.add(value);

                    row.addView(num, 0);
                    row.addView(value, 1);

                    enteredValues.addView(row, i);
                }
                setUpValues.setTitle("Values");
                setUpValues.setMessage("Enter values in seconds for each part of your training, please");
                setUpValues.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < values.size(); i++){
                            String index = Integer.toString(i + 1);
                            TableRow row = new TableRow(getView().getContext());
                            TextView num = new TextView(getView().getContext());
                            TextView value = new TextView(getView().getContext());

                            num.setText("Track # " + index);
                            num.setTextAppearance(R.style.marathon_runner);
                            num.setMinWidth(16);

                            value.setText(values.get(i).getText().toString());
                            value.setTextAppearance(R.style.marathon_runner);
                            value.setMinWidth(16);

                            row.addView(num, 0);
                            row.addView(value, 1);

                            trainingTable.addView(row, i);
                            rows.add(row);
                        }
                        tableIsEmpty = false;
                        index = 0;
                        dialog.dismiss();
                    }
                });
                setUpValues.show();
                dialog.dismiss();
            }
        });
        setUp.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        setUp.show();
    }

    private void startTraining() {
        if(tableIsEmpty){
            AlertDialog.Builder builder = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
            builder.setTitle("Warning!");
            builder.setMessage("You haven't set up your training yet! Do you want to do it now?");

            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setUpTraining();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No...", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
            builder.setTitle("Start your training");
            builder.setMessage("Do you want to start your training?");

            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startTimers(rows.get(index), index);

                   // startTracking();

                    startTrainingButton.setVisibility(View.INVISIBLE);
                    startTrainingButton.setEnabled(false);

                    if(index == rows.size() - 1){
                        finishTrainingButton.setVisibility(View.VISIBLE);
                    }else{
                        continueTrainingButton.setVisibility(View.VISIBLE);
                    }
                    index++;
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No...", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private void continueTraining(){
        AlertDialog.Builder builder = new AlertDialog.Builder((RunActivity)getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle("Continue your training");
        builder.setMessage("Do you want to continue your training?");

        builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                continueTrainingButton.setEnabled(false);

                startTimers(rows.get(index), index);

                if(index == rows.size() - 1){
                    continueTrainingButton.setVisibility(View.INVISIBLE);
                    finishTrainingButton.setVisibility(View.VISIBLE);
                }
                index++;
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void finishTraining(){
        Toast.makeText((RunActivity)getActivity(), "Training is finished", Toast.LENGTH_SHORT).show();

        tableIsEmpty = true;

        trainingTable.removeAllViews();

        TableRow example = new TableRow(getView().getContext());
        TextView trackNumber = new TextView(getView().getContext());
        TextView trackDistance = new TextView(getView().getContext());
        TextView trackTime = new TextView(getView().getContext());
        TextView trackSpeed = new TextView(getView().getContext());

        trackNumber.setText("Track #");
        trackDistance.setText("Distance");
        trackTime.setText("Time");
        trackSpeed.setText("Speed");

        trackNumber.setTextAppearance(R.style.home);
        trackDistance.setTextAppearance(R.style.home);
        trackSpeed.setTextAppearance(R.style.home);
        trackTime.setTextAppearance(R.style.home);

        example.addView(trackNumber, 0);
        example.addView(trackDistance, 1);
        example.addView(trackTime, 2);
        example.addView(trackSpeed, 3);

        trainingTable.addView(example);

        for(int i = 0; i < enteredTracks; i++){
            TableRow tableRow = new TableRow(getView().getContext());
            trackNumber = new TextView(getView().getContext());
            trackDistance = new TextView(getView().getContext());
            trackTime = new TextView(getView().getContext());
            trackSpeed = new TextView(getView().getContext());

            trackNumber.setTextAppearance(R.style.home);
            trackDistance.setTextAppearance(R.style.home);
            trackSpeed.setTextAppearance(R.style.home);
            trackTime.setTextAppearance(R.style.home);

            trackNumber.setText(String.valueOf(i+1));
            trackDistance.setText(String.valueOf(distanceValues.get(i)));
            trackTime.setText(String.valueOf(timeValues.get(i)));
            trackSpeed.setText(String.valueOf(speedValues.get(i)));

            tableRow.addView(trackNumber, 0);
            tableRow.addView(trackDistance, 1);
            tableRow.addView(trackTime, 2);
            tableRow.addView(trackSpeed, 3);

            trainingTable.addView(tableRow);
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

        enteredTracks = 0;

        rows = new ArrayList<>();
        speedValues = new ArrayList<>();
        distanceValues = new ArrayList<>();
        timeValues = new ArrayList<>();

        index = 0;

        distanceDifference = 0;
    }

    private void startTimers(TableRow row, int index){
        CountdownView timerValue = new CountdownView(getView().getContext());
        row.removeViewAt(1);
        row.addView(timerValue, 1);
        timerValue.start(Integer.parseInt(values.get(index).getText().toString())*1000);
        startTracking();
        timerValue.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                timerSignal.start();

                stopTracking();

                mMap.addMarker(new MarkerOptions().position(coordinates.get(coordinates.size()-1)));

                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(coordinates).clickable(false);
                route = mMap.addPolyline(polylineOptions);
                route.setColor(Color.parseColor("#FF8E00"));
                route.setWidth(4);

                distanceDifference = 0;

                for(int i = 1; i < coordinates.size(); i++){
                    distanceDifference += countDistance(coordinates.get(i).latitude, coordinates.get(i - 1).latitude,
                            coordinates.get(i).longitude, coordinates.get(i - 1).longitude);
                }

                double seconds = Long.parseLong((values.get(index).getText().toString())) * 1000.0;

                postTime += (double) Math.round(seconds*10)/10000.0;

                if(distanceValues.size() != 0){
                    distanceValues.add((double)Math.round(((double)Math.round(distanceDifference*100)/100
                            - distanceValues.get(distanceValues.size()-1))*100)/100);
                    postDistance += Math.round(((double) Math.round(distanceDifference*100)/100
                            - distanceValues.get(distanceValues.size()-1))*100)/100;
                }else {
                    distanceValues.add((double)Math.round(distanceDifference*100)/100);
                    postDistance += Math.round(distanceDifference*100)/100;
                }
                timeValues.add((double) Math.round(seconds*10)/10000.0);
                speedValues.add(Math.round((distanceValues.get(index)/timeValues.get(index))*100.0)/100.0);

                gainedPoints = (int)(postDistance/postTime);
                if(gainedPoints >= 7){
                    gainedPoints = 0;
                }
                speed = Math.round((postDistance/postTime)*100.0)/100.0;

                RunActivity.showDistance.setText("Distance: " + postDistance + " metres");
                RunActivity.showTime.setText("Time: " + postTime + " seconds");

                if(index == rows.size() - 1){
                    finishTrainingButton.setEnabled(true);
                }else{
                    continueTrainingButton.setEnabled(true);
                }
            }
        });
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training, container, false);
    }
}