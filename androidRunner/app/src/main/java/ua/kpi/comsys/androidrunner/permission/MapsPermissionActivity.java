package ua.kpi.comsys.androidrunner.permission;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import ua.kpi.comsys.androidrunner.HomeActivity;
import ua.kpi.comsys.androidrunner.R;
import ua.kpi.comsys.androidrunner.RunActivity;

public class MapsPermissionActivity extends AppCompatActivity {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static boolean trainingIsChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_permission);

        ImageView permissionCheck = (ImageView) findViewById(R.id.permission_button);
        //View view = findViewById(R.id.view);

        Animation animationScaleBack = AnimationUtils.loadAnimation(this, R.anim.run_scale_back);
        animationScaleBack.setDuration(1000);
        animationScaleBack.setRepeatCount(1000);

        Animation animationScale = AnimationUtils.loadAnimation(this, R.anim.run_scale);
        animationScale.setDuration(1000);
        animationScale.setRepeatCount(1000);

        //view.startAnimation(animationScale);
        permissionCheck.startAnimation(animationScaleBack);

        permissionCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsPermissionActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("Choice");
                builder.setMessage("Choose what do you want to do: ропаолрмосрпрспмридплпгалоплрпсррмлвдгвю");

                builder.setPositiveButton("Training", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trainingIsChosen = true;
                        HomeActivity.redirectActivity(MapsPermissionActivity.this, RunActivity.class);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Just run", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.redirectActivity(MapsPermissionActivity.this, RunActivity.class);
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }
}