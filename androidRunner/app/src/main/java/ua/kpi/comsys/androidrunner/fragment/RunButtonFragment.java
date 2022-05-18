package ua.kpi.comsys.androidrunner.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.RunActivity;

import static ua.kpi.comsys.androidrunner.permission.MapsPermissionActivity.trainingIsChosen;

public class RunButtonFragment extends Fragment {

    private AppCompatButton runButton;
    public static String EnteredTime;

    Animation animationLeft;
    Animation animationRight;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Animation animationAlpha = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.alpha_run);
        animationLeft = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.set_left);
        animationRight = AnimationUtils.loadAnimation((RunActivity)getActivity(), R.anim.set_right);
        animationLeft.setDuration(1200);
        animationRight.setDuration(1200);
        animationAlpha.setDuration(500);

        runButton = getView().findViewById(R.id.run_button);
        runButton.startAnimation(animationAlpha);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smthOpen((RunActivity)getActivity());
            }
        });

        if(trainingIsChosen){
            trainingIsChosen = false;
            Fragment fragment = new TrainingFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.hide(getActivity().getSupportFragmentManager().findFragmentById(R.id.run_fragment))
                    .add(R.id.run_fragment, fragment);
            fragmentTransaction.addToBackStack("back");
            fragmentTransaction.commit();
        }
    }

    public void smthOpen(Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
        builder.setTitle("Choice");
        builder.setMessage("Choose the way to set your run");

        builder.setPositiveButton("Stopwatch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chronometerOpen();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Timer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder timeBuilder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
                final View timePickerDialog = getLayoutInflater().inflate(R.layout.pick_time_dialog, null);

                timeBuilder.setView(timePickerDialog);
                timeBuilder.setTitle("Enter time");
                timeBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        EditText editText = timePickerDialog.findViewById(R.id.set_time);
                        EnteredTime = editText.getText().toString();
                        timerOpen();
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
                //timerOpen();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void timerOpen(){
        runButton.startAnimation(animationRight);
        new CountDownTimer(1100, 100){
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Fragment fragment = new TimerFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.hide(getActivity().getSupportFragmentManager().findFragmentById(R.id.run_fragment))
                        .add(R.id.run_fragment, fragment);
                fragmentTransaction.addToBackStack("back");
                fragmentTransaction.commit();
            }
        }.start();
    }

    public void chronometerOpen(){
        runButton.startAnimation(animationLeft);
        new CountDownTimer(1200, 200){
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Fragment fragment = new ChronometerFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.hide(getActivity().getSupportFragmentManager().findFragmentById(R.id.run_fragment))
                        .add(R.id.run_fragment, fragment);
                fragmentTransaction.addToBackStack("back");
                fragmentTransaction.commit();
            }
        }.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run_button, container, false);
    }
}